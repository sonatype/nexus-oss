package org.sonatype.nexus.integrationtests.nexus3947;

import java.net.URL;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3947ArchetypeCatalogIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testArchetypeCatalog()
        throws Exception
    {
        Response response;

        ArchetypeCatalog catalog;

        ArchetypeCatalogXpp3Reader acr = new ArchetypeCatalogXpp3Reader();

        // path of catalog
        String relativePath = "archetype-catalog.xml";
        String url = getRepositoryUrl( getTestRepositoryId() ) + relativePath;

        // request the catalog
        response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        // read and check
        catalog = acr.read( response.getEntity().getReader() );
        Assert.assertEquals( catalog.getArchetypes().size(), 1 );

        // deploy one new archetype
        int httpResponseCode =
            getDeployUtils().deployUsingPomWithRest( getTestRepositoryId(), getTestFile( "simple-archetype2.jar" ),
                getTestFile( "simple-archetype2.pom" ), null, null );
        Assert.assertTrue( Status.isSuccess( httpResponseCode ), "Unable to deploy artifact " + httpResponseCode );
        
        // wait
        getEventInspectorsUtil().waitForCalmPeriod();

        // request the catalog
        response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        // read and check
        catalog = acr.read( response.getEntity().getReader() );
        Assert.assertEquals( catalog.getArchetypes().size(), 2 );
    }
}
