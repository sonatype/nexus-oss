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

    protected ProxyServer server;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    @Before
    public void startWebProxy()
        throws Exception
    {
        server = (ProxyServer) lookup( ProxyServer.ROLE );
        server.start();
    }

    @After
    public void stopWebProxy()
        throws Exception
    {
        server.stop();
    }

}
