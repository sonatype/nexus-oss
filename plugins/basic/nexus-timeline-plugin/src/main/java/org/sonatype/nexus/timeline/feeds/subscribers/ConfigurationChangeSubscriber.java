/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.timeline.feeds.subscribers;

import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class ConfigurationChangeSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public ConfigurationChangeSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final ConfigurationChangeEvent event) {
    if (event.getChanges().isEmpty()) {
      return;
    }
    // keep list unique, one component might be reported multiple times
    final HashSet<String> changes = Sets.newHashSet();
    for (Configurable changed : event.getChanges()) {
      changes.add(changed.getName());
    }
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "changes", changes.toString());
    putIfNotNull(data, "user.id", event.getUserId());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_SYSTEM,
        FeedRecorder.SYSTEM_CONFIG,
        event.getClass().getName(),
        event.getEventDate(),
        event.getUserId(),
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }
}
