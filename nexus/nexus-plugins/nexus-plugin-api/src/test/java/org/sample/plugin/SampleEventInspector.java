package org.sample.plugin;

import javax.inject.Inject;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

public class SampleEventInspector
    implements EventInspector
{
    @Inject
    private Logger logger;

    public boolean accepts( Event<?> evt )
    {
        return true;
    }

    public void inspect( Event<?> evt )
    {
        logger.info( "invoked with event: " + evt.toString() + " with sender " + evt.getEventSender().toString() );
    }
}
