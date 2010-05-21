package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus977GroupOfGroupsExpireCacheTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void expireCache()
        throws Exception
    {
        Gav gav = GavUtil.newGav( getTestId(), "project", "1.0" );
        failDownload( gav );

        File dest = new File( localStorageDir, "nexus977tasks/1/nexus977tasks/project/1.0/project-1.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );
        failDownload( gav );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( "ExpireCacheTaskDescriptor-snapshot", ExpireCacheTaskDescriptor.ID, repo );
        TaskScheduleUtil.waitForAllTasksToStop();
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        downloadArtifactFromRepository( "g4", gav, "target/downloads/nexus977tasks" );
    }

    private void failDownload( Gav gav )
        throws IOException
    {
        try
        {
            downloadArtifactFromRepository( "g4", gav, "target/downloads/nexus977tasks" );
            Assert.fail( "snapshot removal should have deleted this" );
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertThat( e.getMessage(), StringContains.containsString( "404" ) );
        }
    }
}
