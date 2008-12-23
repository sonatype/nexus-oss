package org.sonatype.nexus.plugins.migration.nxcm280;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;

public class NXCM280UploadArtifactsTest
    extends AbstractDeployBridgeTest
{

    @Test
    public void deployRelease()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.direct", "direct-deploy-released", "1.0", null, "jar", null, null, null, false, false,
                     null, false, null );
        deploy( gav, "test-releases-local", false );
    }

    @Test
    public void deploySnapshot()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.direct", "direct-deployed-snapshot", "1.0-SNAPSHOT", null, "jar", null, null, null, true,
                     false, null, false, null );
        deploy( gav, "test-snapshots-local", false );
    }

}
