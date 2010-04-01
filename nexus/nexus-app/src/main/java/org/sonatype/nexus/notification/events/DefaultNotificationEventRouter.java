package org.sonatype.nexus.notification.events;

import java.util.HashSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.notification.NotificationCheat;
import org.sonatype.nexus.notification.NotificationManager;
import org.sonatype.nexus.notification.NotificationRequest;
import org.sonatype.nexus.notification.NotificationTarget;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.plexus.appevents.Event;

/**
 * This component routes based on Application event. Currently it is hardwired, but we would have some "mediation"
 * happen, and the best would be to have the "routing" of events to notifications saved in Nexus configuration. There
 * would be event-to-notificationGroup mapping, and routing should use that.
 * 
 * @author cstamas
 */
@Component( role = NotificationEventRouter.class )
public class DefaultNotificationEventRouter
    implements NotificationEventRouter
{
    @Requirement
    private NotificationManager notificationManager;

    public NotificationRequest getRequestForEvent( Event<?> evt )
    {
        if ( evt instanceof RepositoryEventProxyModeChanged )
        {
            // currently we "hardwire" this one only
            // later we should back this with real routing configuration
            HashSet<NotificationTarget> result = new HashSet<NotificationTarget>();

            NotificationTarget autoBlockTarget =
                notificationManager.readNotificationTarget( NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID );

            if ( autoBlockTarget != null )
            {
                result.add( autoBlockTarget );

                RepositoryEventProxyModeChangedMessage message =
                    new RepositoryEventProxyModeChangedMessage( (RepositoryEventProxyModeChanged) evt, null );

                return new NotificationRequest( message, result );
            }
            else
            {
                return NotificationRequest.EMPTY;
            }
        }
        else
        {
            return NotificationRequest.EMPTY;
        }
    }
}
