package org.sonatype.nexus.proxy.maven.metadata.operations;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility.Version;

public class ModelVersionUtilityTest
{

    @Test
    public void testEmptyMetadataGetVersion()
    {
        Metadata metadata = new Metadata();

        assertThat( ModelVersionUtility.getModelVersion( metadata ), is( Version.V100 ) );

    }

    @Test
    public void testEmptyMetadataSetV110()
    {
        Metadata metadata = new Metadata();
        ModelVersionUtility.setModelVersion( metadata, Version.V110 );
        assertThat( ModelVersionUtility.getModelVersion( metadata ), is( Version.V110 ) );
    }

    @Test
    public void testEmptyMetadataSetV100()
    {
        Metadata metadata = new Metadata();
        ModelVersionUtility.setModelVersion( metadata, Version.V100 );
        assertThat( ModelVersionUtility.getModelVersion( metadata ), is( Version.V100 ) );
    }

}
