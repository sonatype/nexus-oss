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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Boot listening event inspector. This one is intentionally not async, to mark exact time stamps of Nexus important
 * events: when it booted and when shutdown was commenced.
 */
@Named
@Singleton
public class NexusBootSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber
{
  private final ApplicationStatusSource applicationStatusSource;

  @Inject
  public NexusBootSubscriber(final FeedRecorder feedRecorder,
                             final ApplicationStatusSource applicationStatusSource)
  {
    super(feedRecorder);
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
  }

  @Subscribe
  public void on(final NexusStartedEvent e) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "boot.action", "Started");
    putIfNotNull(data, "nx.version", applicationStatusSource.getSystemStatus().getVersion());
    putIfNotNull(data, "nx.edition", applicationStatusSource.getSystemStatus().getEditionShort());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_SYSTEM,
        FeedRecorder.SYSTEM_BOOT,
        e.getClass().getName(),
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
    putIfNotNull(data, "boot.action", "Stopped");
    putIfNotNull(data, "nx.version", applicationStatusSource.getSystemStatus().getVersion());
    putIfNotNull(data, "nx.edition", applicationStatusSource.getSystemStatus().getEditionShort());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_SYSTEM,
        FeedRecorder.SYSTEM_BOOT,
        e.getClass().getName(),
        e.getEventDate(),
        null, // "system" is booting
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }
}