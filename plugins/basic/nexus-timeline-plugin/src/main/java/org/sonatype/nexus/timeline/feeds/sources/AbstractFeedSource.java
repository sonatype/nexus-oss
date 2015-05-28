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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.FeedSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support class for {@link FeedSource} implementations.
 *
 * @since 3.0
 */
public abstract class AbstractFeedSource
    extends ComponentSupport
    implements FeedSource
{
  private final FeedRecorder feedRecorder;

  private final String feedKey;

  private final String feedName;

  private final String feedDescription;

  protected AbstractFeedSource(
      final FeedRecorder feedRecorder,
      final String feedKey,
      final String feedName,
      final String feedDescription)
  {
    this.feedRecorder = checkNotNull(feedRecorder);
    this.feedKey = checkNotNull(feedKey);
    this.feedName = checkNotNull(feedName);
    this.feedDescription = checkNotNull(feedDescription);
  }

  protected FeedRecorder getFeedRecorder() {
    return feedRecorder;
  }

  @Override
  public String getFeedKey() {
    return feedKey;
  }

  @Override
  public String getFeedName() {
    return feedName;
  }

  @Override
  public String getFeedDescription() {
    return feedDescription;
  }

  @Override
  public List<FeedEvent> getFeed(int from, int count, Map<String, Object> params)
      throws IOException
  {
    final List<FeedEvent> events = Lists.newArrayList();
    fillInEntries(events, from, count, params);
    return events;
  }

  protected abstract void fillInEntries(final List<FeedEvent> feed, final int from, final int count,
                                        final Map<String, Object> params)
      throws IOException;

  /**
   * Helper method that converts params map into SQL WHERE.
   */
  @Nullable
  protected String toWhere(final Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    for (String key : params.keySet()) {
      if (sb.length() > 0) {
        sb.append(" AND ");
      }
      // key=:key
      sb.append(key).append("=").append(":").append(key);
    }
    return sb.toString();
  }
}
