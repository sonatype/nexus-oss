package org.sonatype.nexus.plugin.discovery;

public class NexusConnectionInfo
{

    private final String nexusUrl;

    private final String user;

    private String password;

    private final String connectionName;

    public NexusConnectionInfo( final String url, final String username, final String password,
                                final String connectionName )
    {
        nexusUrl = url;
        user = username;
        this.password = password;
        this.connectionName = connectionName;
    }

    public NexusConnectionInfo( final String url, final String username, final String password )
    {
        nexusUrl = url;
        user = username;
        this.password = password;
        this.connectionName = "-unnamed-";
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
}
