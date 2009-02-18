/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.proxy.nexus179;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * Create an http server. Create a proxy repo to http server. Access a file from http server. Stop http server. access
 * file again (should work.) Clear cache and try it again.
 */
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

        // delete everything under this tests group id
        Assert.assertTrue( "Expected item to be deleted.", this.deleteFromRepository( "nexus179/" ));

        Gav gav =
            new Gav( this.getTestId(), "repo-down-test-artifact", "1.0.0", null, "xml", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

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

        clearProxyCache();

        // Give task a chance to run
        Thread.sleep( 4000 );

        // unblock the proxy
        this.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false );

        File artifact = this.downloadArtifact( gav, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

    private void clearProxyCache()
        throws IOException
    {

        String serviceURI = "service/local/data_cache/repositories/" + TEST_RELEASE_REPO + "/content";

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not clear the cache for repo: " + TEST_RELEASE_REPO );
        }
    }

}
