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

package org.sonatype.nexus.timeline.feeds.sources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractNexusItemEventFeedSource
    extends AbstractFeedSource
{

  private final SyndEntryBuilder<NexusArtifactEvent> syndEntryBuilder;

  protected AbstractNexusItemEventFeedSource(final ApplicationStatusSource applicationStatusSource,
                                             final FeedRecorder feedRecorder,
                                             final SyndEntryBuilder<NexusArtifactEvent> syndEntryBuilder,
                                             final String feedKey,
                                             final String feedName,
                                             final String feedDescription)
  {
    super(applicationStatusSource, feedRecorder, feedKey, feedName, feedDescription);
    this.syndEntryBuilder = checkNotNull(syndEntryBuilder);
  }

  protected abstract List<NexusArtifactEvent> getEventList(Integer from, Integer count, Map<String, String> params);

  protected void fillInEntries(final SyndFeed feed, final Integer from, final Integer count,
                               final Map<String, String> params)
  {
    final List<NexusArtifactEvent> events = getEventList(from, count, params);
    final List<SyndEntry> entries = Lists.newArrayListWithCapacity(events.size());
    for (NexusArtifactEvent event : events) {
      if (syndEntryBuilder.shouldBuildEntry(event)) {
        entries.add(syndEntryBuilder.buildEntry(event));
      }
    }
    feed.setEntries(entries);
  }

  protected Set<String> getRepoIdsFromParams(final Map<String, String> params) {
    if (params != null && params.containsKey("r")) {
      HashSet<String> result = Sets.newHashSet();
      String value = params.get("r");
      if (value.contains(",")) {
        String[] values = StringUtils.split(value, ",");
        result.addAll(Arrays.asList(values));
      }
      else {
        result.add(value);
      }
      return result;
    }
    else {
      return null;
    }
  }
}
