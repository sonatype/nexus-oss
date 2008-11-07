package org.sonatype.nexus.integrationtests.webproxy.nexus1101;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1101NexusOverWebproxyTest
    extends AbstractNexusWebProxyIntegrationTest
{

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "downloadArtifactOverWebProxy" );
            return;
        }

        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus1101", "artifact", "1.0", "pom" );
        File pomArtifact = this.downloadArtifact( "nexus1101", "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus1101", "artifact", "1.0", "jar" );
        File jarArtifact = this.downloadArtifact( "nexus1101", "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );
    }

}
