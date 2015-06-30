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
package org.sonatype.nexus.timeline.internal.ui

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.common.app.BaseUrlHolder
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.PagedResponse
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.timeline.feeds.FeedSource
import org.sonatype.nexus.timeline.feeds.rest.FeedContentRenderer

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresPermissions

/**
 * Feed {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'timeline_Feed')
class FeedComponent
extends DirectComponentSupport
{
  @Inject
  Map<String, FeedSource> feedSources

  @Inject
  FeedContentRenderer feedContentRenderer

  /**
   * Retrieves feed sources.
   * @return a list of feed sources
   */
  @DirectMethod
  @RequiresPermissions('nexus:feeds:read')
  List<FeedXO> read() {
    return feedSources.values().collect { source ->
      new FeedXO(
          key: source.feedKey,
          name: source.feedName,
          description: source.feedDescription,
          url: "${BaseUrlHolder.get()}/service/siesta/timeline/feeds/${source.feedKey}"
      )
    }
  }

  /**
   * Retrieves feed entries.
   * @return a list of feed entries
   */
  @DirectMethod
  @RequiresPermissions('nexus:feeds:read')
  PagedResponse<FeedEntryXO> readEntries(final StoreLoadParameters parameters) {
    String feedKey = parameters.getFilter('key')
    if (feedKey) {
      FeedSource feedSource = feedSources.get(feedKey);
      if (feedSource) {
        return new PagedResponse<FeedEntryXO>(
            feedSource.getFeed(0, Integer.MAX_VALUE, Collections.emptyMap()).size(),
            feedSource.getFeed(parameters.start, parameters.limit - parameters.start, Collections.emptyMap()).collect { entry ->
              new FeedEntryXO(
                  title: feedContentRenderer.getTitle(entry),
                  published: entry.getPublished(),
                  content: feedContentRenderer.getContent(entry)
              )
            }
        )
      }
    }
  }
}
