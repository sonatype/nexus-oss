package org.sonatype.nexus.notification.events;

import org.sonatype.nexus.notification.NotificationRequest;
import org.sonatype.plexus.appevents.Event;

/**
 * Routes that calculates the proper notification targets to be notified in case of some event.
 * 
 * @author cstamas
 */
public interface NotificationEventRouter
{
    /**
     * Returns the set of targets to be notified for the event.
     * 
     * @param evt
     * @return
     */
    NotificationRequest getRequestForEvent( Event<?> evt );
}
