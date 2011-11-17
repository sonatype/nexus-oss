/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4579;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * IT testing move to trash/delete immediately behavior of the SnapshotRemovalTask.
 *
 * @see org.sonatype.nexus.proxy.wastebasket.DeleteOperation
 */
public class Nexus4579OptionalTrashForSnapshotsIT
    extends AbstractNexusIntegrationTest
{

    protected File artifactFolder;

    protected File repositoryPath;

    private File trashPath;

    private String artifactRelativePath;

    private File trashFolder;

    public Nexus4579OptionalTrashForSnapshotsIT()
    {
        super( "nexus-test-harness-snapshot-repo" );
    }

    @BeforeMethod(alwaysRun = true)
    public void deploySnapshotArtifacts()
        throws Exception
    {
        repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-snapshot-repo" );
        trashPath = new File( repositoryPath, ".nexus/trash" );
        artifactRelativePath = "nexus4579/artifact/1.0-SNAPSHOT";

        artifactFolder = new File( repositoryPath, artifactRelativePath );
        trashFolder = new File( trashPath, artifactRelativePath );

        File oldSnapshot = getTestFile( "repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        RepositoryMessageUtil.updateIndexes( getTestRepositoryId() );

        TaskScheduleUtil.waitForAllTasksToStop();
        new EventInspectorsUtil( this ).waitForCalmPeriod();
    }

    @Test
    public void removeAllSnapshotsToTrash()
        throws Exception
    {
        runSnapshotRemover( "move-to-trash", getTestRepositoryId(), 0, 0, true, false );

        assertThat( "artifact folder was not removed", artifactFolder.list(), nullValue() );
        assertThat( "removed snapshots did not go into trash", trashFolder.isDirectory(), is( true ) );
        Collection<?> jars = FileUtils.listFiles( trashFolder, new String[]{ "jar" }, false );
        assertThat( "removed snapshots did not go into trash", jars, hasSize(1) );
    }

    @Test
    public void removeAllSnapshotsDirectly()
        throws Exception
    {
        runSnapshotRemover( "remove-directly", getTestRepositoryId(), 0, 0, true, true );

        assertThat( artifactFolder.list(), nullValue() );
        assertThat( "removed snapshots did go into trash", trashFolder.list(), nullValue() );
    }

    protected void runSnapshotRemover( String name, String repositoryId, int minSnapshotsToKeep, int removeOlderThanDays,
                                       boolean removeIfReleaseExists, boolean deleteImmediately )
        throws Exception
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setKey( SnapshotRemovalTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        repositoryProp.setValue( repositoryId );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setKey( SnapshotRemovalTaskDescriptor.MIN_TO_KEEP_FIELD_ID );
        keepSnapshotsProp.setValue( String.valueOf( minSnapshotsToKeep ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setKey( SnapshotRemovalTaskDescriptor.KEEP_DAYS_FIELD_ID);
        ageProp.setValue( String.valueOf( removeOlderThanDays ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setKey( SnapshotRemovalTaskDescriptor.REMOVE_WHEN_RELEASED_FIELD_ID );
        removeReleasedProp.setValue( Boolean.toString( removeIfReleaseExists ) );

        ScheduledServicePropertyResource deleteImmediatelyProp = new ScheduledServicePropertyResource();
        deleteImmediatelyProp.setKey( SnapshotRemovalTaskDescriptor.DELETE_IMMEDIATELY );
        deleteImmediatelyProp.setValue( Boolean.toString( deleteImmediately ) );

        TaskScheduleUtil.runTask( name, SnapshotRemovalTaskDescriptor.ID, repositoryProp,
                                  keepSnapshotsProp, ageProp,
                                  removeReleasedProp, deleteImmediatelyProp );

    }
}
