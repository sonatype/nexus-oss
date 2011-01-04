/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
