package org.sonatype.nexus.plugins.p2.repository.its.p2r01;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;

public class P2R0101DeployBundleIT
    extends AbstractNexusP2GeneratorIT
{

    public P2R0101DeployBundleIT()
    {
        super( "p2r01" );
    }

    /**
     * When an OSGi bundle is deployed pArtifacts && p2Content are created.
     */
    @Test
    public void test()
        throws Exception
    {
        createP2MetadataGeneratorCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File p2Artifacts = downloadP2ArtifactsFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Artifacts has been downloaded", p2Artifacts, is( notNullValue() ) );
        assertThat( "p2Artifacts exists", p2Artifacts.exists(), is( true ) );
        // TODO compare downloaded file with an expected one

        final File p2Content = downloadP2ContentFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Content has been downloaded", p2Content, is( notNullValue() ) );
        assertThat( "p2Content exists", p2Content.exists(), is( true ) );
        // TODO compare downloaded file with an expected one
    }

}
