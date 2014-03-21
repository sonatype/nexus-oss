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
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailed;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailedChecksum;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailedFileType;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class ItemValidationSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public ItemValidationSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryItemValidationEventFailed ievt) {
    if (ievt.getItem() instanceof StorageFileItem) {
      String action = FeedRecorder.ITEM_BROKEN;
      if (ievt instanceof RepositoryItemValidationEventFailedChecksum) {
        action = FeedRecorder.ITEM_BROKEN_WRONG_REMOTE_CHECKSUM;
      }
      else if (ievt instanceof RepositoryItemValidationEventFailedFileType) {
        action = FeedRecorder.ITEM_BROKEN_INVALID_CONTENT;
      }

      final StorageFileItem fileItem = (StorageFileItem) ievt.getItem();
      final Map<String, String> data = Maps.newHashMap();
      putIfNotNull(data, "validation", ievt.getMessage());
      putIfNotNull(data, "repo.id", fileItem.getRepositoryItemUid().getRepository().getId());
      putIfNotNull(data, "repo.name", fileItem.getRepositoryItemUid().getRepository().getName());
      putIfNotNull(data, "item.path", fileItem.getPath());
      putIfNotNull(data, "item.remoteUrl", fileItem.getRemoteUrl());
      final String userId = (String) fileItem.getResourceStoreRequest().getRequestContext()
          .get(AccessManager.REQUEST_USER);
      putIfNotNull(data, "user.id", userId);
      putIfNotNull(data, "user.ip",
          (String) fileItem.getResourceStoreRequest().getRequestContext().get(AccessManager.REQUEST_REMOTE_ADDRESS));
      putIfNotNull(data, "user.ua",
          (String) fileItem.getResourceStoreRequest().getRequestContext().get(AccessManager.REQUEST_AGENT));
      final FeedEvent fe = new FeedEvent(
          FeedRecorder.FAMILY_ITEM,
          action,
          ievt.getClass().getName(),
          ievt.getEventDate(),
          userId,
          "/content/repositories/" + fileItem.getRepositoryId() + fileItem.getParentPath(), // link to item parent
          data
      );
      getFeedRecorder().addEvent(fe);
    }
  }
}
