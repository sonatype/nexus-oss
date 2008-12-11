/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public class RebuildMavenMetadataTaskTest
    extends AbstractMavenRepoContentTests
{
    protected NexusScheduler nexusScheduler;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = (NexusScheduler) lookup( NexusScheduler.class );

        nexusScheduler.startService();
    }

    protected void tearDown()
        throws Exception
    {
        nexusScheduler.stopService();

        super.tearDown();
    }

    public void testOneRun()
        throws Exception
    {
        fillInRepo();

        RebuildMavenMetadataTask task = (RebuildMavenMetadataTask) nexusScheduler
            .createTaskInstance( RebuildMavenMetadataTask.class );

        task.setRepositoryId( snapshots.getId() );

        ScheduledTask<Object> handle = nexusScheduler.submit( "task", task );

        // block until it finishes
        handle.getIfDone();
    }
}
