/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.notification.events;

import org.sonatype.nexus.notification.NotificationManager;
import org.sonatype.nexus.notification.NotificationRequest;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * A "bridge" that funnels events into notifications using the event to notification router.
 *
 * @author cstamas
 */
@Component(role = EventInspector.class, hint = "NotificationEventInspector")
public class NotificationEventInspector
    extends AbstractEventInspector
{
  private static final String NOTIFICATION_ROUTE_KEY = "notificationRoute";

  @Requirement
  private NotificationEventRouter notificationEventRouter;

  @Requirement
  private NotificationManager notificationManager;

  public boolean accepts(Event<?> evt) {
    if (!notificationManager.isEnabled()) {
      return false;
    }

    NotificationRequest route = notificationEventRouter.getRequestForEvent(evt);

    if (route != null && !route.isEmpty()) {
      evt.getEventContext().put(NOTIFICATION_ROUTE_KEY, route);

      // yes, we have a route, we want to handle it
      return true;
    }
    else {
      // nah, no route to this one, forget it
      return false;
    }
  }

  public void inspect(Event<?> evt) {
    if (!notificationManager.isEnabled()) {
      return;
    }

    NotificationRequest request = (NotificationRequest) evt.getEventContext().get(NOTIFICATION_ROUTE_KEY);

    // just a sanity check, eventInspectorHost should not call us in this case, see accepts() above
    if (request != null && !request.isEmpty()) {
      notificationManager.notifyTargets(request);
    }
  }
}
