package org.sonatype.nexus.integrationtests.proxy.nexus179;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus179RemoteRepoDownTest
    extends AbstractNexusProxyIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public Nexus179RemoteRepoDownTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void downloadFromDisconnectedProxy()
        throws Exception
    {
        // stop the proxy
        this.stopProxy();

        Gav gav =
            new Gav( this.getTestId(), "repo-down-test-artifact", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        File localFile = this.getLocalFile( TEST_RELEASE_REPO, gav );

        // make sure this exists first, or the test is invalid anyway.
        Assert.assertTrue( "The File: " + localFile + " does not exist.", localFile.exists() );

        try
        {
            this.downloadArtifact( gav, "target/downloads" );
            Assert.fail( "A FileNotFoundException should have been thrown." );
        }
        catch ( FileNotFoundException e )
        {
        }
        

        // Start up the proxy
        this.startProxy();

        // should not be able to download artifact after starting proxy, without clearing the cache.
        try
        {
            this.downloadArtifact( gav, "target/downloads" );
            Assert.fail( "A FileNotFoundException should have been thrown." );
        }
        catch ( FileNotFoundException e )
        {
        }

        // unblock the proxy
        this.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false);

        File artifact = this.downloadArtifact( gav, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

    private void clearProxyCache()
    {

        String serviceURI =
            this.getBaseNexusUrl() + "service/local/data_cache/repositories/" + TEST_RELEASE_REPO + "/content";

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( Method.DELETE );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not clear the cache for repo: " + TEST_RELEASE_REPO );
        }
    }
    

}
