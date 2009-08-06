package org.sonatype.nexus.proxy.repository;

public interface RemoteProxySettings
{
    boolean isEnabled();
    
    boolean isBlockInheritance();

    void setBlockInheritance( boolean val );

    String getHostname();

    void setHostname( String hostname );

    int getPort();

    void setPort( int port );

    RemoteAuthenticationSettings getProxyAuthentication();

    void setProxyAuthentication( RemoteAuthenticationSettings proxyAuthentication );
}
