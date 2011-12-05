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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.record.NexusAuthenticationEventInspector;
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

        final ComponentDescriptor<DummyNexusConfiguration> fakeNexusConfiguration =
            new ComponentDescriptor<DummyNexusConfiguration>( DummyNexusConfiguration.class,
                getContainer().getLookupRealm() );
        fakeNexusConfiguration.setRoleClass( NexusConfiguration.class );

        getContainer().addComponentDescriptor( fakeFeedRecorder );
        getContainer().addComponentDescriptor( fakeNexusConfiguration );
    }

    public void perform( final String username, final int expected )
        throws Exception
    {
        final DummyFeedRecorder feedRecorder = (DummyFeedRecorder) lookup( FeedRecorder.class );

        final NexusAuthenticationEventInspector naei =
            (NexusAuthenticationEventInspector) lookup( EventInspector.class,
                NexusAuthenticationEventInspector.class.getSimpleName() );

        final ClientInfo authSuccess = new ClientInfo( username, "192.168.0.1", "Foo/Bar" );
        final ClientInfo authFailed = new ClientInfo( username, "192.168.0.1", "Foo/Bar" );

        NexusAuthenticationEvent naeSuccess = new NexusAuthenticationEvent( this, authSuccess, true );
        NexusAuthenticationEvent naeFailed = new NexusAuthenticationEvent( this, authFailed, false );

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
        Thread.sleep( 2100L );
        // and we send again the second event, but this one should be recorded, since the gap between last sent and this
        // is more than 2 seconds
        naei.inspect( naeFailed );

        // total 11 events "fired", but 3 recorded due to "similarity filtering"
        MatcherAssert.assertThat( feedRecorder.getReceivedEventCount(), CoreMatchers.equalTo( expected ) );
    }

    @Test
    public void testNonAnon()
        throws Exception
    {
        perform( "test", 3 );
    }

    @Test
    public void testAnon()
        throws Exception
    {
        perform( "anonymous", 0 );
    }
}
