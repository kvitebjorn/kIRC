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
    private ArrayList<Channel> _channels;
    private String _host;
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
        
        //add host to channel list
        _channels.add(new Channel(_host, "", ""));
        
        _frame.getKIRCFrame().setVisible(true);
    }
    
    public void runClient()
    {
        try
        {
            connectToServer();
            getStreams();
            sendIRCConnInfo();
            joinChannel();
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
                // move all this to PARSE.java and refactor
                msg = (String) input.readLine();
                
                if (msg.startsWith("PING ")) 
                {
                    output.write("PONG " + msg.substring(5) + "\r\n");
                    output.flush( );
                }
                else
                {
                    String[] smsg = msg.split(" ");
                    if(smsg[1].equals("PRIVMSG"))
                    {
                        String sender     = smsg[0].split("!")[0].substring(1);
                        String channel    = smsg[2];
                        String channelMsg = sender + "> " + smsg[3].substring(1);
                        
                        int channelIndex;
                        for(channelIndex = 0; channelIndex < _channels.size(); channelIndex++)
                        {
                            if(_channels.get(channelIndex).getChannelName().toLowerCase().equals(channel.toLowerCase()))
                            {
                                _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
                                break;
                            }
                        } 
                    }
                    else
                    {
                        //write in server tab for now
                        _frame.getKIRCFrame().displayMessage("\n" + msg, 0);
                    }
                }
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        } while(!msg.equals("SERVER>>> TERMINATE")); //TODO
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
            
            //wait for server here
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
    
    private void joinChannel()
    {
        try
        {
            output.write("JOIN " + _channel + "\r\n");
            output.flush();

            _channels.add(new Channel(_channel, "", ""));
            _frame.getKIRCFrame().addTab(_channel);
            int channelIndex = _frame.getKIRCFrame().getChannelPaneSize() - 1;
            _frame.getKIRCFrame().setFocusOnChannel(channelIndex);
            _frame.getKIRCFrame().displayMessage("Joining " + _channel + "...", channelIndex);
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
            //get focused channel's name
            String channelName = _channels.get(channelIndex).getChannelName();
            
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
        // get the index of the current channel tab focus
        int i = _frame.getKIRCFrame().getChannelFocus();
        
        sendData(msg, i);
    }
    
    public String getNick()
    {
        return _nick;
    }
}
