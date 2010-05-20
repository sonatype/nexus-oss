package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus977GroupOfGroupsRebuildMavenMetadataTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkMetadata()
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( "RebuildMavenMetadata-snapshot", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        File metadataFile =
            downloadFile( new URL( nexusBaseUrl + "content/repositories/g4/"
                + "nexus977tasks/project/maven-metadata.xml" ), "target/downloads/nexus977" );

        final FileInputStream in = new FileInputStream( metadataFile );
        Metadata metadata = MetadataBuilder.read( in );
        IOUtil.close( in );

        List<String> versions = metadata.getVersioning().getVersions();
        Assert.assertThat( versions, IsCollectionContaining.hasItems( "1.5", "1.0.1", "1.0-SNAPSHOT", "0.8", "2.1" ) );
    }
}
