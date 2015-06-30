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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.nexus.common.event.Asynchronous;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subscriber listening for events recorded under {@link FeedRecorder#FAMILY_REPO} event type.
 */
@Named
@Singleton
public class RepositorySubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  private final Provider<SystemStatus> systemStatusProvider;

  @Inject
  public RepositorySubscriber(final FeedRecorder feedRecorder,
                              final Provider<SystemStatus> systemStatusProvider)
  {
    super(feedRecorder);
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
  }

  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final RepositoryEventLocalStatusChanged revt) {
  //  final Map<String, String> data = Maps.newHashMap();
  //  putIfNotNull(data, "oldLocalStatus", revt.getOldLocalStatus().name());
  //  putIfNotNull(data, "newLocalStatus", revt.getNewLocalStatus().name());
  //  putIfNotNull(data, "repoId", revt.getRepository().getId());
  //  putIfNotNull(data, "repoName", revt.getRepository().getName());
  //  // TODO: who changed it?
  //  //putIfNotNull(data, "user.id", userId);
  //  //putIfNotNull(data, "user.ip", userIp);
  //  //putIfNotNull(data, "user.ua", userAgent);
  //  final FeedEvent fe = new FeedEvent(
  //      FeedRecorder.FAMILY_REPO,
  //      FeedRecorder.REPO_LSTATUS,
  //      revt.getEventDate(),
  //      null, // TODO: who changed it?
  //      "/", // link to UI
  //      data
  //  );
  //  getFeedRecorder().addEvent(fe);
  //}

  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final RepositoryEventProxyModeChanged revt) {
  //  final Map<String, String> data = Maps.newHashMap();
  //  putIfNotNull(data, "oldProxyMode", revt.getOldProxyMode().name());
  //  putIfNotNull(data, "newProxyMode", revt.getNewProxyMode().name());
  //  if (revt.getCause() != null) {
  //    putIfNotNull(data, "lastError", String.valueOf(revt.getCause()));
  //  }
  //  putIfNotNull(data, "repoId", revt.getRepository().getId());
  //  putIfNotNull(data, "repoName", revt.getRepository().getName());
  //  // TODO: who changed it?
  //  //putIfNotNull(data, "user.id", userId);
  //  //putIfNotNull(data, "user.ip", userIp);
  //  //putIfNotNull(data, "user.ua", userAgent);
  //  final FeedEvent fe = new FeedEvent(
  //      FeedRecorder.FAMILY_REPO,
  //      FeedRecorder.REPO_PSTATUS,
  //      revt.getEventDate(),
  //      null, // TODO: who changed it?
  //      "/", // link to UI
  //      data
  //  );
  //  getFeedRecorder().addEvent(fe);
  //}

  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final RepositoryRegistryEventAdd e) {
  //  inspect(e);
  //}
  //
  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final RepositoryRegistryEventRemove e) {
  //  inspect(e);
  //}
  //
  //@Subscribe
  //@AllowConcurrentEvents
  //public void on(final RepositoryConfigurationUpdatedEvent e) {
  //  inspect(e);
  //}
  //
  //private void inspect(Object evt) {
  //  if (!isNexusStarted()) {
  //    return;
  //  }
  //  final Repository repository;
  //  final Date date;
  //  if (evt instanceof RepositoryRegistryRepositoryEvent) {
  //    RepositoryRegistryRepositoryEvent target = (RepositoryRegistryRepositoryEvent)evt;
  //    repository = target.getRepository();
  //    date = target.getEventDate();
  //  }
  //  else {
  //    RepositoryConfigurationUpdatedEvent target = (RepositoryConfigurationUpdatedEvent)evt;
  //    repository = target.getRepository();
  //    date = target.getEventDate();
  //  }
  //
  //  final Map<String, String> data = Maps.newHashMap();
  //  final String action;
  //  if (evt instanceof RepositoryRegistryEventAdd) {
  //    action = FeedRecorder.REPO_CREATED;
  //  }
  //  else if (evt instanceof RepositoryRegistryEventRemove) {
  //    action = FeedRecorder.REPO_DROPPED;
  //  }
  //  else {
  //    action = FeedRecorder.REPO_UPDATED;
  //    final RepositoryConfigurationUpdatedEvent configured = (RepositoryConfigurationUpdatedEvent) evt;
  //    putIfNotNull(data, "localUrlChanged", String.valueOf(configured.isLocalUrlChanged()));
  //    putIfNotNull(data, "remoteUrlChanged", String.valueOf(configured.isRemoteUrlChanged()));
  //    putIfNotNull(data, "downloadRemoteIndexEnabled", String.valueOf(configured.isDownloadRemoteIndexEnabled()));
  //    putIfNotNull(data, "madeSearchable", String.valueOf(configured.isMadeSearchable()));
  //    putIfNotNull(data, "localStatusChanged", String.valueOf(configured.isLocalStatusChanged()));
  //  }
  //
  //  putIfNotNull(data, "repoId", repository.getId());
  //  putIfNotNull(data, "repoName", repository.getName());
  //  // TODO: who changed it?
  //  //putIfNotNull(data, "user.id", userId);
  //  //putIfNotNull(data, "user.ip", userIp);
  //  //putIfNotNull(data, "user.ua", userAgent);
  //  final FeedEvent fe = new FeedEvent(
  //      FeedRecorder.FAMILY_REPO,
  //      action,
  //      date,
  //      null, // TODO: who changed it?
  //      "/", // link to UI
  //      data
  //  );
  //  getFeedRecorder().addEvent(fe);
  //}

  private boolean isNexusStarted() {
    return systemStatusProvider.get().isNexusStarted();
  }
}
