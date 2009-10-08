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
package org.sonatype.nexus.integrationtests.webproxy.nexus1101;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1101NexusOverWebproxyIT
    extends AbstractNexusWebProxyIntegrationTest
{

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus1101", "artifact", "1.0", "pom" );
        File pomArtifact = this.downloadArtifact( "nexus1101", "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus1101", "artifact", "1.0", "jar" );
        File jarArtifact = this.downloadArtifact( "nexus1101", "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1101/artifact/1.0/artifact-1.0.jar";
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }

}
