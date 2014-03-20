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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.timeline.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;

@Named(AuthcAuthzFeedSource.CHANNEL_KEY)
@Singleton
public class AuthcAuthzFeedSource
    extends AbstractFeedSource
{
  public static final String CHANNEL_KEY = "authcAuthz";

  @Inject
  public AuthcAuthzFeedSource(final ApplicationStatusSource applicationStatusSource,
                              final FeedRecorder feedRecorder)
  {
    super(applicationStatusSource,
        feedRecorder,
        CHANNEL_KEY,
        "Authentication and Authorization",
        "Authentication and Authorization Events");
  }

  protected void fillInEntries(final SyndFeed feed, final Integer from, final Integer count,
                               final Map<String, String> params)
      throws IOException
  {
    final List<AuthcAuthzEvent> items = getFeedRecorder().getAuthcAuthzEvents(null, from, count, null);
    final List<SyndEntry> entries = Lists.newArrayListWithCapacity(items.size());
    for (AuthcAuthzEvent item : items) {
      final SyndEntry entry = new SyndEntryImpl();
      if (AuthcAuthzEvent.ACTION_AUTHC.equals(item.getAction())) {
        entry.setTitle("Authentication");
      }
      else if (AuthcAuthzEvent.ACTION_AUTHZ.equals(item.getAction())) {
        entry.setTitle("Authorization");
      }
      else {
        entry.setTitle(item.getAction());
      }
      final SyndContent content = new SyndContentImpl();
      content.setType("text/plain");
      content.setValue(item.getMessage());
      entry.setPublishedDate(item.getEventDate());
      entry.setAuthor(feed.getAuthor());
      entry.setLink("/");
      entry.setDescription(content);
      entries.add(entry);
    }
    feed.setEntries(entries);
  }

}
