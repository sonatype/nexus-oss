/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.timeline.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.timeline.Timeline;

/**
 * A Feed source. By implementing this interface, one can produce a feed that will be automatically published
 * by feeds resource. A feed source might use {@link FeedRecorder} as source, or use directly {@link Timeline}
 * as source, but does not have to either. Feeds might be sources from any suitable source or produced in any
 * way (ie. by reading up a file).
 *
 * @since 3.0
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
  List<FeedEvent> getFeed(int from, int count, Map<String, String> params)
      throws IOException;
}
