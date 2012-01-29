package org.sonatype.nexus.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspectorWrapper;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.Event;

public class DefaultEventInspectorHostTest
{
    @Test
    public void testSyncThenAsyncExecution()
        throws Exception
    {
        final InvocationTimestampEventInspector syncEI = new InvocationTimestampEventInspector();
        final InvocationTimestampEventInspector asyncEI = new InvocationTimestampEventInspector();

        final HashMap<String, EventInspector> map = new HashMap<String, EventInspector>( 2 );
        map.put( "sync", syncEI );
        map.put( "async", new AsynchronousEventInspectorWrapper( asyncEI ) );

        final DefaultEventInspectorHost host = new DefaultEventInspectorHost( map );

        host.onEvent( new NexusStartedEvent( this ) );

        // to handle possible async peculiarites
        syncEI.await();
        asyncEI.await();

        // they both should be invoked
        assertThat( syncEI.getInspectInvoked(), greaterThan( 0L ) );
        assertThat( asyncEI.getInspectInvoked(), greaterThan( 0L ) );

        // sync has to be invoked before async
        assertThat( asyncEI.getInspectInvoked(), greaterThan( syncEI.getInspectInvoked() ) );
        assertThat( asyncEI.getInspectInvoked() - syncEI.getInspectInvoked(), greaterThanOrEqualTo( 100L ) );
    }

    // ==

    public static class InvocationTimestampEventInspector
        implements EventInspector
    {
        private long inspectInvoked = -1;

        private CountDownLatch countDownLatch = new CountDownLatch( 1 );

        public long getInspectInvoked()
        {
            return inspectInvoked;
        }

        public void await()
            throws InterruptedException
        {
            countDownLatch.await();
        }

        @Override
        public boolean accepts( Event<?> evt )
        {
            return true;
        }

        @Override
        public void inspect( Event<?> evt )
        {
            try
            {
                this.inspectInvoked = System.currentTimeMillis();
                Thread.sleep( 100 );
            }
            catch ( InterruptedException e )
            {
                // nothing
            }
            finally
            {
                countDownLatch.countDown();
            }
        }
    }
}
