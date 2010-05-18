package org.sonatype.nexus.integrationtests.nexus977metadata;

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

public class Nexus977MavenMetadataGroupOfGroupsIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_release" );
        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( "RebuildMavenMetadata-release", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_release2" );
        task = TaskScheduleUtil.runTask( "RebuildMavenMetadata-release2", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_snapshot" );
        task = TaskScheduleUtil.runTask( "RebuildMavenMetadata-snapshot", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkMetadata()
        throws Exception
    {
        File metadataFile =
            downloadFile( new URL( nexusBaseUrl + "content/repositories/g4/"
                + "nexus977metadata/project/maven-metadata.xml" ), "target/downloads/nexus977" );

        final FileInputStream in = new FileInputStream( metadataFile );
        Metadata metadata = MetadataBuilder.read( in );
        IOUtil.close( in );

        List<String> versions = metadata.getVersioning().getVersions();
        Assert.assertThat( versions, IsCollectionContaining.hasItems( "1.5", "1.0.1", "1.0-SNAPSHOT", "0.8", "2.1" ) );
    }
}
