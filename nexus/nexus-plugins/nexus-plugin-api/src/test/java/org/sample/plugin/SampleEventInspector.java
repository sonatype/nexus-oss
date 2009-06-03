package org.sample.plugin;

import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

public class SampleEventInspector
    implements EventInspector
{
    public boolean accepts( Event<?> evt )
    {
        return true;
    }

    public void inspect( Event<?> evt )
    {
        System.out.println( "invoked with event: " + evt.toString() + " with sender " + evt.getEventSender().toString() );
    }
}
