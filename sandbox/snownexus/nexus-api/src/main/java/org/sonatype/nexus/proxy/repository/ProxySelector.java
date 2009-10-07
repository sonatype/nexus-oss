package org.sonatype.nexus.proxy.repository;

public interface ProxySelector
{
    RemoteProxySettings select( ProxyRepository proxy, String url );
}
