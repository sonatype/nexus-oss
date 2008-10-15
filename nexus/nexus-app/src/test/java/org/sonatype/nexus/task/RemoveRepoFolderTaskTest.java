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
