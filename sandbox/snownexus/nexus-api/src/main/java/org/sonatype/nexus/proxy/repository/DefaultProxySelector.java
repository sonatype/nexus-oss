package org.sonatype.nexus.proxy.repository;

/**
 * A default proxy selector implementation that implements the "default" proxy selection that was supported with nexus
 * up to version 1.3.
 * 
 * @author cstamas
 */
public class DefaultProxySelector
    implements ProxySelector
{
    public RemoteProxySettings select( ProxyRepository proxy, String url )
    {
        return proxy.getRemoteProxySettings();
    }
}
