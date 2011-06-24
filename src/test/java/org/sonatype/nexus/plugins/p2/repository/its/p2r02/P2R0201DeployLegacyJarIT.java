package org.sonatype.nexus.plugins.p2.repository.its.p2r02;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractP2GeneratorIT;

public class P2R0201DeployLegacyJarIT
    extends AbstractP2GeneratorIT
{

    /**
     * When deploying a legacy jar (non OSGi bundle), p2Artifacts & p2Content are not created.
     */
    @Test
    public void test()
        throws Exception
    {
        createP2MetadataGeneratorCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File p2Artifacts = storageP2ArtifactsFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "p2Artifacts does not exist", p2Artifacts.exists(), is( false ) );

        final File p2Content = storageP2ContentFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "p2Content does not exist", p2Content.exists(), is( false ) );
    }

}
