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
package org.sonatype.nexus.task;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.RemoveRepoFolderTask;
import org.sonatype.nexus.tasks.descriptors.RemoveRepoFolderTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

public class RemoveRepoFolderTaskTest extends AbstractNexusTestCase {
	
	private NexusScheduler nexusScheduler;

	protected boolean loadConfigurationAtSetUp() {
		// IT IS NEEDED FROM NOW ON!
		return true;
	}

	protected void setUp() throws Exception {
		super.setUp();

		nexusScheduler = (NexusScheduler) lookup(NexusScheduler.class);

		nexusScheduler.startService();
	}

	protected void tearDown() throws Exception {
		
		nexusScheduler.stopService();

		super.tearDown();
	}
	
	public void testRemoveRepoFolder() throws Exception
	{
		RemoveRepoFolderTask task = (RemoveRepoFolderTask)lookup(SchedulerTask.class, RemoveRepoFolderTaskDescriptor.ID);
	
		nexusScheduler.submit( "task", task );
	}
}
