package org.sonatype.nexus.integrationtests.nexus1329;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.restlet.data.MediaType;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.sonatype.nexus.test.utils.TestProperties;

public abstract class AbstractMirrorIT
    extends AbstractNexusIntegrationTest
{

    public static final String REPO = "nexus1329-repo";

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    @BeforeClass
    public static void init()
        throws Exception
    {
        cleanWorkDir();
    }

    protected ControlledServer server;

    protected MirrorMessageUtils messageUtil;

    public AbstractMirrorIT()
    {
        super();
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Before
    public void start()
        throws Exception
    {
        server = (ControlledServer) lookup( ControlledServer.ROLE );
    }

    @After
    public void stop()
        throws Exception
    {
        server.stop();
    }

}