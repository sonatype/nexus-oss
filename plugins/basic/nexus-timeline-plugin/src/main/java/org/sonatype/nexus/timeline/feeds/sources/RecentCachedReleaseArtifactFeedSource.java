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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent;
import org.sonatype.nexus.timeline.feeds.RepositoryIdTimelineFilter;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

@Named(RecentCachedReleaseArtifactFeedSource.CHANNEL_KEY)
@Singleton
public class RecentCachedReleaseArtifactFeedSource
    extends AbstractNexusReleaseArtifactEventFeedSource
{

  public static final String CHANNEL_KEY = "recentlyCachedReleaseArtifacts";

  @Inject
  public RecentCachedReleaseArtifactFeedSource(
      final ApplicationStatusSource applicationStatusSource,
      final FeedRecorder feedRecorder,
      final @Named("artifact") SyndEntryBuilder<NexusArtifactEvent> syndEntryBuilder,
      final RepositoryRegistry repositoryRegistry)
  {
    super(applicationStatusSource, feedRecorder, syndEntryBuilder, CHANNEL_KEY,
        "New cached release artifacts",
        "New cached release artifacts in all Nexus repositories (cached).",
        repositoryRegistry);
  }

  @Override
  public List<NexusArtifactEvent> getEventList(Integer from, Integer count, Map<String, String> params) {
    final Set<String> repositoryIds = getRepoIdsFromParams(params);

    final Predicate<Entry> filter =
        (repositoryIds == null || repositoryIds.isEmpty()) ? null
            : new RepositoryIdTimelineFilter(repositoryIds);

    return getFeedRecorder().getNexusArtifectEvents(ImmutableSet.of(NexusArtifactEvent.ACTION_CACHED), from, count,
        filter);
  }
}
