package org.sonatype.nexus.integrationtests.nexus602;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class Nexus602SearchSnapshotArtifactTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void searchSnapshot()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPOSITORY_NEXUS_TEST_HARNESS_SNAPSHOT_REPO + "&g="
                + getTestId() + "&a=" + "artifact" + "&v=" + "1.0-SNAPSHOT";
        Response response = RequestFacade.doGetRequest( serviceURI );
        Assert.assertEquals( "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString(), 301, response.getStatus().getCode() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString(), redirectRef );

        serviceURI = redirectRef.toString();

        response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );

        Assert.assertTrue( "Unable to fetch snapshot artifact", response.getStatus().isSuccess() );
    }

    @Test
    public void searchRelease()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPOSITORY_NEXUS_TEST_HARNESS_REPO + "&g=" + getTestId()
                + "&a=" + "artifact" + "&v=" + "1.0";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertTrue( "Unable to fetch release artifact", response.getStatus().isSuccess() );
    }

    @Test
    public void searchInvalidArtifact()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPOSITORY_NEXUS_TEST_HARNESS_REPO + "&g=" + "invalidGroupId"
                + "&a=" + "invalidArtifact" + "&v=" + "32.64";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertTrue( "Unable to fetch release artifact", response.getStatus().isSuccess() );
    }

}
