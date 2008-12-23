package org.sonatype.nexus.plugins.migration.nxcm280;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;

public class NXCM280DeployToMixedRepositoryTest
    extends AbstractDeployBridgeTest
{

    @Test
    public void mavenDeployReleaseMixed()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-mixed-released", "1.0", null, "jar", null, null, null, false, false, null,
                     false, null );
        deploy( gav, "main-local", true, "main-local-releases" );
    }

    @Test
    public void mavenDeploySnapshotMixed()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-mixed-snapshot", "1.0-SNAPSHOT", null, "jar", null, null, null, true,
                     false, null, false, null );
        deploy( gav, "main-local", true, 9, "main-local-snapshots" );
    }

}
