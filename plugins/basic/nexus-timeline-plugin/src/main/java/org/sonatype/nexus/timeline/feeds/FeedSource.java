/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

/**
 * A Feed source component. By implementing this interface, one can produce feeds, and will be automatically published
 * by feeds REST resource.
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
