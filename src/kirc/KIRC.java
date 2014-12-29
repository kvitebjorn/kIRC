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
import java.util.Arrays;

public class KIRC
{
    private BufferedWriter output; //output stream to server
    private BufferedReader input;  //input stream from server
    private Socket client;
    
    KIRCFrame _frame;
    private final ArrayList<Channel> _channels;
    private final String _host;
    private String _nick;
    private String _oldNick; //incase of 433, etc
    private String msg = "";
    
    public KIRC(KIRCFrame frame, String host, String nick)
    {
        _frame   = frame;
        _host    = host;
        _nick    = nick;
        _oldNick = "";
        
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
            
            sendNICK(_nick);
            
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
    
    private void startPrivateMsg(final String user, final String pvtMsg, Boolean iStartedThis)
    {
        try
        {
            if(iStartedThis)
            {
                output.write("PRIVMSG " + user + " :" + pvtMsg + "\r\n");
                output.flush();
            }

            _channels.add(new Channel(user));
            _frame.getKIRCFrame().addTab(user);
            final int channelIndex = findChannelIndex(user);
            _channels.get(channelIndex).addUserToList(_nick);
            _channels.get(channelIndex).addUserToList(user);
            _frame.getKIRCFrame().setFocusOnChannel(channelIndex);
            _frame.getKIRCFrame().displayMessage("Starting private message with " + user + "...", channelIndex);
            
            if(iStartedThis)
                _frame.getKIRCFrame().displayMessage("\n" + _nick + "> " + pvtMsg, channelIndex);
            else
                _frame.getKIRCFrame().displayMessage("\n" + pvtMsg, channelIndex);
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
     
    private void sendData(final String message, final int channelIndex)
    {
        String[] enterFieldMsg = message.split(" ");
        
        try
        {
            switch (enterFieldMsg[0].toLowerCase())
            {
                case "/join":
                    if(enterFieldMsg.length != 2)
                    {
                        if(channelIndex != -1)
                            _frame.getKIRCFrame().displayMessage("\nIncorrect number of parameters", 0);
                    }
                    else
                    {
                        final String channel = enterFieldMsg[1];
                        joinChannel(channel);
                    }   
                    break;
                case "/msg":
                    if(enterFieldMsg.length < 2)
                    {
                        if(channelIndex != -1)
                            _frame.getKIRCFrame().displayMessage("\nIncorrect number of parameters", 0);
                    }
                    else
                    {
                        final String user = enterFieldMsg[1];
                        int index = -1;
                        
                        for(int i = 0; i < _channels.size(); i++)
                            if(_channels.get(i).getChannelName().toLowerCase().equals(user.toLowerCase()))
                                index = findChannelIndex(user);
                        
                        String pvtMsg = "";
                        for(int i = 2; i < enterFieldMsg.length; i++)
                            pvtMsg = pvtMsg.concat(enterFieldMsg[i] + " ");
                        
                        if(index == -1)
                            startPrivateMsg(user, pvtMsg, true);
                        else
                            sendPRIVMSG(pvtMsg, index);
                    }   
                    break;
                case "/quit":
                case "/disconnect":
                    sendQUIT();
                    break;
                case "/nick":
                    if(enterFieldMsg.length != 2)
                    {
                        if(channelIndex != -1)
                            _frame.getKIRCFrame().displayMessage("\nIncorrect number of parameters", 0);
                    }
                    else
                    {
                        _oldNick = _nick;
                        final String newNick = enterFieldMsg[1];
                        _nick = newNick;
                        sendNICK(newNick);
                        _frame.getKIRCFrame().displayMessage("\nNick changed to " + newNick, 0);
                        _frame.getKIRCFrame().setUserNameLabel(newNick);
                    }
                    break;
                default:
                    sendPRIVMSG(message, channelIndex);
                    break;
            }
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
            case "QUIT":
                processQUIT(message, originalMessage);
                break;
            case "PART":
                processPART(message);
                break;
            case "NICK":
                processNICK(message);
                break;
            case "332":
                process332(message);
                break;
            case "353":
                process353(message, msg);
                break;
            case "366":
                process366(message, msg);
                break;
            case "433":
                process433();
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
        final String nick       = prefix.substring(0, prefix.indexOf("!"));
        final String channel    = message.getParameters().get(0);
        final String channelMsg = nick + "> " + message.getParameters().get(1);
        int channelIndex  = -1;
        
        if(channel.equals(_nick))
        {
            if(findChannelIndex(nick) != -1)
                channelIndex = findChannelIndex(nick);
            else
            {
                startPrivateMsg(nick, channelMsg, false);
            }
        }
        else
            channelIndex = findChannelIndex(channel);
        
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);        
    }
    
    private void processJOIN(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String nick       = prefix.substring(0, prefix.indexOf("!"));
        final String channel    = message.getParameters().get(0);
        final String channelMsg = prefix + " " + message.getCommand() + "ED " + channel;
        final int channelIndex  = findChannelIndex(channel);
        if(channelIndex != -1)
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
        
        if(!nick.equals(_nick))
        {
            _channels.get(channelIndex).addUserToList(nick);
            final ArrayList<String> userList = _channels.get(channelIndex).getUsersList();
            if(_frame.getKIRCFrame().getChannelFocus() == channelIndex)
            {
                _frame.getKIRCFrame().updateUserCountLabel(userList.size());
                _frame.getKIRCFrame().setUserList(userList.toArray(new String[userList.size()]));
            }
        }
    }
    
    private void processPART(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String nick       = prefix.substring(0, prefix.indexOf("!"));
        final String channel    = message.getParameters().get(0);
        final String channelMsg = prefix + " " + message.getCommand() + "ED " + channel;
        final int channelIndex  = findChannelIndex(channel);
        if(channelIndex != -1)
        {
            _frame.getKIRCFrame().displayMessage("\n" + channelMsg, channelIndex);
        
            _channels.get(channelIndex).removeUserFromList(nick);
            final ArrayList<String> userList = _channels.get(channelIndex).getUsersList();
            if(_frame.getKIRCFrame().getChannelFocus() == channelIndex)
            {
                _frame.getKIRCFrame().updateUserCountLabel(userList.size());
                _frame.getKIRCFrame().setUserList(userList.toArray(new String[userList.size()]));
            }
        }
    }
    
    private void processQUIT(final Message message, final String originalMessage) throws IOException
    {
        final String prefix = message.getPrefix();
        final String nick   = prefix.substring(0, prefix.indexOf("!")); 
        
        for(int i = 0; i < _channels.size(); i++)
            for(int j = 0; j < _channels.get(i).getUsersList().size(); j++)
                if(_channels.get(i).getUsersList().get(j).contains(nick))
                {
                    _channels.get(i).removeUserFromList(nick);
                    _frame.getKIRCFrame().displayMessage("\n" + originalMessage.substring(1), i);
                    
                    if(_frame.getKIRCFrame().getChannelFocus() == i)
                    {
                        final ArrayList<String> userList = _channels.get(i).getUsersList();
                        _frame.getKIRCFrame().updateUserCountLabel(userList.size());
                        _frame.getKIRCFrame().setUserList(userList.toArray(new String[userList.size()]));
                    }
                    break;
                }
    }
    
    private void processNICK(final Message message) throws IOException
    {
        final String prefix     = message.getPrefix();
        final String nick       = prefix.substring(0, prefix.indexOf("!"));
        final String newNick    = message.getParameters().get(0);
        final String newNickmsg = "\n" + nick + " is now known as " + newNick;
        
        //change all occurrences of nick to newNick in the channels
        for(int i = 0; i < _channels.size(); i++)
            for(int j = 0; j < _channels.get(i).getUsersList().size(); j++)
                if(_channels.get(i).getUsersList().get(j).contains(nick))
                {
                    _channels.get(i).getUsersList().set(j, newNick);
                    _frame.getKIRCFrame().displayMessage(newNickmsg, i);
                }
        final int channelFocus = _frame.getKIRCFrame().getChannelFocus();
        if(channelFocus != -1)
            _frame.getKIRCFrame().setUserList(_channels.get(channelFocus).getUsersList().
                toArray(new String[_channels.get(channelFocus).getUsersList().size()]));
    }
    
    private void process332(final Message message) throws IOException
    {
        final String channelName   = message.getParameters().get(1);
        final String channelNotice = message.getParameters().get(2);
        
        for(int i = 0; i < _channels.size(); i++)
        {
            if(_channels.get(i).getChannelName().equals(channelName))
            {
                _channels.get(i).setBanner(channelNotice);
                _frame.getKIRCFrame().setBannerNow(channelNotice);
                break;
            }
        }
    }
    
    private void process353(final Message message, final String originalMessage) throws IOException
    {
        final String channel = message.getParameters().get(2);
        final int channelIndex = findChannelIndex(channel);
        if(channelIndex != -1)
        {
            final String[] users = message.getParameters().get(3).split(" ");
            _channels.get(channelIndex).setUsersList(users);
        }
    }
    
    private void process366(final Message message, final String originalMessage) throws IOException
    {
        final String channel = message.getParameters().get(1);
        final int channelIndex = findChannelIndex(channel);
        if(channelIndex != -1)
        {
            _frame.getKIRCFrame().setUserList(_channels.get(channelIndex).getUsersList().
                    toArray(new String[_channels.get(channelIndex).getUsersList().size()]));
        }
    }
    
    private void process433() throws IOException
    {
        _frame.getKIRCFrame().displayMessage("\n" + _nick + " is already in use", 0);
        _nick = _oldNick;
        _frame.getKIRCFrame().setUserNameLabel(_nick);
    }
    
    public void removeChannel(int i)
    {
        if(i < _channels.size())
            _channels.remove(i);
    }
    
    public void sendPARTOneChannel(int channelIndex) throws IOException
    {
        final String channelName = _channels.get(channelIndex).getChannelName();
        if(channelName.contains("#")) //avoid private messages
        {
            output.write("PART " + channelName + " :" + "hadet" + "\r\n");
            output.flush();
        }
    }
    
    public void sendQUIT() throws IOException
    {
        output.write("QUIT\r\n");
        output.flush();
    }
    
    private void sendNICK(final String nick) throws IOException
    {
        final String nickMsg = "NICK " + nick + "\r\n";
        output.write(nickMsg);
        output.flush();
    }
    
    private void sendPRIVMSG(final String message, final int channelIndex) throws IOException
    {
        final String channelName = _channels.get(channelIndex).getChannelName();
        output.write("PRIVMSG " + channelName + " :" + message + "\r\n");
        output.flush();
        _frame.getKIRCFrame().displayMessage("\n" + _nick + "> " + message, channelIndex);
    }
}
