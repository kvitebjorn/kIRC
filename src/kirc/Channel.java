package kirc;

import java.util.ArrayList;
import java.util.Arrays;

public class Channel
{
    private String _channelName;
    private String _banner;
    private ArrayList<String> _users;
    
    public Channel(String name)
    {
        _channelName = name;
        _users = new ArrayList<>();
        _banner = "";
    }
    
    public String getChannelName()
    {
        return _channelName;
    }
    
    public String getBanner()
    {
        return _banner;
    }
    
    public void setBanner(String msg)
    {
        _banner = msg;
    }
    
    public ArrayList<String> getUsersList()
    {
        return _users;
    }
    
    public void setUsersList(String[] usersList)
    {
        _users.addAll(Arrays.asList(usersList));
    }
    
    public void addUserToList(String user)
    {
        _users.add(user);
    }
    
    public void removeUserFromList(String user)
    {
        _users.remove(user);
    }
}
