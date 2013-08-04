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

package org.sonatype.nexus.feeds.record;

import java.util.HashSet;

import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;

/**
 * @author Juven Xu
 */
@Component(role = EventInspector.class, hint = "ConfigurationChangeEvent")
public class ConfigurationChangeEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{

  public boolean accepts(Event<?> evt) {
    return (evt instanceof ConfigurationChangeEvent);
  }

  public void inspect(Event<?> evt) {
    inspectForNexus(evt);
  }

  private void inspectForNexus(Event<?> evt) {
    ConfigurationChangeEvent event = (ConfigurationChangeEvent) evt;

    if (event.getChanges().isEmpty()) {
      return;
    }

    StringBuilder msg = new StringBuilder();

    msg.append("Nexus server configuration was changed: ");

    // keep list unique, one component might be reported multiple times
    final HashSet<String> changes = new HashSet<String>();
    for (Configurable changed : event.getChanges()) {
      changes.add(changed.getName());
    }
    msg.append(changes.toString());

    if (event.getUserId() != null) {
      msg.append(", change was made by [" + event.getUserId() + "]");
    }

    getFeedRecorder().addSystemEvent(FeedRecorder.SYSTEM_CONFIG_ACTION, msg.toString());
  }
}
