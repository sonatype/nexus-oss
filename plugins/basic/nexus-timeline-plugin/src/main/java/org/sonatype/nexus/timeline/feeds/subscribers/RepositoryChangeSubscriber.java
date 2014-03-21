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

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.Event;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class RepositoryChangeSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  private final ApplicationStatusSource applicationStatusSource;

  private final RepositoryRegistry repoRegistry;

  @Inject
  public RepositoryChangeSubscriber(final FeedRecorder feedRecorder,
                                    final ApplicationStatusSource applicationStatusSource,
                                    final RepositoryRegistry repoRegistry)
  {
    super(feedRecorder);
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.repoRegistry = checkNotNull(repoRegistry);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryRegistryEventAdd e) {
    inspect(e);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryRegistryEventRemove e) {
    inspect(e);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryConfigurationUpdatedEvent e) {
    inspect(e);
  }

  private void inspect(Event<?> evt) {
    if (!isNexusStarted()) {
      return;
    }
    final Repository repository;
    if (evt instanceof RepositoryRegistryRepositoryEvent) {
      repository = ((RepositoryRegistryRepositoryEvent) evt).getRepository();
    }
    else {
      repository = ((RepositoryConfigurationUpdatedEvent) evt).getRepository();
    }

    final Map<String, String> data = Maps.newHashMap();
    final String action;
    if (evt instanceof RepositoryRegistryEventAdd) {
      action = FeedRecorder.REPO_CREATED;
    }
    else if (evt instanceof RepositoryRegistryEventRemove) {
      action = FeedRecorder.REPO_DROPPED;
    }
    else {
      action = FeedRecorder.REPO_UPDATED;
      final RepositoryConfigurationUpdatedEvent configured = (RepositoryConfigurationUpdatedEvent) evt;
      putIfNotNull(data, "localUrlChanged", String.valueOf(configured.isLocalUrlChanged()));
      putIfNotNull(data, "remoteUrlChanged", String.valueOf(configured.isRemoteUrlChanged()));
      putIfNotNull(data, "downloadRemoteIndexEnabled", String.valueOf(configured.isDownloadRemoteIndexEnabled()));
      putIfNotNull(data, "madeSearchable", String.valueOf(configured.isMadeSearchable()));
      putIfNotNull(data, "localStatusChanged", String.valueOf(configured.isLocalStatusChanged()));
    }

    putIfNotNull(data, "repo.id", repository.getId());
    putIfNotNull(data, "repo.name", repository.getName());
    // TODO: who changed it?
    //putIfNotNull(data, "user.id", userId);
    //putIfNotNull(data, "user.ip", userIp);
    //putIfNotNull(data, "user.ua", userAgent);
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_REPO,
        action,
        evt.getClass().getName(),
        evt.getEventDate(),
        null, // TODO: who changed it?
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  private boolean isNexusStarted() {
    return applicationStatusSource.getSystemStatus().isNexusStarted();
  }
}
