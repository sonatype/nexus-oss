package org.sonatype.nexus.maven.tasks;

import java.lang.reflect.Method;

import org.junit.Test;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.scheduling.CancellableProgressListenerWrapper;
import org.sonatype.scheduling.ProgressListener;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskUtil;

/**
 * TODO
 *
 * @author: cstamas
 */
public class Nexus4588CancellationTest
    extends AbstractMavenRepoContentTests
{

    @Test( expected = TaskInterruptedException.class )
    public void testNexus4588()
        throws Exception
    {
        fillInRepo();

        setUpProgressListener();

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), 1, 10, true );

        TaskUtil.getCurrentProgressListener().cancel();

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
    }

    @Test( expected = TaskInterruptedException.class )
    public void testNexus4588After1stWalk()
        throws Exception
    {
        fillInRepo();

        setUpProgressListener();

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), 1, 10, true );

        // activate the molester
        ( (Nexus4588CancellationEventInspector) lookup( EventInspector.class, "nexus4588" ) ).setActive( true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
    }

    public static void setUpProgressListener()
        throws Exception
    {
        Method setCurrentMethod = TaskUtil.class.getDeclaredMethod( "setCurrent", ProgressListener.class );

        setCurrentMethod.setAccessible( true );

        setCurrentMethod.invoke( null, new CancellableProgressListenerWrapper( null ) );
    }
}
