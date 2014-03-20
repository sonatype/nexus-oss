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

package org.sonatype.nexus.timeline.feeds.record;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.Event;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenArtifactSignatureAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenChecksumAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenRepositoryMetadataAttribute;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent.NexusItemInfo;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class ItemChangesFeedEventInspector
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  @Inject
  public ItemChangesFeedEventInspector(final FeedRecorder feedRecorder) {
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
      StorageFileItem pomItem = (StorageFileItem) ievt.getItem();

      String action;
      if (ievt instanceof RepositoryItemEventCache) {
        action = NexusArtifactEvent.ACTION_CACHED;
      }
      else if (ievt instanceof RepositoryItemEventStore) {
        action = NexusArtifactEvent.ACTION_DEPLOYED;
      }
      else if (ievt instanceof RepositoryItemEventDelete) {
        action = NexusArtifactEvent.ACTION_DELETED;
      }
      else {
        return;
      }

      NexusArtifactEvent nae = new NexusArtifactEvent(ievt.getEventDate(), action, "",
          new NexusItemInfo(pomItem.getPath(), pomItem.getRepositoryId(), pomItem.getRemoteUrl()));
      // set context
      nae.addEventContext(ievt.getItemContext());
      // set attributes
      nae.addItemAttributes(ievt.getItem().getRepositoryItemAttributes().asMap());

      getFeedRecorder().addNexusArtifactEvent(nae);
    }
  }

}
