package org.sonatype.nexus.notification.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.notification.NotificationManager;
import org.sonatype.nexus.notification.NotificationRequest;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * A "bridge" that funnels events into notifications using the event to notification router.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "NotificationEventInspector" )
public class NotificationEventInspector
    extends AbstractEventInspector
{
    private static final String NOTIFICATION_ROUTE_KEY = "notificationRoute";

    @Requirement
    private NotificationEventRouter notificationEventRouter;

    @Requirement
    private NotificationManager notificationManager;

    public boolean accepts( Event<?> evt )
    {
        if ( !notificationManager.isEnabled() )
        {
            return false;
        }

        NotificationRequest route = notificationEventRouter.getRequestForEvent( evt );

        if ( route != null && !route.isEmpty() )
        {
            evt.getEventContext().put( NOTIFICATION_ROUTE_KEY, route );

            // yes, we have a route, we want to handle it
            return true;
        }
        else
        {
            // nah, no route to this one, forget it
            return false;
        }
    }

    public void inspect( Event<?> evt )
    {
        if ( !notificationManager.isEnabled() )
        {
            return;
        }

        NotificationRequest request = (NotificationRequest) evt.getEventContext().get( NOTIFICATION_ROUTE_KEY );

        // just a sanity check, eventInspectorHost should not call us in this case, see accepts() above
        if ( request != null && !request.isEmpty() )
        {
            notificationManager.notifyTargets( request );
        }
    }
}
