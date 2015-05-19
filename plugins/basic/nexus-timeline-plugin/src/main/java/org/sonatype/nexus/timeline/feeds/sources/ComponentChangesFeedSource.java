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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.sisu.goodies.common.SimpleFormat;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Named(ComponentChangesFeedSource.CHANNEL_KEY)
@Singleton
public class ComponentChangesFeedSource
    extends AbstractFeedSource
{
  public static final String CHANNEL_KEY = "componentChanges";

  @Inject
  public ComponentChangesFeedSource(final FeedRecorder feedRecorder) {
    super(feedRecorder,
        CHANNEL_KEY,
        "Changed components",
        "Component changes (cached, deployed or deleted).");
  }


  @Override
  public void fillInEntries(final List<FeedEvent> entries,
                            final int from,
                            final int count,
                            final Map<String, String> params)
  {
    Iterables.addAll(entries,
        Iterables.transform(
            getFeedRecorder().getEvents(
                ImmutableSet.of(FeedRecorder.FAMILY_COMPONENT),
                ImmutableSet.of(
                    FeedRecorder.COMPONENT_CACHED,
                    FeedRecorder.COMPONENT_DEPLOYED,
                    FeedRecorder.COMPONENT_CACHED_UPDATE,
                    FeedRecorder.COMPONENT_DEPLOYED_UPDATE,
                    FeedRecorder.COMPONENT_DELETED
                ),
                from,
                count,
                filters(params)),
            new Function<FeedEvent, FeedEvent>()
            {
              @Override
              public FeedEvent apply(final FeedEvent input) {
                input.setTitle(title(input));
                return input;
              }
            }
        ));
  }

  /**
   * Formats entry title for event.
   */
  private String title(FeedEvent evt) {
    final String componentName = SimpleFormat.format(
        "%s:%s:%s",
        evt.getData().get("componentGroup"),
        evt.getData().get("componentName"),
        evt.getData().get("componentVersion"));
    if (FeedRecorder.ASSET_CACHED.equals(evt.getEventSubType())) {
      return "Cached: " + componentName;
    }
    else if (FeedRecorder.ASSET_CACHED_UPDATE.equals(evt.getEventSubType())) {
      return "Cached (update): " + componentName;
    }
    else if (FeedRecorder.ASSET_DEPLOYED.equals(evt.getEventSubType())) {
      return "Deployed: " + componentName;
    }
    else if (FeedRecorder.ASSET_DEPLOYED_UPDATE.equals(evt.getEventSubType())) {
      return "Deployed (update): " + componentName;
    }
    else if (FeedRecorder.ASSET_DELETED.equals(evt.getEventSubType())) {
      return "Deleted: " + componentName;
    }
    else {
      // TODO: Some human-readable fallback?
      return evt.getEventType() + ":" + evt.getEventSubType();
    }
  }
}
