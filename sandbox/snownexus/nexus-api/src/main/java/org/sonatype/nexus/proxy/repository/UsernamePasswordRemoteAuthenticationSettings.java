package org.sonatype.nexus.proxy.repository;

public class UsernamePasswordRemoteAuthenticationSettings
    implements RemoteAuthenticationSettings
{
    private final String username;

    private final String password;

    public UsernamePasswordRemoteAuthenticationSettings( String username, String password )
    {
        this.username = username;

        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}
