package org.sonatype.nexus.plugins.migration.nxcm280;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;

public class NXCM280UploadToMixedRepositoryTest
    extends AbstractDeployBridgeTest
{

    @Test
    public void deployReleaseMixed()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.direct", "direct-mixed-released", "1.0", null, "jar", null, null, null, false, false,
                     null, false, null );
        deploy( gav, "main-local", false, "main-local-releases" );
    }

    @Test
    public void deploySnapshotMixed()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.direct", "direct-mixed-snapshot", "1.0-SNAPSHOT", null, "jar", null, null, null, true,
                     false, null, false, null );
        deploy( gav, "main-local", false, "main-local-snapshots" );
    }

}
