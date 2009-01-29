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

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * The nexus.xml for this test is located:
 * /nexus-test-harness-launcher/src/test/resources/nexus1329/test-config/nexus.xml The mirrors are located:
 * /nexus-test-harness-launcher/src/test/resources/proxyRepo/nexus1329 They are located on the same server with
 * different URLs.
 */
public class Nexus1329MirrorTest
    extends AbstractNexusIntegrationTest // AbstractNexusProxyIntegrationTest should be moved out of proxy package when
// build is stable
{

    private ControlledServer server;

    @Before
    public void start()
        throws Exception
    {
        server = (ControlledServer) lookup( ControlledServer.ROLE );
    }

    @Test
    public void downloadFromMirrorTest()
        throws Exception
    {
        File content = getTestFile( "basic" );

        server.addServer( "repository", content );
        server.addServer( "mirror1", 500 );
        server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, false, null, false, null );

        String repositoryId = "nexus1329-repo";
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloads/nexus1329" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s(
                                                              this.getTestFile( "basic/nexus1329/sample/1.0.0/sample-1.0.0.xml" ),
                                                              artifactFile ) );
    }

    @Test
    public void downloadFileThatIsOnlyInMirrorTest()
        throws Exception
    {
        File content = getTestFile( "basic" );

        server.addServer( "repository", HttpServletResponse.SC_NOT_FOUND );
        server.addServer( "mirror1", content );
        server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, false, null, false, null );

        String repositoryId = "nexus1329-repo";
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloads/nexus1329" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s(
                                                              this.getTestFile( "basic/nexus1329/sample/1.0.0/sample-1.0.0.xml" ),
                                                              artifactFile ) );
    }

    @After
    public void stop()
        throws Exception
    {
        server.stop();
    }
}
