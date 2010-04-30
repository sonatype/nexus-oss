package org.sonatype.nexus.integrationtests.nexus1650;

import java.io.File;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1650MultipleManualTaskIT
    extends AbstractNexusIntegrationTest
{
    @SuppressWarnings( "unchecked" )
    public static Collection<File> listFiles( File directory, String[] extensions, boolean recursive )
    {
        return FileUtils.listFiles( directory, extensions, recursive );
    }

    protected File artifactFolder;

    protected File repositoryPath;

    public Nexus1650MultipleManualTaskIT()
    {
        super( "nexus-test-harness-snapshot-repo" );
    }

    @Before
    public void deploySnapshotArtifacts()
        throws Exception
    {
        initFolders();

        File oldSnapshot = getTestFile( "repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        RepositoryMessageUtil.updateIndexes( "nexus-test-harness-snapshot-repo" );
    }

    public void initFolders()
        throws Exception
    {
        repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-snapshot-repo" );
        artifactFolder = new File( repositoryPath, "nexus634/artifact/1.0-SNAPSHOT" );
    }

    protected void createSnapshotTask( String name )
        throws Exception
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setId( "repositoryOrGroupId" );
        repositoryProp.setValue( "nexus-test-harness-snapshot-repo" );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setId( "minSnapshotsToKeep" );
        keepSnapshotsProp.setValue( String.valueOf( 0 ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setId( "removeOlderThanDays" );
        ageProp.setValue( String.valueOf( 0 ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setId( "removeIfReleaseExists" );
        removeReleasedProp.setValue( String.valueOf( true ) );

        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( name );
        scheduledTask.setTypeId( SnapshotRemovalTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( repositoryProp );
        scheduledTask.addProperty( keepSnapshotsProp );
        scheduledTask.addProperty( ageProp );
        scheduledTask.addProperty( removeReleasedProp );

        Status status = TaskScheduleUtil.create( scheduledTask );

        Assert.assertTrue( status.isSuccess() );
    }

    @Test
    public void testMultipleManualInstances()
        throws Exception
    {
        TaskScheduleUtil.waitForTasks();

        createSnapshotTask( "Nexus1650Task1" );
        createSnapshotTask( "Nexus1650Task2" );
        createSnapshotTask( "Nexus1650Task3" );

        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();

        Assert.assertEquals( 3, tasks.size() );

        for ( ScheduledServiceListResource resource : tasks )
        {
            TaskScheduleUtil.run( resource.getId() );
        }

        tasks = TaskScheduleUtil.getTasks();

        for ( ScheduledServiceListResource resource : tasks )
        {
            TaskScheduleUtil.run( resource.getId() );
        }

        Thread.sleep( 200 );

        Assert.assertTrue( isAtLeastOneSleeping() );

        waitForTasksToComplete();
    }

    private boolean isAtLeastOneSleeping()
        throws Exception
    {
        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();

        for ( ScheduledServiceListResource resource : tasks )
        {
            if ( resource.getStatus().equals( "SLEEPING" ) )
            {
                return true;
            }
        }

        return false;
    }

    private void waitForTasksToComplete()
        throws Exception
    {
        // Wait 1 full second between checks
        long sleep = 1000;

        Thread.sleep( 500 ); // give an time to task start

        boolean allDone = false;

        for ( int attempt = 0; attempt < 300; attempt++ )
        {
            Thread.sleep( sleep );

            List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();

            for ( ScheduledServiceListResource task : tasks )
            {
                log.info( "Task: " + task.getName() + ", Attempt: " + attempt + ", LastRunResult: "
                    + task.getLastRunResult() + ", Status: " + task.getStatus() );
                if ( !task.getStatus().equals( "SUBMITTED" ) )
                {
                    allDone = false;
                    break;
                }
                else
                {
                    allDone = true;
                }
            }

            if ( allDone )
            {
                break;
            }
        }
    }
}
