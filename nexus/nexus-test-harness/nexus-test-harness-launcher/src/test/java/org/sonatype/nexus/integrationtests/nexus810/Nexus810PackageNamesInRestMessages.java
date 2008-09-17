package org.sonatype.nexus.integrationtests.nexus810;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

/**
 * Checks to make sure the tasks don't have packages in the type field.
 */
public class Nexus810PackageNamesInRestMessages extends AbstractNexusIntegrationTest
{

    @Test
    public void checkForPackageNamesInResponse() throws IOException
    {
        // I like simple tests
        Response response = RequestFacade.doGetRequest( "service/local/schedule_types" );
        String responseText = response.getEntity().getText();
        Assert.assertFalse( "Found package names in response.", responseText.contains( "org.sonatype." ) );
    }
}
