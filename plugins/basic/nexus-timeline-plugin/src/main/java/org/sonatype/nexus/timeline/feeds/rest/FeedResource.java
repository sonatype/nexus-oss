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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;
import org.sonatype.sisu.siesta.common.error.WebApplicationMessageException;

import com.google.common.collect.Maps;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Feed publishing REST resource. All the components implementing {@link FeedSource} interface will get auto-registered
 * by this resource, and published.
 *
 * @since 3.0
 */
@Named
@Singleton
@Path("/feeds/{" + FeedResource.FEED_KEY + "}")
@Produces({FeedResource.APPLICATION_RSS_XML, FeedResource.APPLICATION_ATOM_XML, MediaType.TEXT_XML})
public class FeedResource
    extends ComponentSupport
    implements Resource
{
  public static final String APPLICATION_RSS_XML = "application/rss+xml";

  public static final String APPLICATION_ATOM_XML = MediaType.APPLICATION_ATOM_XML;

  public static final String FEED_KEY = "feedKey";

  private final Map<String, FeedSource> feeds;

  @Inject
  public FeedResource(final Map<String, FeedSource> feeds) {
    this.feeds = checkNotNull(feeds);
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
  @RequiresPermissions("nexus:feeds")
  public SyndFeed get(@PathParam(FEED_KEY) String feedKey, @DefaultValue("0") @QueryParam("from") int from,
                      @DefaultValue("40") @QueryParam("count") int count, final @Context UriInfo uriInfo)
  {
    final Map<String, String> params = getParameters(uriInfo);
    final SyndFeed feed;
    try {
      final FeedSource src = feeds.get(feedKey);
      if (src != null) {
        feed = src.getFeed(from, count, params);
      }
      else {
        throw new WebApplicationMessageException(Status.NOT_FOUND, "Feed " + feedKey + " not found!");
      }
    }
    catch (IOException e) {
      throw new WebApplicationException(e);
    }

    feed.setLink(BaseUrlHolder.get() + "/service/siesta/feeds/" + feedKey);
    for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
      if (entry.getLink() != null) {
        // full URLs should not be touched
        if (!entry.getLink().startsWith("http")) {
          if (entry.getLink().startsWith("/")) {
            entry.setLink(entry.getLink().substring(1));
          }
          entry.setLink(BaseUrlHolder.get() + entry.getLink());
        }
      }
    }
    return feed;
  }

  /**
   * Just repack into simple KV map, using only the first values.
   */
  private Map<String, String> getParameters(final UriInfo uriInfo) {
    final Map<String, String> result = Maps.newHashMap();
    for (String key : uriInfo.getQueryParameters().keySet()) {
      result.put(key, uriInfo.getQueryParameters().getFirst(key));
    }
    return result;
  }
}
