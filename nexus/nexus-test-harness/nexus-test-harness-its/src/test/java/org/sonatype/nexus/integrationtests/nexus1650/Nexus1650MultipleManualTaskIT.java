/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1650;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    @BeforeMethod
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
        repositoryProp.setKey( "repositoryId" );
        repositoryProp.setValue( "nexus-test-harness-snapshot-repo" );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setKey( "minSnapshotsToKeep" );
        keepSnapshotsProp.setValue( String.valueOf( 0 ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setKey( "removeOlderThanDays" );
        ageProp.setValue( String.valueOf( 0 ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setKey( "removeIfReleaseExists" );
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
        TaskScheduleUtil.waitForAllTasksToStop();

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
