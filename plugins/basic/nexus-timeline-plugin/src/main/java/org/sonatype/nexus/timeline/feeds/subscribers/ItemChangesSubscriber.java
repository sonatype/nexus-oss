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
import org.sonatype.nexus.events.Event;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenArtifactSignatureAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenChecksumAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenRepositoryMetadataAttribute;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class ItemChangesSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public ItemChangesSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  // RETRIEVE event creates a lot of noise in events, so we are not processing those

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryItemEventCache evt) {
    inspectForNexus(evt);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryItemEventStore evt) {
    inspectForNexus(evt);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryItemEventDelete evt) {
    inspectForNexus(evt);
  }

  private void inspectForNexus(Event<?> evt) {
    RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

    // filter out links and dirs/collections and hidden files
    if (StorageFileItem.class.isAssignableFrom(ievt.getItem().getClass())
        && !ievt.getItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)
        && !ievt.getItemUid().getBooleanAttributeValue(IsMavenRepositoryMetadataAttribute.class) // "maven-metadata.xml"
        && !ievt.getItemUid().getBooleanAttributeValue(IsMavenArtifactSignatureAttribute.class) // "*.asc"
        && !ievt.getItemUid().getBooleanAttributeValue(IsMavenChecksumAttribute.class) // "*.sha1" or "*.md5"
        && !((StorageFileItem) ievt.getItem()).isContentGenerated()) {

      String action;
      if (ievt instanceof RepositoryItemEventCache) {
        action = FeedRecorder.ITEM_CACHED;
      }
      else if (ievt instanceof RepositoryItemEventStore) {
        action = FeedRecorder.ITEM_DEPLOYED;
      }
      else if (ievt instanceof RepositoryItemEventDelete) {
        action = FeedRecorder.ITEM_DELETED;
      }
      else {
        return;
      }

      final StorageFileItem fileItem = (StorageFileItem) ievt.getItem();
      final Map<String, String> data = Maps.newHashMap();
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
          evt.getClass().getName(),
          evt.getEventDate(),
          userId,
          "/content/repositories/" + fileItem.getRepositoryId() + "/" + fileItem.getParentPath(), // link to item parent
          data
      );
      getFeedRecorder().addEvent(fe);
    }
  }

}
