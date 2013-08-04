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

package org.sonatype.nexus.feeds.record;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenArtifactSignatureAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenChecksumAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenRepositoryMetadataAttribute;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Event inspector that persists item events into Timeline.
 *
 * @author Juven Xu
 * @author cstamas
 */
@Component(role = EventInspector.class, hint = "ItemChangesFeedEventInspector")
public class ItemChangesFeedEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{

  public ItemChangesFeedEventInspector() {
  }

  // for now used just for UTs
  ItemChangesFeedEventInspector(final FeedRecorder feedRecorder,
                                final ApplicationStatusSource applicationStatusSource)
  {
    super(feedRecorder, applicationStatusSource);
  }

  public boolean accepts(Event<?> evt) {
    // RETRIEVE event creates a lot of noise in events, so we are not processing those
    return evt instanceof RepositoryItemEvent && !(evt instanceof RepositoryItemEventRetrieve);
  }

  public void inspect(Event<?> evt) {
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

      NexusItemInfo ai = new NexusItemInfo();
      ai.setRepositoryId(pomItem.getRepositoryId());
      ai.setPath(pomItem.getPath());
      ai.setRemoteUrl(pomItem.getRemoteUrl());

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

      NexusArtifactEvent nae = new NexusArtifactEvent(ievt.getEventDate(), action, "", ai);
      // set context
      nae.addEventContext(ievt.getItemContext());
      // set attributes
      nae.addItemAttributes(ievt.getItem().getRepositoryItemAttributes().asMap());

      getFeedRecorder().addNexusArtifactEvent(nae);
    }
  }

}
