package org.sonatype.nexus.plugins.p2.repository.its.p2r03;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractP2GeneratorIT;

public class P2R0302P2RepositoryDeployBundleIT
    extends AbstractP2GeneratorIT
{

    /**
     * When a bundle is deployed p2Artifacts/p2Content gets created and added to the top generated p2 repository.
     */
    @Test
    public void test()
        throws Exception
    {
        createP2MetadataGeneratorCapability();
        createP2RepositoryGeneratorCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );
    }

}
