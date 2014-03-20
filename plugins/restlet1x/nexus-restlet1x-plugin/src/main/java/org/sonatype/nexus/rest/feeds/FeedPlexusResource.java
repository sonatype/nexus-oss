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

package org.sonatype.nexus.rest.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Feed publishing REST resource. All the components implementing {@link FeedSource} interface will get auto-registered
 * by this resource, and published.
 *
 * @author cstamas
 * @author dip
 * @since 3.0 (relocated from nexus-timeline-plugin)
 */
@Path("/feeds/{" + FeedPlexusResource.FEED_KEY + "}")
@Produces({"application/rss+xml", "application/atom+xml", "text/xml"})
@Named
@Singleton
public class FeedPlexusResource
    extends AbstractNexusPlexusResource
{
  public static final MediaType RSS_MEDIA_TYPE = new MediaType("application/rss+xml", "RSS syndication documents");

  public static final MediaType ATOM_MEDIA_TYPE = MediaType.APPLICATION_ATOM_XML;

  public static final String FEED_KEY = "feedKey";

  private static final String RSS_2_0 = "rss_2.0";

  private static final String ATOM_1_0 = "atom_1.0";

  private final Map<String, FeedSource> feeds;

  @Inject
  public FeedPlexusResource(final Map<String, FeedSource> feeds) {
    this.feeds = checkNotNull(feeds);
  }

  @Override
  public Object getPayloadInstance() {
    // RO resource, no payload
    return null;
  }

  @Override
  public String getResourceUri() {
    return "/feeds/{" + FEED_KEY + "}";
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/feeds/*", "authcBasic,perms[nexus:feeds]");
  }

  @Override
  public List<Variant> getVariants() {
    List<Variant> result = super.getVariants();

    // the default resource implementation returns
    // application/xml and application/json
    result.add(new Variant(RSS_MEDIA_TYPE));
    result.add(new Variant(ATOM_MEDIA_TYPE));
    result.add(new Variant(MediaType.TEXT_XML));

    return result;
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
  @Override
  @GET
  @ResourceMethodSignature(pathParams = {@PathParam("feedKey")}, queryParams = {
      @QueryParam("from"), @QueryParam("count"), @QueryParam("r")
  }, output = String.class)
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    MediaType mediaType = variant.getMediaType();
    if (!MediaType.APPLICATION_JSON.equals(mediaType, true)) {
      final SyndFeed feed = getFeed(request);

      if (ATOM_MEDIA_TYPE.equals(mediaType, true)) {
        feed.setFeedType(ATOM_1_0);
      }
      else {
        feed.setFeedType(RSS_2_0);

        // set the content type to RSS by default,
        // however keep text/xml if it was requested this way (IE bug, see NEXUS-991)
        if (!MediaType.TEXT_XML.equals(mediaType, true)) {
          mediaType = RSS_MEDIA_TYPE;
        }
      }
      feed.setLink(request.getResourceRef().toString());
      return new FeedRepresentation(mediaType, feed);
    }
    else {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
          "Supported content type is '" + RSS_MEDIA_TYPE.getMainType() + "' and '" + ATOM_MEDIA_TYPE.getMainType() +
              "' only."
      );
    }
  }

  private SyndFeed getFeed(final Request request)
      throws ResourceException
  {
    final Form params = request.getResourceRef().getQueryAsForm();
    Integer from = null;
    Integer count = null;

    try {
      if (params.getFirstValue("from") != null) {
        from = Integer.valueOf(params.getFirstValue("from"));
      }

      if (params.getFirstValue("count") != null) {
        count = Integer.valueOf(params.getFirstValue("count"));
      }
    }
    catch (NumberFormatException e) {
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST,
          "The 'from' and 'count' parameters must be numbers!",
          e);
    }

    final String feedKey = (String) request.getAttributes().get(FEED_KEY);
    final SyndFeed feed;
    try {
      final FeedSource src = feeds.get(feedKey);
      if (src != null) {
        feed = src.getFeed(from, count, params.getValuesMap());
      }
      else {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Feed " + feedKey + " not found!");
      }
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }

    // resolve all links, if needed. Create absolute URLs
    if (feed.getLink() != null) {
      // full URLs should not be touched
      if (!feed.getLink().startsWith("http")) {
        if (feed.getLink().startsWith("/")) {
          feed.setLink(feed.getLink().substring(1));
        }
        feed.setLink(createRootReference(request, feed.getLink()).toString());
      }
    }
    for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
      if (entry.getLink() != null) {
        // full URLs should not be touched
        if (!entry.getLink().startsWith("http")) {
          if (entry.getLink().startsWith("/")) {
            entry.setLink(entry.getLink().substring(1));
          }
          entry.setLink(createRootReference(request, entry.getLink()).toString());
        }
      }
    }
    return feed;
  }
}
