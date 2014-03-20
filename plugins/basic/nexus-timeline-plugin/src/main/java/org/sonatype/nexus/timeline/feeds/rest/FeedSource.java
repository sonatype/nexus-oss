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

package org.sonatype.nexus.timeline.feeds.rest;

import java.io.IOException;
import java.util.Map;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A Feed source. Components implementing this interface produce feeds, and will be automatically published by
 * {@link FeedPlexusResource} and {@link FeedsListPlexusResource}.
 *
 * @since 3.0 (relocated from nexus-timeline-plugin)
 */
public interface FeedSource
{
  /**
   * Returns the key that identifies this channel.
   */
  String getFeedKey();

  /**
   * Returns the feed human name, used in UI to describe the feed.
   */
  String getFeedName();

  /**
   * Returns longer (than {@link #getFeedName()}) description about the feed contents.
   */
  String getFeedDescription();

  /**
   * Returns the feed.
   *
   * @param from   Record number to start with. Newest record is 0.
   * @param count  Count of the entries to generate.
   * @param params A map of all the client made parameters.
   */
  SyndFeed getFeed(Integer from, Integer count, Map<String, String> params)
      throws IOException;
}
