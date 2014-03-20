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

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.rest.FeedSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

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
  private final ApplicationStatusSource applicationStatusSource;

  private final FeedRecorder feedRecorder;

  private final String feedKey;

  private final String feedName;

  private final String feedDescription;

  protected AbstractFeedSource(final ApplicationStatusSource applicationStatusSource,
                               final FeedRecorder feedRecorder,
                               final String feedKey,
                               final String feedName,
                               final String feedDescription)
  {
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
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
  public SyndFeed getFeed(Integer from, Integer count, Map<String, String> params)
      throws IOException
  {
    final SyndFeedImpl feed = new SyndFeedImpl();
    feed.setTitle(getFeedName());
    feed.setDescription(getFeedDescription());
    feed.setAuthor("Nexus " + applicationStatusSource.getSystemStatus().getVersion());
    feed.setPublishedDate(new Date());
    fillInEntries(feed, from, count, params);
    return feed;
  }

  protected abstract void fillInEntries(final SyndFeed feed, final Integer from, final Integer count,
                                        final Map<String, String> params)
      throws IOException;
}
