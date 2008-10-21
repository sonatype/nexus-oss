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
        deploy( "artifact1" );
        deploy( "artifact2" );
        feedListContainsArtifact( "nexus779", "artifact1", "1.0" );

        deploy( "artifact3" );
        feedListContainsArtifact( "nexus779", "artifact3", "1.0" );

        deploy( "artifact4" );
        feedListContainsArtifact( "nexus779", "artifact4", "1.0" );
        // I wanna to delay this check
        feedListContainsArtifact( "nexus779", "artifact2", "1.0" );
    }

    private int deploy( String artifactName )
        throws Exception
    {
        File jarFile = getTestFile( artifactName + ".jar" );
        File pomFile = getTestFile( artifactName + ".pom" );

        int status = DeployUtils.deployUsingPomWithRest( REPO_TEST_HARNESS_REPO, jarFile, pomFile, "", "jar" );
        return status;
    }

}
