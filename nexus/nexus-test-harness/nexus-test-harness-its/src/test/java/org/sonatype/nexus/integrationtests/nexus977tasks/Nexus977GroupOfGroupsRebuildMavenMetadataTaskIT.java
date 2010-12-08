package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsRebuildMavenMetadataTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkMetadata()
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        TaskScheduleUtil.runTask( "RebuildMavenMetadata-snapshot", RebuildMavenMetadataTaskDescriptor.ID, repo );

        File metadataFile =
            downloadFile( new URL( nexusBaseUrl + "content/repositories/g4/"
                + "nexus977tasks/project/maven-metadata.xml" ), "target/downloads/nexus977" );

        final FileInputStream in = new FileInputStream( metadataFile );
        Metadata metadata = MetadataBuilder.read( in );
        IOUtil.close( in );

        List<String> versions = metadata.getVersioning().getVersions();
        MatcherAssert.assertThat( versions, IsCollectionContaining.hasItems( "1.5", "1.0.1", "1.0-SNAPSHOT", "0.8", "2.1" ) );
    }
}
