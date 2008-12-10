/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus779;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus779DeployRssTest
    extends AbstractRssTest
{

    @Test
    public void restDeployRssCheck()
        throws Exception
    {
        deployRest( "artifact1" );
        feedListContainsArtifact( "nexus779", "artifact1", "1.0" );
        deployRest( "artifact2" );
        feedListContainsArtifact( "nexus779", "artifact2", "1.0" );
    }

    @Test
    public void wagonDeployRSSCheck()
        throws Exception
    {
        deployWagon( "artifact3" );
        feedListContainsArtifact( "nexus779", "artifact3", "1.0" );

        deployWagon( "artifact4" );
        feedListContainsArtifact( "nexus779", "artifact4", "1.0" );
    }

    private void deployWagon( String artifactName )
        throws Exception
    {
        File jarFile = getTestFile( artifactName + ".jar" );
        File pomFile = getTestFile( artifactName + ".pom" );

        String deployUrl = baseNexusUrl + "content/repositories/" + REPO_TEST_HARNESS_REPO;
        DeployUtils.deployWithWagon( this.container, "http", deployUrl, jarFile, "nexus779/" + artifactName + "/1.0/"
            + artifactName + "-1.0.jar" );
        DeployUtils.deployWithWagon( this.container, "http", deployUrl, pomFile, "nexus779/" + artifactName + "/1.0/"
            + artifactName + "-1.0.pom" );

    }

    private int deployRest( String artifactName )
        throws Exception
    {
        File jarFile = getTestFile( artifactName + ".jar" );
        File pomFile = getTestFile( artifactName + ".pom" );

        int status = DeployUtils.deployUsingPomWithRest( REPO_TEST_HARNESS_REPO, jarFile, pomFile, "", "jar" );
        return status;
    }

}
