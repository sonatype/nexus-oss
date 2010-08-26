#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VirusScannerIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void download()
        throws Exception
    {
        File file =
            downloadArtifactFromRepository( "release-proxy-repo-1", GavUtil.newGav( "kungfu", "artifact", "1.0" ),
                                            "target/download" );

        Assert.assertTrue( file.exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/release-proxy-repo-1/kungfu/artifact/1.0/artifact-1.0.jar" ).exists() );
    }

    @Test
    public void downloadInfected()
        throws Exception
    {
        File file =
            downloadArtifactFromRepository( "release-proxy-repo-1", GavUtil.newGav( "kungfu", "infected-artifact",
                                                                                    "1.0" ), "target/download" );

        Assert.assertTrue( file.exists() );
        Assert.assertFalse( new File( nexusWorkDir,
                                      "storage/release-proxy-repo-1/kungfu/infected-artifact/1.0/infected-artifact-1.0.jar" ).exists() );
    }
}
