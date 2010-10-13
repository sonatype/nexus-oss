package org.sonatype.nexus.integrationtests.nexus1329;

import org.restlet.data.MediaType;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractMirrorIT
    extends AbstractNexusIntegrationTest
{

    public static final String REPO = "nexus1329-repo";

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    protected ControlledServer server;

    protected MirrorMessageUtils messageUtil;

    public AbstractMirrorIT()
    {
        super();
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @BeforeMethod
    public void start()
        throws Exception
    {
        server = (ControlledServer) lookup( ControlledServer.ROLE );
    }

    @AfterMethod
    public void stop()
        throws Exception
    {
        // @After will be called even if there is a failure in @Before, and server is null!
        if ( server != null )
        {
            server.stop();
        }
    }

}