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

package org.sonatype.nexus.timeline.feeds.subscribers;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class RepositoryLocalStatusChangeSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public RepositoryLocalStatusChangeSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryEventLocalStatusChanged revt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "oldLocalStatus", revt.getOldLocalStatus().name());
    putIfNotNull(data, "newLocalStatus", revt.getNewLocalStatus().name());
    putIfNotNull(data, "repo.id", revt.getRepository().getId());
    putIfNotNull(data, "repo.name", revt.getRepository().getName());
    // TODO: who changed it?
    //putIfNotNull(data, "user.id", userId);
    //putIfNotNull(data, "user.ip", userIp);
    //putIfNotNull(data, "user.ua", userAgent);
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_REPO,
        FeedRecorder.REPO_LSTATUS,
        revt.getClass().getName(),
        revt.getEventDate(),
        null, // TODO: who changed it?
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }
}
