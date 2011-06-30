package org.sonatype.nexus.plugin.discovery;

public class NexusConnectionInfo
{

    private final String nexusUrl;

    private String user;

    private String password;

    private String connectionName;

    private String connectionId;

    public NexusConnectionInfo( final String url )
    {
        this.nexusUrl = url;
    }

    public NexusConnectionInfo( final String url, final String username, final String password,
                                final String connectionName, final String connectionId )
    {
        nexusUrl = url;
        user = username;
        this.password = password;
        this.connectionName = connectionName;
        this.connectionId = connectionId;
    }

    public NexusConnectionInfo( final String url, final String username, final String password )
    {
        nexusUrl = url;
        user = username;
        this.password = password;
    }

    public boolean isConnectable()
    {
        return nexusUrl != null && user != null && password != null;
    }

    public NexusConnectionInfo setPassword( final String password )
    {
        this.password = password;
        return this;
    }

    public String getConnectionName()
    {
        return connectionName;
    }

    public String getNexusUrl()
    {
        return nexusUrl;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }

    public NexusConnectionInfo setUser( final String user )
    {
        this.user = user;
        return this;
    }

    public NexusConnectionInfo setConnectionName( final String name )
    {
        this.connectionName = name;
        return this;
    }

    public String getConnectionId()
    {
        return connectionId;
    }

    public NexusConnectionInfo setConnectionId( final String connectionId )
    {
        this.connectionId = connectionId;
        return this;
    }

}
