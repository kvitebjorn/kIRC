package kirc;

import java.util.ArrayList;

public class Message 
{
    private final String _prefix;
    private final String _command;
    private final ArrayList<String> _parameters;
    
    public Message(String prefix, String command, ArrayList<String> parameters)
    {
        _prefix = prefix;
        _command = command;
        _parameters = parameters;
    }
    
    public String getPrefix()
    {
        return _prefix;
    }
    
    public String getCommand()
    {
        return _command;
    }
    
    public ArrayList<String> getParameters()
    {
        return _parameters;
    }
    
    public int getParametersLength()
    {
        return _parameters.size();
    }
}
