package kirc;

import java.io.EOFException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.net.InetAddress;

public class KIRC
{
    private BufferedWriter output; //output stream to server
    private BufferedReader input;  //input stream from server
    private Socket client;
    
    KIRCFrame _frame;
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
            _frame.getKIRCFrame().displayMessage("\nClient terminated connection");
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
        _frame.getKIRCFrame().displayMessage("Attempting connection\n");
        client = new Socket(InetAddress.getByName(_host), 6667);
        _frame.getKIRCFrame().addTab(_host);
        _frame.getKIRCFrame().displayMessage("Connected to: " + client.getInetAddress().getHostName());
    }
    
    private void getStreams() throws IOException
    {
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        output.flush();
        
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
        _frame.getKIRCFrame().displayMessage("\nAcquired IO streams\n");
    }
    
    private void processConnection() throws IOException
    {
        _frame.getKIRCFrame().setTextFieldEditable(true);
        
        do
        {
            try
            {
                msg = (String) input.readLine();
                
                if (msg.startsWith("PING ")) 
                {
                    output.write("PONG " + msg.substring(5) + "\r\n");
                    output.flush( );
                }
                else
                    _frame.getKIRCFrame().displayMessage("\n" + msg);
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        } while(!msg.equals("SERVER>>> TERMINATE"));
    }
    
    private void closeConnection()
    {
        _frame.getKIRCFrame().displayMessage("\nClosing connection");
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
            _frame.getKIRCFrame().displayMessage("\n" + userMsg);
            
            String nickMsg = "NICK " + _nick + "\r\n";
            output.write(nickMsg);
            output.flush();
            _frame.getKIRCFrame().displayMessage("\n" + nickMsg);
            
            //wait for server here
            waitForConn();
        }
        catch(IOException ioException)
        {
            _frame.getKIRCFrame().displayMessage("\nError writing object");
        }
    }
    
    private void waitForConn()
    {
        try
        {
            while((msg = input.readLine()) != null)
            {
                _frame.getKIRCFrame().displayMessage("\n" + msg);
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
            _frame.getKIRCFrame().addTab(_channel);
            _frame.getKIRCFrame().displayMessage("Joining " + _channel + "...");
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
     
    private void sendData(String message)
    {
        try
        {
            output.write("PRIVMSG " + _channel + " :" + message + "\r\n");
            output.flush();
            _frame.getKIRCFrame().displayMessage("\n" + _nick + "> " + message);
        }
        catch(IOException ioException)
        {
            _frame.getKIRCFrame().displayMessage("\nError writing object");
        }
    }
    
    public void enterFieldFired(String msg)
    {
        sendData(msg);
    }
    
    public String getNick()
    {
        return _nick;
    }
}
