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
package org.sonatype.nexus.integrationtests.nexus1329;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;

public class Nexus1329UnavailableIT
    extends AbstractMirrorIT
{

    /**
     * 5. download from mirror fails. download from repository fails. the mirror is NOT blacklisted
     */
    @Test
    public void downloadUnavailable()
        throws Exception
    {
        List<String> repoUrls = server.addServer( "repository", 500 );
        List<String> mirror1Urls = server.addServer( "mirror1", 500 );
        server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, false, null, false, null );

        try
        {
            this.downloadArtifactFromRepository( REPO, gav, "./target/downloads/nexus1329" );
            Assert.fail( "Artifact is not available, shouldn't download!" );
        }
        catch ( FileNotFoundException e )
        {
            // expected
        }

        Assert.assertFalse( "Nexus should try repository canonical url " + repoUrls, repoUrls.isEmpty() );
        Assert.assertFalse( "Nexus should try mirror 1 " + mirror1Urls, mirror1Urls.isEmpty() );

        MirrorStatusResourceListResponse response = this.messageUtil.getMirrorsStatus( REPO );

        MirrorStatusResource one = (MirrorStatusResource) response.getData().get( 0 );

        Assert.assertEquals( "http://localhost:" + webProxyPort + "/mirror1", one.getUrl() );
        Assert.assertEquals( "Available", one.getStatus() );
    }

}
