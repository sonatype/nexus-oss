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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1329ChecksumTest
    extends AbstractMirrorTest
{

    @Test
    public void downloadChecksumTest()
        throws Exception
    {
        File content = getTestFile( "basic" );
        File metadata = getTestFile( "metadata" );

        List<String> repoUrls = server.addServer( "repository", metadata );
        List<String> mirror1Urls = server.addServer( "mirror1", content );
        List<String> mirror2Urls = server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, false, null, false, null );

        File artifactFile = this.downloadArtifactFromRepository( REPO, gav, "./target/downloads/nexus1329" );

        File originalFile = this.getTestFile( "basic/nexus1329/sample/1.0.0/sample-1.0.0.xml" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifactFile ) );

        Assert.assertTrue( "Nexus should not access second mirror " + mirror2Urls, mirror2Urls.isEmpty() );
        Assert.assertFalse( "Nexus did not download checksum " + repoUrls, repoUrls.isEmpty() );
        Assert.assertFalse( "Nexus should access first mirror " + mirror1Urls, mirror1Urls.isEmpty() );
    }

}
