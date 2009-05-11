package org.sonatype.nexus.proxy.repository;

public class RemoteProxySettings
{
    private String hostname;

    private int port;

    private RemoteAuthenticationSettings proxyAuthentication;

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
