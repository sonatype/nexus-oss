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
import org.sonatype.nexus.repository.storage.AssetCreatedEvent;
import org.sonatype.nexus.repository.storage.AssetDeletedEvent;
import org.sonatype.nexus.repository.storage.AssetEvent;
import org.sonatype.nexus.repository.storage.AssetUpdatedEvent;
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
  public void onAssetEvent(AssetEvent e) {
    String action;
    if (e instanceof AssetCreatedEvent) {
      action = FeedRecorder.ITEM_DEPLOYED;
    }
    else if (e instanceof AssetUpdatedEvent) {
      action = FeedRecorder.ITEM_DEPLOYED_UPDATE;
    }
    else if (e instanceof AssetDeletedEvent) {
      action = FeedRecorder.ITEM_DELETED;
    }
    else {
      return;
    }

    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "repoId", e.getRepository().getName());
    putIfNotNull(data, "repoName", e.getRepository().getName());
    putIfNotNull(data, "itemPath", e.getAsset().name());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_ITEM,
        action,
        new Date(),
        "TODO",
        "/repository/" + e.getRepository().getName() + "/" + e.getAsset().name(),
        data
    );
    getFeedRecorder().addEvent(fe);
  }
}
