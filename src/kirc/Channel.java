package kirc;

import java.util.ArrayList;

public class Channel
{
    private String _channelName;
    private String _banner;
    private ArrayList<String> _users;
    
    public Channel(String name, String banner, String usersListString)
    {
        _channelName = name;
        _banner = banner;
        fillUsersList(usersListString);
    }
    
    private void fillUsersList(String usersList)
    {
        return; //TODO
    }
    
    public String getChannelName()
    {
        return _channelName;
    }
    
    public String getBanner()
    {
        return _banner;
    }
}
