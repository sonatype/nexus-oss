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
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Create an http server. Create a proxy repo to http server. Access a file from http server. Stop http server. access
 * file again (should work.) Clear cache and try it again.
 */
public class Nexus179RemoteRepoDownIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus179RemoteRepoDownIT()
    {
        super( REPO_RELEASE_PROXY_REPO1 );
    }

    @Test
    public void downloadFromDisconnectedProxy()
        throws Exception
    {
        // stop the proxy
        this.stopProxy();

        // delete everything under this tests group id if exist anything
        this.deleteFromRepository( "nexus179/" );

        Gav gav =
            new Gav( this.getTestId(), "repo-down-test-artifact", "1.0.0", null, "xml", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        File localFile = this.getLocalFile( REPO_RELEASE_PROXY_REPO1, gav );

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

        // unblock the proxy
        this.setBlockProxy( this.getBaseNexusUrl(), REPO_RELEASE_PROXY_REPO1, false );

        File artifact = this.downloadArtifact( gav, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

    private void clearProxyCache()
        throws Exception
    {

        String serviceURI = "service/local/data_cache/repositories/" + REPO_RELEASE_PROXY_REPO1 + "/content";

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not clear the cache for repo: " + REPO_RELEASE_PROXY_REPO1 );
        }

        TaskScheduleUtil.waitForTasks();
    }

}
