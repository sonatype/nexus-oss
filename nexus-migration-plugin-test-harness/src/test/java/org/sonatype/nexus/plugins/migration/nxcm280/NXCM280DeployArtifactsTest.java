package org.sonatype.nexus.plugins.migration.nxcm280;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;

public class NXCM280DeployArtifactsTest
    extends AbstractDeployBridgeTest
{

    @Test
    public void mavenDeployRelease()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-deploy-released", "1.0", null, "jar", null, null, null, false, false,
                     null, false, null );
        deploy( gav, "test-releases-local", true );
    }

    @Test
    public void mavenDeploySnapshot()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-deployed-snapshot", "1.0-SNAPSHOT", null, "jar", null, null, null, true,
                     false, null, false, null );
        deploy( gav, "test-snapshots-local", false, 1 );
    }

}
