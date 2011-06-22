package org.sonatype.nexus.plugins.p2.repository.its.p2r01;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractP2MetadataGeneratorIT;

public class P2R0101DeployBundleIT
    extends AbstractP2MetadataGeneratorIT
{

    @Test
    public void test()
        throws Exception
    {
        createCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File p2Artifacts = downloadP2ArtifactsFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Artifacts has been downloaded", p2Artifacts, is( notNullValue() ) );
        assertThat( "p2Artifacts exists", p2Artifacts.exists(), is( true ) );

        final File p2Content = downloadP2ContentFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Content has been downloaded", p2Content, is( notNullValue() ) );
        assertThat( "p2Content exists", p2Content.exists(), is( true ) );
    }

}
