/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Simple class that calculates the feed key by attribute (and it is probably mapped with FEED_KEY in some router).
 * 
 * @author cstamas
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "feed" )
@Path( "/feeds/{" + FeedPlexusResource.FEED_KEY + "}" )
@Produces( { "application/rss+xml", "application/atom+xml", "text/xml" } )
public class FeedPlexusResource
    extends AbstractFeedPlexusResource
{
    public static final String FEED_KEY = "feedKey";

    @Override
    public Object getPayloadInstance()
    {
        // RO resource, no payload
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/feeds/{" + FEED_KEY + "}";
    }

    @Override
    protected String getChannelKey( Request request )
    {
        return (String) request.getAttributes().get( FEED_KEY );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/feeds/*", "authcBasic,perms[nexus:feeds]" );
    }

    /**
     * Returns the feed corresponding to the requested feed key. The existing feed keys (the list of feeds is not fixed,
     * plugins may contribute new feeds) should be queried by fetching the /feeds resource. Content negotiation is used
     * to figure out returned representation, but RSS (application/rss+xml MIME type) is the default one.
     * 
     * @param feedKey The feed key of the feed to be returned.
     * @param from The number of skipped entries (for paging).
     * @param count The count of entries to be returned (for paging).
     * @param r The repository ID to which a feed entries should be narrowed/filtered.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( "feedKey" ) }, queryParams = { @QueryParam( "from" ),
        @QueryParam( "count" ), @QueryParam( "r" ) }, output = String.class )
    protected SyndFeed getFeed( Context context, Request request, String channelKey, Integer from, Integer count,
                                Map<String, String> params )
        throws IOException, ComponentLookupException
    {
        SyndFeed feed = super.getFeed( context, request, channelKey, from, count, params );

        if ( feed.getLink() != null )
        {
            // full URLs should not be touched
            if ( !feed.getLink().startsWith( "http" ) )
            {
                if ( feed.getLink().startsWith( "/" ) )
                {
                    feed.setLink( feed.getLink().substring( 1 ) );
                }
                feed.setLink( createRootReference( request, feed.getLink() ).toString() );
            }
        }

        // TODO: A hack: creating "absolute" links
        for ( SyndEntry entry : (List<SyndEntry>) feed.getEntries() )
        {
            if ( entry.getLink() != null )
            {
                // full URLs should not be touched
                if ( !entry.getLink().startsWith( "http" ) )
                {
                    if ( entry.getLink().startsWith( "/" ) )
                    {
                        entry.setLink( entry.getLink().substring( 1 ) );
                    }
                    entry.setLink( createRootReference( request, entry.getLink() ).toString() );
                }
            }
        }

        return feed;
    }
}
