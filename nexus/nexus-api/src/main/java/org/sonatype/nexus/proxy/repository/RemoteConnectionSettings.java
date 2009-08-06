package org.sonatype.nexus.proxy.repository;

public interface RemoteConnectionSettings
{
    int getConnectionTimeout();

    void setConnectionTimeout( int connectionTimeout );

    int getRetrievalRetryCount();

    void setRetrievalRetryCount( int retrievalRetryCount );

    String getQueryString();

    void setQueryString( String queryString );

    String getUserAgentCustomizationString();

    void setUserAgentCustomizationString( String userAgentCustomizationString );
}
