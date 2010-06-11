package org.sonatype.nexus.integrationtests.nxcm2124;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class NXCM2124CheckConsoleDocumentationIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void checkDoc()
        throws IOException
    {
        Response r = RequestFacade.doGetRequest( "pluginConsole/docs/index.html" );
        Assert.assertTrue( r.getStatus().isSuccess() );
    }

}
