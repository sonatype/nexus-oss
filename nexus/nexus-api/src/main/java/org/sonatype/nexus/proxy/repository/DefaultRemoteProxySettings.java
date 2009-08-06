package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;

public class DefaultRemoteProxySettings
    implements RemoteProxySettings
{
    private boolean blockInheritance;

    private String hostname;

    private int port;

    private RemoteAuthenticationSettings proxyAuthentication;

    public boolean isEnabled()
    {
        return StringUtils.isNotBlank( getHostname() ) && getPort() != 0;
    }

    public boolean isBlockInheritance()
    {
        return blockInheritance;
    }

    public void setBlockInheritance( boolean blockInheritance )
    {
        this.blockInheritance = blockInheritance;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname( String hostname )
    {
        this.hostname = hostname;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public RemoteAuthenticationSettings getProxyAuthentication()
    {
        return proxyAuthentication;
    }

    public void setProxyAuthentication( RemoteAuthenticationSettings proxyAuthentication )
    {
        this.proxyAuthentication = proxyAuthentication;
    }
}
