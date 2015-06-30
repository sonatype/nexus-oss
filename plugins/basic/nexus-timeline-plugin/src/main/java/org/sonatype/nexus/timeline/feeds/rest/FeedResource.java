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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedSource;
import org.sonatype.siesta.Resource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Feed publishing resource. All the components implementing {@link FeedSource} interface will get auto-registered
 * by this resource, and published. This resource is not a "usual" resource, meaning it produces feed content (RSS/2.0
 * or Atom/1.0, based on client's request) and not the "usual" JSON or XML response.
 *
 * @since 3.0
 */
@Named
@Singleton
@Path("/timeline/feeds/{" + FeedResource.FEED_KEY + "}")
@Produces({RomeProvider.APPLICATION_RSS_XML, RomeProvider.APPLICATION_ATOM_XML, MediaType.TEXT_XML})
public class FeedResource
    extends ComponentSupport
    implements Resource
{
  public static final String FEED_KEY = "feedKey";

  private final Provider<SystemStatus> systemStatusProvider;

  private final Map<String, FeedSource> feeds;

  private final FeedContentRenderer feedContentRenderer;

  @Inject
  public FeedResource(final Provider<SystemStatus> systemStatusProvider,
                      final Map<String, FeedSource> feeds,
                      final FeedContentRenderer feedContentRenderer)
  {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
    this.feeds = checkNotNull(feeds);
    this.feedContentRenderer = checkNotNull(feedContentRenderer);
  }

  /**
   * Returns the feed corresponding to the requested feed key. The existing feed keys (the list of feeds is not
   * fixed, plugins may contribute new feeds) should be queried by fetching the /feeds resource. Content negotiation is
   * used to figure out returned representation, but RSS (application/rss+xml MIME type) is the default one.
   *
   * @param feedKey The feed key of the feed to be returned.
   * @param from    The number of skipped entries (for paging).
   * @param count   The count of entries to be returned (for paging).
   */
  @GET
  @RequiresPermissions("nexus:feeds:read")
  public SyndFeed get(@PathParam(FEED_KEY) String feedKey, @DefaultValue("0") @QueryParam("from") int from,
                      @DefaultValue("40") @QueryParam("count") int count, final @Context UriInfo uriInfo)
  {
    final FeedSource feedSource = feeds.get(feedKey);
    if (feedSource == null) {
      throw new NotFoundException("Feed " + feedKey + " not found!");
    }

    try {
      final Map<String, String> params = getParameters(uriInfo);
      final List<FeedEvent> feedEvents = feedSource.getFeed(from, count, params);

      final SyndFeed feed = new SyndFeedImpl();
      feed.setTitle(feedSource.getFeedName());
      feed.setDescription(feedSource.getFeedDescription());
      feed.setAuthor("Nexus " + systemStatusProvider.get().getVersion());
      feed.setPublishedDate(new Date());
      feed.setLink(BaseUrlHolder.get() + "/service/siesta/feeds/" + feedSource.getFeedKey());


      final List<SyndEntry> entries = Lists.newArrayListWithCapacity(feedEvents.size());
      for (FeedEvent event : feedEvents) {
        final SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(feedContentRenderer.getTitle(event));
        entry.setPublishedDate(event.getPublished());
        if (event.getAuthor() != null) {
          entry.setAuthor(event.getAuthor());
        }
        else {
          entry.setAuthor(feed.getAuthor());
        }
        if (event.getLink() != null) {
          if (event.getLink().startsWith("http:") || event.getLink().startsWith("https:")) {
            // this is full URL, use it as is
            entry.setLink(event.getLink());
          }
          else {
            entry.setLink(BaseUrlHolder.get() + event.getLink());
          }
        }
        final SyndContent content = new SyndContentImpl();
        content.setType(feedContentRenderer.getContentType(event));
        content.setValue(feedContentRenderer.getContent(event));
        entry.setDescription(content);
        entries.add(entry);
      }
      feed.setEntries(entries);
      return feed;
    }
    catch (Exception e) {
      log.error("Problem during feed creation", e);
      throw new WebApplicationException(e);
    }
  }

  /**
   * Just repack into simple KV map, using only the first values.
   */
  private Map<String, String> getParameters(final UriInfo uriInfo) {
    final Map<String, String> result = Maps.newHashMap();
    for (String key : uriInfo.getQueryParameters().keySet()) {
      result.put(key, uriInfo.getQueryParameters().getFirst(key));
    }
    result.remove("from");
    result.remove("count");
    return result;
  }
}
