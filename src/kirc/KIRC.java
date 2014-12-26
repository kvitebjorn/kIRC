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
    
    public Channel getChannel(int index)
    {
        Channel channel = null;
        if(index < _channels.size() && index > -1)
            channel = _channels.get(index);
        return channel;
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
        Message message = null;
        
        do
        {
            try
            {
                msg = (String) input.readLine();
                if(msg != null)
                {
                    message = Parse.parseIrcMessage(msg);
                    processCommand(message, msg);
                }
            }
            
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        } while(!msg.equals("SERVER TERMINATE") && msg != null); //TODO
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
            final String userMsg = "USER " + _nick + " " + _nick + " " + 
                    _nick + " :" + _nick + "\r\n";
            output.write(userMsg); 
            output.flush();
            _frame.getKIRCFrame().displayMessage("\n" + userMsg, 0);
            
            final String nickMsg = "NICK " + _nick + "\r\n";
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
            final int channelIndex = findChannelIndex(channel);
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
            final String channelName = _channels.get(channelIndex).getChannelName();
            
            //TODO: create IRC commands per user input from the text field event fire
            // like /JOIN and /NICK, etc. Default for now is PRIVMSG
            // call the functions like sendPART() from here after parse
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
        final int i = _frame.getKIRCFrame().getChannelFocus();
        
        sendData(msg, i);
    }
    
    public String getNick()
    {
        return _nick;
    }
    
    private void processCommand(final Message message, final String originalMessage) throws IOException
    {
        final String command = message.getCommand();
        
        switch(command)
        {
            case "PING":
                processPING(originalMessage);
                break;
            case "PRIVMSG":
                processPRIVMSG(message);
                break;
            case "JOIN":
                processJOIN(message);
                break;
            case "PART":
                processPART(message);
                break;
            case "353":
                process353(message, msg);
                break;
            case "366":
                process366(message, msg);
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
        Boolean found = false;
        
        for(channelIndex = 0; channelIndex < _channels.size(); channelIndex++)
            if(_channels.get(channelIndex).getChannelName().toLowerCase().equals(channel.toLowerCase()))
            {
                found = true;
                break;
            }
        
        if(!found)
        {
            channelIndex = -1;
        }
        
        return channelIndex;
    }
    
    private void processPING(final String msg) throws IOException
    {
            output.write("PONG " + msg.substring(5) + "\r\n");
            output.flush();
    }
    
    private void processPRIVMSG(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String nick       = prefix.substring(0, prefix.indexOf("!~"));
        final String channel    = message.getParameters().get(0);
        final String channelMsg = nick + "> " + message.getParameters().get(1);
        final int channelIndex  = findChannelIndex(channel);
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);        
    }
    
    private void processJOIN(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String channel    = message.getParameters().get(0);
        final String channelMsg = prefix + " " + message.getCommand() + "ED " + channel;
        final int channelIndex  = findChannelIndex(channel);
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
    }
    
    private void processPART(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String channel    = message.getParameters().get(0);
        final String channelMsg = prefix + " " + message.getCommand() + "ED " + channel;
        final int channelIndex  = findChannelIndex(channel);
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex); 
    }
    
    private void process353(final Message message, final String originalMessage) throws IOException
    {
        final String channel = message.getParameters().get(2);
        final int channelIndex = findChannelIndex(channel);
        if(channelIndex != -1)
        {
            final String[] users = message.getParameters().get(3).split(" ");
            _channels.get(channelIndex).setUsersList(users);
            //update user list panel
            _frame.getKIRCFrame().setUserList(users);
            _frame.getKIRCFrame().displayMessage("\n" + originalMessage, channelIndex); 
        }
    }
    
    private void process366(final Message message, final String originalMessage) throws IOException
    {
        final String channel = message.getParameters().get(1);
        final int channelIndex = findChannelIndex(channel);
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + originalMessage, channelIndex);
    }
    
    public void removeChannel(int i)
    {
        if(i < _channels.size())
            _channels.remove(i);
    }
    
    public void sendPARTOneChannel(int channelIndex) throws IOException
    {
        final String channelName = _channels.get(channelIndex).getChannelName();
        output.write("PART " + channelName + " :" + "hadet" + "\r\n");
        output.flush();
    }
    
    public void sendQUIT() throws IOException
    {
        output.write("QUIT " + "\r\n");
        output.flush();
    }
}
