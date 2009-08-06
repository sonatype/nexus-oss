package org.sonatype.nexus.proxy.repository;

public class DefaultRemoteConnectionSettings
    implements RemoteConnectionSettings
{
    private int connectionTimeout = 1000;

    private int retrievalRetryCount = 3;

    private String queryString;

    private String userAgentCustomizationString;

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout( int connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRetrievalRetryCount()
    {
        return retrievalRetryCount;
    }

    public void setRetrievalRetryCount( int retrievalRetryCount )
    {
        this.retrievalRetryCount = retrievalRetryCount;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString( String queryString )
    {
        this.queryString = queryString;
    }

    public String getUserAgentCustomizationString()
    {
        return userAgentCustomizationString;
    }

    public void setUserAgentCustomizationString( String userAgentCustomizationString )
    {
        this.userAgentCustomizationString = userAgentCustomizationString;
    }
}
