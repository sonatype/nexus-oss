/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.NexusStartedEvent;
import org.sonatype.nexus.common.app.NexusStoppedEvent;
import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subscriber listening for events recorded under {@link FeedRecorder#FAMILY_SYSTEM} event type.
 */
@Named
@Singleton
public class SystemSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber
{
  private final Provider<SystemStatus> systemStatusProvider;

  @Inject
  public SystemSubscriber(final FeedRecorder feedRecorder,
                          final Provider<SystemStatus> systemStatusProvider)
  {
    super(feedRecorder);
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
  }

  @Subscribe
  public void on(final NexusStartedEvent e) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "bootAction", "started");
    putIfNotNull(data, "nxVersion", systemStatusProvider.get().getVersion());
    putIfNotNull(data, "nxEdition", systemStatusProvider.get().getEditionShort());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_SYSTEM,
        FeedRecorder.SYSTEM_BOOT,
        e.getEventDate(),
        null, // "system" is booting
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Subscribe
  public void on(final NexusStoppedEvent e) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "bootAction", "stopped");
    putIfNotNull(data, "nxVersion", systemStatusProvider.get().getVersion());
    putIfNotNull(data, "nxEdition", systemStatusProvider.get().getEditionShort());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_SYSTEM,
        FeedRecorder.SYSTEM_BOOT,
        e.getEventDate(),
        null, // "system" is booting
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }
  //
  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final ConfigurationChangeEvent event) {
  //  if (event.getChanges().isEmpty()) {
  //    return;
  //  }
  //  // keep list unique, one component might be reported multiple times
  //  final HashSet<String> changes = Sets.newHashSet();
  //  for (Configurable changed : event.getChanges()) {
  //    changes.add(changed.getName());
  //  }
  //  final Map<String, String> data = Maps.newHashMap();
  //  putIfNotNull(data, "changes", changes.toString());
  //  putIfNotNull(data, "userId", event.getUserId());
  //  final FeedEvent fe = new FeedEvent(
  //      FeedRecorder.FAMILY_SYSTEM,
  //      FeedRecorder.SYSTEM_CONFIG,
  //      event.getEventDate(),
  //      event.getUserId(),
  //      "/", // link to UI
  //      data
  //  );
  //  getFeedRecorder().addEvent(fe);
  //}

}