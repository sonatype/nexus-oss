package org.sonatype.nexus.plugin.deploy;

import java.io.File;

public class ZapperRequest
{
    private final File stageRepository;

    private final String remoteUrl;

    private String remoteUsername;

    private String remotePassword;

    private String proxyProtocol;

    private String proxyHost;

    private int proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    public ZapperRequest( File stageRepository, String remoteUrl )
    {
        this.stageRepository = stageRepository;
        this.remoteUrl = remoteUrl;
    }

    protected String getRemoteUsername()
    {
        return remoteUsername;
    }

    protected void setRemoteUsername( String remoteUsername )
    {
        this.remoteUsername = remoteUsername;
    }

    protected String getRemotePassword()
    {
        return remotePassword;
    }

    protected void setRemotePassword( String remotePassword )
    {
        this.remotePassword = remotePassword;
    }

    protected String getProxyProtocol()
    {
        return proxyProtocol;
    }

    protected void setProxyProtocol( String proxyProtocol )
    {
        this.proxyProtocol = proxyProtocol;
    }

    protected String getProxyHost()
    {
        return proxyHost;
    }

    protected void setProxyHost( String proxyHost )
    {
        this.proxyHost = proxyHost;
    }

    protected int getProxyPort()
    {
        return proxyPort;
    }

    protected void setProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
    }

    protected String getProxyUsername()
    {
        return proxyUsername;
    }

    protected void setProxyUsername( String proxyUsername )
    {
        this.proxyUsername = proxyUsername;
    }

    protected String getProxyPassword()
    {
        return proxyPassword;
    }

    protected void setProxyPassword( String proxyPassword )
    {
        this.proxyPassword = proxyPassword;
    }

    protected File getStageRepository()
    {
        return stageRepository;
    }

    protected String getRemoteUrl()
    {
        return remoteUrl;
    }
}
