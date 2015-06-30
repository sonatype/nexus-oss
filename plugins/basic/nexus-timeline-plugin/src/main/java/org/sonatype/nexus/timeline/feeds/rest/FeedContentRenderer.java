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
package org.sonatype.nexus.timeline.feeds.rest;

import org.sonatype.nexus.timeline.feeds.FeedEvent;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Feed content renderer renders the feed entry content and title. Usually it performs some templating, but
 * does not have to.
 *
 * @since 3.0
 */
public interface FeedContentRenderer
{
  /**
   * Used to set content type (mime) that this entry will be rendered. See {@link SyndContent#setType(String)}.
   */
  String getContentType(FeedEvent evt);

  /**
   * Returns the title of entry. See {@link SyndEntry#setTitle(String)}.
   */
  String getTitle(FeedEvent evt);

  /**
   * Returns the content of the entry. See {@link SyndContent#setValue(String)}.
   */
  String getContent(FeedEvent evt);
}
