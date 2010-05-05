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
package org.sonatype.nexus.integrationtests.nexus779;

import java.io.File;

import org.junit.Test;

public class Nexus779DeployRssIT
    extends AbstractRssIT
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

        String deployUrl = nexusBaseUrl + "content/repositories/" + REPO_TEST_HARNESS_REPO;
        getDeployUtils().deployWithWagon( "http", deployUrl, jarFile, "nexus779/" + artifactName + "/1.0/"
            + artifactName + "-1.0.jar" );
        getDeployUtils().deployWithWagon( "http", deployUrl, pomFile, "nexus779/" + artifactName + "/1.0/"
            + artifactName + "-1.0.pom" );

    }

    private int deployRest( String artifactName )
        throws Exception
    {
        File jarFile = getTestFile( artifactName + ".jar" );
        File pomFile = getTestFile( artifactName + ".pom" );

        int status = getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_REPO, jarFile, pomFile, "", "jar" );
        return status;
    }

}
