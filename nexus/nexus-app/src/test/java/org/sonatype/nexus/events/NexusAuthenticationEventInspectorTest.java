package org.sonatype.nexus.events;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.auth.AuthenticationItem;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

public class NexusAuthenticationEventInspectorTest
    extends PlexusTestCaseSupport
{
    @Before
    public void manglePlexus()
        throws Exception
    {
        final ComponentDescriptor<DummyFeedRecorder> fakeFeedRecorder =
            new ComponentDescriptor<DummyFeedRecorder>( DummyFeedRecorder.class, getContainer().getLookupRealm() );
        fakeFeedRecorder.setRoleClass( FeedRecorder.class );

        getContainer().addComponentDescriptor( fakeFeedRecorder );
    }

    @Test
    public void testSimple()
        throws Exception
    {
        final DummyFeedRecorder feedRecorder = (DummyFeedRecorder) lookup( FeedRecorder.class );

        final NexusAuthenticationEventInspector naei =
            (NexusAuthenticationEventInspector) lookup( EventInspector.class,
                NexusAuthenticationEventInspector.class.getSimpleName() );

        final AuthenticationItem authSuccess = new AuthenticationItem( "test", "192.168.0.1", "Foo/Bar", true );
        final AuthenticationItem authFailed = new AuthenticationItem( "test", "192.168.0.1", "Foo/Bar", false );

        NexusAuthenticationEvent naeSuccess = new NexusAuthenticationEvent( this, authSuccess );
        NexusAuthenticationEvent naeFailed = new NexusAuthenticationEvent( this, authFailed );

        // we send same event 5 times, but only one of them should be recorded since the rest 4 are "similar" and within
        // 2 sec
        for ( int i = 0; i < 5; i++ )
        {
            naei.inspect( naeSuccess );
        }
        // we send another event 5 times, but only one of them should be recorded since it is not "similar" to previous
        // sent ones, but the rest 4 are "similar" and within 2 sec
        for ( int i = 0; i < 5; i++ )
        {
            naei.inspect( naeFailed );
        }
        // we sleep a bit over two seconds
        Thread.sleep( 2001L );
        // and we send again the second event, but this one should be recorded, since the gap between last sent and this is more than 2 seconds
        naei.inspect( naeFailed );

        // total 11 events "fired", but 3 recorded due to "similarity filtering"
        MatcherAssert.assertThat( feedRecorder.getReceivedEventCount(), CoreMatchers.equalTo( 3 ) );
    }
}
