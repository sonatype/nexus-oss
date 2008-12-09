package org.sonatype.nexus.integrationtests.proxy.nexus1089;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1089SecureProxyTest
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    public void startProxy()
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE, "secure" );
        server.start();
    }

    @Override
    public void stopProxy()
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE, "secure" );
        server.stop();
    }

    @Test
    public void downloadArtifact()
        throws Exception
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "nexus1089", "artifact", "1.0", "jar" );

        File artifact = this.downloadArtifact( "nexus1089", "artifact", "1.0", "jar", null, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );

    }
}
