package org.sonatype.nexus.auth;

public class AuthenticationItem
{
    private String userid;
    private String remoteIP;
    private boolean success;
    
    public AuthenticationItem( String userid, String remoteIP, boolean success )
    {
        this.userid = userid;
        this.remoteIP = remoteIP;
        this.success = success;
    }
    
    public String getRemoteIP()
    {
        return remoteIP;
    }
    public String getUserid()
    {
        return userid;
    }
    public boolean isSuccess()
    {
        return success;
    }
}
