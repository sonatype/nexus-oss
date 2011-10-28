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
package org.sonatype.nexus.tasks;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.tasks.DeleteRepositoryFoldersTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Test if the repo folders(storage, indexer, proxy attributes) were deleted correctly
 *
 * @author juven
 */
public class DeleteRepositoryFoldersTaskTest
    extends AbstractMavenRepoContentTests
{
    @Test
    public void testTrashRepositoryFolders()
        throws Exception
    {
        fillInRepo();

        String repoId = snapshots.getId();

        DeleteRepositoryFoldersTask task = (DeleteRepositoryFoldersTask) lookup( SchedulerTask.class, DeleteRepositoryFoldersTask.class.getSimpleName() );
        task.setRepository( snapshots );
        task.setDeleteForever( false );

        task.call();

        File workDir = defaultNexus.getNexusConfiguration().getWorkingDirectory();
        File trashDir = new File( workDir, "trash" );

        assertFalse( new File( new File( workDir, "storage" ), repoId ).exists() );
        assertFalse( new File( new File( workDir, "indexer" ), repoId + "-local" ).exists() );
        assertFalse( new File( new File( workDir, "indexer" ), repoId + "-remote" ).exists() );
        assertFalse( new File( new File( new File( workDir, "proxy" ), "attributes" ), repoId ).exists() );

        assertTrue( new File( trashDir, repoId ).exists() );
        assertFalse( new File( trashDir, repoId + "-local" ).exists() );
        assertFalse( new File( trashDir, repoId + "-remote" ).exists() );
    }

    @Test
    public void testDeleteForeverRepositoryFolders()
        throws Exception
    {
        fillInRepo();

        String repoId = snapshots.getId();

        DeleteRepositoryFoldersTask task = (DeleteRepositoryFoldersTask) lookup( SchedulerTask.class, DeleteRepositoryFoldersTask.class.getSimpleName() );
        task.setRepository( snapshots );
        task.setDeleteForever( true );

        task.call();

        File workDir = defaultNexus.getNexusConfiguration().getWorkingDirectory();
        File trashDir = new File( workDir, "trash" );

        assertFalse( new File( new File( workDir, "storage" ), repoId ).exists() );
        assertFalse( new File( new File( workDir, "indexer" ), repoId + "-local" ).exists() );
        assertFalse( new File( new File( workDir, "indexer" ), repoId + "-remote" ).exists() );
        assertFalse( new File( new File( new File( workDir, "proxy" ), "attributes" ), repoId ).exists() );

        assertFalse( new File( trashDir, repoId ).exists() );
        assertFalse( new File( trashDir, repoId + "-local" ).exists() );
        assertFalse( new File( trashDir, repoId + "-remote" ).exists() );
    }
}
