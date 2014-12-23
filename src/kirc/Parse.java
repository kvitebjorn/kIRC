package kirc;

import java.util.ArrayList;

public class Parse 
{
    static Message parseIrcMessage(String message)
    {
        Message msg;
        int prefixEnd = -1;
        int trailingStart = message.length();
        String trailing = "";
        String prefix = "";
        String command = "";
        ArrayList<String> parameters = new ArrayList<>();
 
        // get the prefix (if it exists)
        if (message.startsWith(":"))
        {
            prefixEnd = message.indexOf(" ");
            prefix = message.substring(1, prefixEnd);
        }
 
        // grab the trailing (if it exists)
        trailingStart = message.indexOf(" :");
        if (trailingStart >= 0)
            trailing = message.substring(trailingStart + 2);
        else
            trailingStart = message.length();
 
        // extract command and parameters
        String[] commandOgParameters = message.substring(prefixEnd + 1, trailingStart).split(" ");
        
        // command is først
        command = commandOgParameters[0];
 
        // skip the command and add the parameters
        if (commandOgParameters.length > 1)
            for(int i = 1; i < commandOgParameters.length; i++)
                parameters.add(commandOgParameters[i]); 
 
        // add trailing part if valid
        if (!trailing.isEmpty())
            parameters.add(trailing);
        
        msg = new Message(prefix, command, parameters);
        return msg;
    }
}
