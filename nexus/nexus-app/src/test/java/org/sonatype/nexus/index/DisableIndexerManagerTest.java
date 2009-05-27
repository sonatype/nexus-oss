package org.sonatype.nexus.index;

import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.scheduling.ScheduledTask;

public class DisableIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testDisableIndex()
        throws Exception
    {
        fillInRepo();

        ReindexTask reindexTask = nexusScheduler.createTaskInstance( ReindexTask.class );

        ScheduledTask<Object> st = nexusScheduler.submit( "reindexAll", reindexTask );

        // make it block until finished
        st.get();

        searchFor( "org.sonatype.plexus", 1 );

        snapshots.setIndexable( false );

        nexusConfiguration.saveConfiguration();

        searchFor( "org.sonatype.plexus", 0 );
    }
}
