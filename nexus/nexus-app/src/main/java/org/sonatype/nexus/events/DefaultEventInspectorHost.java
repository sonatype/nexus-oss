package org.sonatype.nexus.events;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it.
 * 
 * @author cstamas
 */
@Component( role = EventInspectorHost.class )
public class DefaultEventInspectorHost
    extends AbstractLogEnabled
    implements EventInspectorHost
{
    @Requirement( role = EventInspector.class )
    private Map<String, EventInspector> eventInspectors;

    public void processEvent( AbstractEvent evt )
    {
        for ( Map.Entry<String, EventInspector> entry : eventInspectors.entrySet() )
        {
            EventInspector ei = entry.getValue();

            try
            {
                if ( ei.accepts( evt ) )
                {
                    ei.inspect( evt );
                }
            }
            catch ( Throwable e )
            {
                getLogger().warn(
                    "EventInspector hinted='" + entry.getKey() + "' class='" + ei.getClass().getName()
                        + "' had problem inspecting an event='" + evt.getClass() + "'",
                    e );
            }
        }
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        processEvent( evt );
    }

}
