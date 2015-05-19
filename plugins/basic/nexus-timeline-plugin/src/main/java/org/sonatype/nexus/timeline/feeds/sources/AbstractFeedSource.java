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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.feeds.AnyOfFilter;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.FeedSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
  public List<FeedEvent> getFeed(int from, int count, Map<String, String> params)
      throws IOException
  {
    final List<FeedEvent> events = Lists.newArrayList();
    fillInEntries(events, from, count, params);
    return events;
  }

  protected abstract void fillInEntries(final List<FeedEvent> feed, final int from, final int count,
                                        final Map<String, String> params)
      throws IOException;

  /**
   * Creates predicates out of the map keys and values. It consider entry key for key, and value as "allowed value".
   * Hence, for values, one might use comma separated values. It uses {@link AnyOfFilter} to implement predicate for
   * each map entry. Values are turned into sets in method {@link #valueSet(String)}.
   * <p/>
   * Example HTTP request with query parameters {@code ?foo=1&bar=a,b,c} will be turned into predicates like
   * (pseudo code):
   * <pre>
   *   and(
   *     value of "foo" member of {1}, // this basically means "foo=1"
   *     value of "bar" member of {a,b,c}, // this basically means "bar" value is in set of {a,b,c}
   *   );
   * </pre>
   *
   * @see {@link AnyOfFilter}
   * @see {@link #valueSet(String)}
   */
  protected Predicate<Entry> filters(final Map<String, String> params) {
    if (params.isEmpty()) {
      return Predicates.alwaysTrue();
    }
    final List<Predicate<Entry>> filters = Lists.newArrayList();
    for (Map.Entry<String, String> param : params.entrySet()) {
      // TODO: maybe all with "_" as prefix? But then how to filter for type and subType?
      if (param.getKey().equals("_dc")) {
        continue;
      }
      final AnyOfFilter filter = new AnyOfFilter(param.getKey(), valueSet(param.getValue()));
      filters.add(filter);
    }
    return Predicates.and(filters);
  }

  /**
   * Creates a string set out of passed in string. Separator detected is comma.
   */
  protected Set<String> valueSet(final String value) {
    final HashSet<String> result = Sets.newHashSet();
    if (value.contains(",")) {
      result.addAll(Splitter.on(",").splitToList(value));
    }
    else {
      result.add(value);
    }
    return result;
  }
}
