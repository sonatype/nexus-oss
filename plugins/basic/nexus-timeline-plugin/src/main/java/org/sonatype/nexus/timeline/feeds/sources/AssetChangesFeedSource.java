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
package org.sonatype.nexus.timeline.feeds.sources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.ImmutableSet;

@Named(AssetChangesFeedSource.CHANNEL_KEY)
@Singleton
public class AssetChangesFeedSource
    extends AbstractFeedSource
{
  public static final String CHANNEL_KEY = "assetChanges";

  private static final Set<String> TYPE_SET = ImmutableSet.of(FeedRecorder.FAMILY_ASSET);

  private static final Set<String> SUBTYPE_SET = ImmutableSet.of(
      FeedRecorder.ASSET_CACHED,
      FeedRecorder.ASSET_DEPLOYED,
      FeedRecorder.ASSET_CACHED_UPDATE,
      FeedRecorder.ASSET_DEPLOYED_UPDATE,
      FeedRecorder.ASSET_DELETED
  );

  @Inject
  public AssetChangesFeedSource(final FeedRecorder feedRecorder) {
    super(feedRecorder,
        CHANNEL_KEY,
        "Changed assets",
        "Asset changes (cached, deployed or deleted).");
  }

  @Override
  protected List<FeedEvent> getEntries(final int from,
                                       final int count,
                                       final Map<String, Object> params)
  {
    return getFeedRecorder().getEvents(
        TYPE_SET,
        SUBTYPE_SET,
        toWhere(params),
        params,
        from,
        count,
        null
    );
  }

  /**
   * Formats entry title for event.
   */
  @Override
  protected String title(FeedEvent evt) {
    final String assetName = evt.getData().get("assetName");
    if (FeedRecorder.ASSET_CACHED.equals(evt.getEventSubType())) {
      return "Cached: " + assetName;
    }
    else if (FeedRecorder.ASSET_CACHED_UPDATE.equals(evt.getEventSubType())) {
      return "Cached (update): " + assetName;
    }
    else if (FeedRecorder.ASSET_DEPLOYED.equals(evt.getEventSubType())) {
      return "Deployed: " + assetName;
    }
    else if (FeedRecorder.ASSET_DEPLOYED_UPDATE.equals(evt.getEventSubType())) {
      return "Deployed (update): " + assetName;
    }
    else if (FeedRecorder.ASSET_DELETED.equals(evt.getEventSubType())) {
      return "Deleted: " + assetName;
    }
    return super.title(evt);
  }
}
