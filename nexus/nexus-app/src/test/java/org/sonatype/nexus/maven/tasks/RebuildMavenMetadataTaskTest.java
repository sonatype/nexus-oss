package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public class RebuildMavenMetadataTaskTest
    extends AbstractMavenTaskTests
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
