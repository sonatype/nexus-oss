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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 *
 * The nexus.xml for this test is located: /nexus-test-harness-launcher/src/test/resources/nexus1329/test-config/nexus.xml
 * 
 * The mirrors are located: /nexus-test-harness-launcher/src/test/resources/proxyRepo/nexus1329
 * They are located on the same server with different URLs.
 *
 */
public class Nexus1329MirrorTest
    extends AbstractNexusProxyIntegrationTest // AbstractNexusProxyIntegrationTest should be moved out of proxy package when build is stable
{

    @Test
    public void downloadFromMirrorTest()
        throws IOException
    {
        // add a mirror
        // TODO: add REST call

        // build a GAV for the artifact
        Gav gav = new Gav(
            this.getTestId(),
            "sample",
            "1.0.0",
            null,
            "xml",
            0,
            new Date().getTime(),
            "Artifact Name",
            false,
            false,
            null,
            false,
            null );

        String repositoryId = "nexus1329-repo";

        // download file from repo
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloadedFiles/repo" );
        Assert.assertNotNull( artifactFile );
        Assert.assertTrue( artifactFile.exists() );

        // if you want to check the downloaded file against a known file you can compare the SHA1's
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( this.getLocalFile(
            this.getTestId() + "/repository",
            gav ), artifactFile ) );

        // download file from group
        artifactFile = this.downloadArtifactFromGroup( "nexus1329-group", gav, "./target/downloadedFiles/group" );

    }

    @Test
    public void downloadFileThatIsOnlyInMirrorTest() throws IOException
    {
        // build a GAV for the artifact
        Gav gav = new Gav(
            this.getTestId(),
            "sample",
            "1.0.1", // Different version then above
            null,
            "xml",
            0,
            new Date().getTime(),
            "Artifact Name",
            false,
            false,
            null,
            false,
            null );

        String repositoryId = "nexus1329-repo";

        // download file from repo
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloadedFiles/repo" );
        Assert.assertNotNull( artifactFile );
        Assert.assertTrue( artifactFile.exists() );

        // if you want to check the downloaded file against a known file you can compare the SHA1's
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( this.getLocalFile(
            this.getTestId() + "/nexus1329-repo",
            gav ), artifactFile ) );

        // download file from group
        artifactFile = this.downloadArtifactFromGroup( "nexus1329-group", gav, "./target/downloadedFiles/group" );
    }

}
