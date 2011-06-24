package org.sonatype.nexus.plugins.p2.repository.its.p2r03;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractP2GeneratorIT;

public class P2R0301P2RepositoryCapabilityIT
    extends AbstractP2GeneratorIT
{

    /**
     * When p2 repository generator capability is created p2 repository is created. When removed p2 repository gets
     * deleted.
     */
    @Test
    public void test()
        throws Exception
    {
        final File artifactsXML = storageP2RepositoryArtifactsXML();
        final File contentXML = storageP2RepositoryContentXML();

        createP2RepositoryGeneratorCapability();

        assertThat( "P2 artifacts.xml does exist", artifactsXML.exists(), is( true ) );
        assertThat( "P2 content.xml does exist", contentXML.exists(), is( true ) );

        removeP2RepositoryGeneratorCapability();

        assertThat( "P2 artifacts.xml does not exist", artifactsXML.exists(), is( false ) );
        assertThat( "P2 content.xml does not exist", contentXML.exists(), is( false ) );
    }

}
