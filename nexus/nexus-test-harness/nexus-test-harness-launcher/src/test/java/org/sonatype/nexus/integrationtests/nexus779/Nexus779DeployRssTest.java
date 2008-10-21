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
