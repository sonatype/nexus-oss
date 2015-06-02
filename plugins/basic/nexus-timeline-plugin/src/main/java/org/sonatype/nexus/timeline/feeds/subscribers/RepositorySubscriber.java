/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.event.Asynchronous;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.repository.RepositoryEvent;
import org.sonatype.nexus.repository.manager.RepositoryCreatedEvent;
import org.sonatype.nexus.repository.manager.RepositoryDeletedEvent;
import org.sonatype.nexus.repository.manager.RepositoryUpdatedEvent;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * Subscriber listening for events recorded under {@link FeedRecorder#FAMILY_REPO} event type.
 */
@Named
@Singleton
public class RepositorySubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public RepositorySubscriber(final FeedRecorder feedRecorder)
  {
    super(feedRecorder);
  }

  // TODO: proxy connection changes?
  // TODO: online state change?

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryCreatedEvent e) {
    onRepositoryEvent(e, FeedRecorder.REPO_CREATED);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryUpdatedEvent e) {
    onRepositoryEvent(e, FeedRecorder.REPO_UPDATED);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryDeletedEvent e) {
    onRepositoryEvent(e, FeedRecorder.REPO_DELETED);
  }


  private void onRepositoryEvent(final RepositoryEvent event, final String subType) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "repoName", event.getRepository().getName());
    putIfNotNull(data, "repoType", event.getRepository().getType().getValue());
    putIfNotNull(data, "repoFormat", event.getRepository().getFormat().getValue());
    final FeedEvent feedEvent = new FeedEvent(
        FeedRecorder.FAMILY_REPO,
        subType,
        new Date(),
        "unknown", // TODO: Async EventSubscriber cannot get clientInfo of originating thread
        "http://localhost:8081/#admin/repository/repositories",
        data
    );
    getFeedRecorder().addEvent(feedEvent);
  }
}
