package kirc;

import java.io.EOFException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.net.InetAddress;

import java.util.ArrayList;

public class KIRC
{
    private BufferedWriter output; //output stream to server
    private BufferedReader input;  //input stream from server
    private Socket client;
    
    KIRCFrame _frame;
    private final ArrayList<Channel> _channels;
    private final String _host;
    private String _channel;
    private String _nick;
    private String msg = "";
    
    public KIRC(KIRCFrame frame, String host, String channel, String nick)
    {
        _frame   = frame;
        _host    = host;
        _channel = channel;
        _nick    = nick;
        
        _channels = new ArrayList<>();
        
        _channels.add(new Channel(_host));
        
        _frame.getKIRCFrame().setVisible(true);
    }
    
    public void runClient()
    {
        try
        {
            connectToServer();
            getStreams();
            sendIRCConnInfo();
            joinChannel(_channel); //temporary: call from sendData after constructing a JOIN command
            processConnection();
        }
        catch(EOFException eofException)
        {
            _frame.getKIRCFrame().displayMessage("\nClient terminated connection", 0);
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
        finally
        {
            closeConnection();
        }
    }
    
    private void connectToServer() throws IOException
    {     
        _frame.getKIRCFrame().addTab(_host);
        _frame.getKIRCFrame().displayMessage("Attempting connection\n", 0);
        
        client = new Socket(InetAddress.getByName(_host), 6667);

        _frame.getKIRCFrame().displayMessage("\nConnected to: " + client.getInetAddress().getHostName(), 0);
    }
    
    private void getStreams() throws IOException
    {
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        output.flush();
        
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
        _frame.getKIRCFrame().displayMessage("\nAcquired IO streams\n", 0);
    }
    
    private void processConnection() throws IOException
    {
        _frame.getKIRCFrame().setTextFieldEditable(true);
        
        do
        {
            try
            {
                msg = (String) input.readLine();
                Message message = Parse.parseIrcMessage(msg);
                
                processCommand(message, msg);
            }
            
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        } while(!msg.equals("SERVER TERMINATE")); //TODO
    }
    
    private void closeConnection()
    {
        _frame.getKIRCFrame().displayMessage("\nClosing connection", 0);
        _frame.getKIRCFrame().setTextFieldEditable(false);
        
        try
        {
            output.close();
            input.close();
            client.close();
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
    
    private void sendIRCConnInfo()
    {
        try
        {
            String userMsg = "USER " + _nick + " " + _nick + " " + 
                    _nick + " :" + _nick + "\r\n";
            output.write(userMsg); 
            output.flush();
            _frame.getKIRCFrame().displayMessage("\n" + userMsg, 0);
            
            String nickMsg = "NICK " + _nick + "\r\n";
            output.write(nickMsg);
            output.flush();
            _frame.getKIRCFrame().displayMessage("\n" + nickMsg, 0);
            
            waitForConn();
        }
        catch(IOException ioException)
        {
            _frame.getKIRCFrame().displayMessage("\nError writing object", 0);
        }
    }
    
    private void waitForConn()
    {
        try
        {
            while((msg = input.readLine()) != null)
            {
                _frame.getKIRCFrame().displayMessage("\n" + msg, 0);
                if(msg.indexOf("004") >= 0)
                    break;
            }
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
    
    private void joinChannel(final String channel)
    {
        try
        {
            output.write("JOIN " + channel + "\r\n");
            output.flush();

            _channels.add(new Channel(channel));
            _frame.getKIRCFrame().addTab(channel);
            int channelIndex = findChannelIndex(channel);
            _frame.getKIRCFrame().setFocusOnChannel(channelIndex);
            _frame.getKIRCFrame().displayMessage("Joining " + channel + "...", channelIndex);
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
     
    private void sendData(final String message, final int channelIndex)
    {
        try
        {
            String channelName = _channels.get(channelIndex).getChannelName();
            
            //TODO: create IRC commands per user input from the text field event fire
            // like /JOIN and /NICK, etc. Default for now is PRIVMSG
            output.write("PRIVMSG " + channelName + " :" + message + "\r\n");
            output.flush();
            
            _frame.getKIRCFrame().displayMessage("\n" + _nick + "> " + message, channelIndex);
        }
        catch(IOException ioException)
        {
            _frame.getKIRCFrame().displayMessage("\nError writing object", channelIndex);
        }
    }
    
    public void enterFieldFired(final String msg)
    {
        int i = _frame.getKIRCFrame().getChannelFocus();
        
        sendData(msg, i);
    }
    
    public String getNick()
    {
        return _nick;
    }
    
    private void processCommand(final Message message, final String originalMessage) throws IOException
    {
        String command    = message.getCommand();
        String prefix     = message.getPrefix();
        int channelIndex  = 0;
        String channel    = "";
        String channelMsg = "";
        String nick       = "";
        
        switch(command)
        {
            case "PING":
                processPING(originalMessage);
                break;
            case "PRIVMSG":
                nick         = prefix.substring(0, prefix.indexOf("!~"));
                channel      = message.getParameters().get(0);
                channelMsg   = nick + "> " + message.getParameters().get(1);
                channelIndex = findChannelIndex(channel);
                _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
                break;
            case "JOIN":
                channel      = message.getParameters().get(0);
                channelMsg   = prefix + " " + message.getCommand() + "ED " + channel;
                channelIndex = findChannelIndex(channel);
                _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
                break;
            case "PART":
                channel      = message.getParameters().get(0);
                channelMsg   = prefix + " " + message.getCommand() + "ED " + channel;
                channelIndex = findChannelIndex(channel);
                _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex); 
                break;
            case "353":
                channel = message.getParameters().get(2);
                channelIndex = findChannelIndex(channel);
                String[] users = message.getParameters().get(3).split(" ");
                _channels.get(channelIndex).setUsersList(users);
                //update user list panel
                _frame.getKIRCFrame().setUserList(users);
                _frame.getKIRCFrame().displayMessage("\n" + originalMessage, channelIndex); 
                break;
            case "366":
                channel = message.getParameters().get(1);
                channelIndex = findChannelIndex(channel);
                _frame.getKIRCFrame().displayMessage("\n" + originalMessage, channelIndex);
                break;
            default:
                //write in server tab for now
                _frame.getKIRCFrame().displayMessage("\n" + originalMessage, 0);
                break;
        }
    }
    
    private int findChannelIndex(final String channel)
    {
        int channelIndex;
        for(channelIndex = 0; channelIndex < _channels.size(); channelIndex++)
            if(_channels.get(channelIndex).getChannelName().toLowerCase().equals(channel.toLowerCase()))
                break;
        return channelIndex;
    }
    
    private void processPING(final String msg) throws IOException
    {
            output.write("PONG " + msg.substring(5) + "\r\n");
            output.flush();
    }
    
    private void processPRIVMSG(final String msg)
    {
        throw new UnsupportedOperationException();
    }
    
    private void processJOIN(final String msg) throws IOException
    {
        throw new UnsupportedOperationException();
    }
    
    private void processPART(final String msg) throws IOException
    {
        throw new UnsupportedOperationException();
    }
    
    // process etc....
}
