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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.event.Asynchronous;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.repository.storage.AssetCreatedEvent;
import org.sonatype.nexus.repository.storage.AssetDeletedEvent;
import org.sonatype.nexus.repository.storage.AssetEvent;
import org.sonatype.nexus.repository.storage.AssetUpdatedEvent;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.security.ClientInfo;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * Subscriber listening for asset events.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AssetSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public AssetSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onEvent(AssetEvent e) {
    String action;
    if (e instanceof AssetCreatedEvent) {
      if (ProxyType.NAME.equals(e.getRepository().getType().getValue())) {
        action = FeedRecorder.ASSET_CACHED;
      }
      else {
        action = FeedRecorder.ASSET_DEPLOYED;
      }
    }
    else if (e instanceof AssetUpdatedEvent) {
      if (ProxyType.NAME.equals(e.getRepository().getType().getValue())) {
        action = FeedRecorder.ASSET_CACHED_UPDATE;
      }
      else {
        action = FeedRecorder.ASSET_DEPLOYED_UPDATE;
      }
    }
    else if (e instanceof AssetDeletedEvent) {
      action = FeedRecorder.ASSET_DELETED;
    }
    else {
      return;
    }

    final ClientInfo clientInfo = e.getClientInfo();

    final Map<String, String> data = Maps.newHashMap();
    // map is for display/templating purposes
    putIfNotNull(data, "repoName", e.getRepository().getName());
    putIfNotNull(data, "assetName", e.getAsset().name());
    putIfNotNull(data, "userId", getUserId(clientInfo, "n/a"));
    putIfNotNull(data, "userIp", clientInfo == null ? "n/a" : clientInfo.getRemoteIP());
    putIfNotNull(data, "userUa", clientInfo == null ? "n/a" : clientInfo.getUserAgent());
    // feed event is persisted, is searchable/filterable by these properties
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_ASSET,
        action,
        new Date(),
        getUserId(clientInfo, null),
        "/repository/" + e.getRepository().getName() + e.getAsset().name(), // TODO: this is true for Maven only!
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Nullable
  private String getUserId(@Nullable final ClientInfo clientInfo, @Nullable final String defaultValue) {
    if (clientInfo == null) {
      return defaultValue;
    }
    if (clientInfo.getUserid() == null) {
      return defaultValue;
    }
    return clientInfo.getUserid();
  }
}
