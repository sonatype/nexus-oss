package org.sonatype.nexus.integrationtests.webproxy;

import org.junit.After;
import org.junit.Before;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public abstract class AbstractNexusWebProxyIntegrationTest
    extends AbstractNexusProxyIntegrationTest
{

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    @Before
    public void startWebProxy()
        throws Exception
    {
        ProxyServer server = (ProxyServer) this.lookup( ProxyServer.ROLE );
        server.start();
    }

    @After
    public void stopWebProxy()
        throws Exception
    {
        ProxyServer server = (ProxyServer) this.lookup( ProxyServer.ROLE );
        server.stop();
    }

}
