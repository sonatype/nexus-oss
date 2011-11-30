package org.sonatype.nexus.timeline;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginDeactivatedEvent;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * TODO
 *
 * @author: cstamas
 */
@Component( role = EventInspector.class, hint = "TimelinePluginEventInspector" )
public class TimelinePluginEventInspector
    extends AbstractEventInspector
    implements EventInspector
{

    @Requirement
    private NexusTimeline nexusTimeline;

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return evt instanceof NexusInitializedEvent;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( nexusTimeline instanceof RedirectingTimeline )
        {
            ( (RedirectingTimeline) nexusTimeline ).tryToActivateTimeline();
        }
    }
}
