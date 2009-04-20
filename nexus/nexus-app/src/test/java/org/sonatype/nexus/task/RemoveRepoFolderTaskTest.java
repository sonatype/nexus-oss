/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.task;

import java.io.File;

import org.sonatype.nexus.AbstractMavenRepoContentTests;

/**
 * Test if the repo folders(storage, indexer, proxy attributes) were deleted correctly
 * 
 * @author juven
 */
public class RemoveRepoFolderTaskTest
    extends AbstractMavenRepoContentTests
{
    public void testRemoveRepoFolder()
        throws Exception
    {
        fillInRepo();

        String repoId = snapshots.getId();

        defaultNexus.removeRepositoryFolder( snapshots );

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
}
