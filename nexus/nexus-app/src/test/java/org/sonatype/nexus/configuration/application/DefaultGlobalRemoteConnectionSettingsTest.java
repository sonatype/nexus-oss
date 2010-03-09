package org.sonatype.nexus.configuration.application;

import junit.framework.Assert;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteConnectionEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

public class DefaultGlobalRemoteConnectionSettingsTest
    extends AbstractNexusTestCase
{

    @SuppressWarnings( "unchecked" )
    public void testEvents()
        throws Exception
    {
        NexusConfiguration cfg = lookup( NexusConfiguration.class );
        cfg.loadConfiguration();

        final Event<GlobalRemoteConnectionSettings>[] event = new Event[1];
        ApplicationEventMulticaster applicationEventMulticaster = lookup( ApplicationEventMulticaster.class );
        applicationEventMulticaster.addEventListener( new EventListener()
        {
            public void onEvent( Event<?> evt )
            {
                if ( evt instanceof GlobalRemoteConnectionEvent )
                {
                    event[0] = (GlobalRemoteConnectionEvent) evt;
                }
            }
        } );

        GlobalRemoteConnectionSettings settings = lookup( GlobalRemoteConnectionSettings.class );

        settings.setConnectionTimeout( 2 );
        settings.setRetrievalRetryCount( 3 );

        cfg.saveConfiguration();

        Assert.assertNotNull( event[0].getEventSender() );
        Assert.assertEquals( settings, event[0].getEventSender() );
        Assert.assertEquals( 2, event[0].getEventSender().getConnectionTimeout() );
        Assert.assertEquals( 3, event[0].getEventSender().getRetrievalRetryCount() );

    }
}
