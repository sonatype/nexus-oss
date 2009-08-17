package org.sonatype.nexus.error.reporting;

import org.apache.commons.httpclient.HttpClient;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.HttpClientProxyUtil;
import org.sonatype.spice.utils.proxyserver.ProxyServerConfigurator;

public class NexusProxyServerConfigurator
    implements ProxyServerConfigurator
{
    private Logger logger;
    private RemoteStorageContext ctx;
    
    public NexusProxyServerConfigurator( RemoteStorageContext ctx, Logger logger )
    {
        this.ctx = ctx;
        this.logger = logger;
    }
    
    public void applyToClient( HttpClient client )
    {   
        HttpClientProxyUtil.applyProxyToHttpClient( client, ctx, logger );
    }
}
