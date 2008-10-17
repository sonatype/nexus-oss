/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A base Resource class for RSS feeds to be published over restlet.org. It overrides the get() method, the user only
 * needs to implement getChannel() method of ChannelSource interface.
 * 
 * @author cstamas
 * @author dip
 */
public abstract class AbstractFeedPlexusResource
    extends AbstractNexusPlexusResource
{
    private static final String RSS_2_0 = "rss_2.0";

    private static final String ATOM_1_0 = "atom_1.0";

    @Requirement( role = FeedSource.class )
    private Map<String, FeedSource> feeds;

    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        // the default resource implementation returns
        // application/xml and application/json
        result.add( new Variant( FeedRepresentation.RSS_MEDIA_TYPE ) );
        result.add( new Variant( FeedRepresentation.ATOM_MEDIA_TYPE ) );
        result.add( new Variant( MediaType.TEXT_XML ) );

        return result;
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MediaType mediaType = variant.getMediaType();

        Form params = request.getResourceRef().getQueryAsForm();

        Long from = null;
        Integer count = null;

        try
        {
            if ( params.getFirstValue( "from" ) != null )
            {
                from = Long.valueOf( params.getFirstValue( "from" ) );
            }

            if ( params.getFirstValue( "count" ) != null )
            {
                count = Integer.valueOf( params.getFirstValue( "count" ) );
            }
        }
        catch ( NumberFormatException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "The 'from' and 'count' parameters must be numbers!",
                e );
        }

        try
        {
            if ( !MediaType.APPLICATION_JSON.equals( mediaType, true ) )
            {
                SyndFeed feed = getFeed( context, request, getChannelKey( request ), from, count );

                if ( FeedRepresentation.ATOM_MEDIA_TYPE.equals( mediaType, true ) )
                {
                    feed.setFeedType( ATOM_1_0 );
                }
                else if ( FeedRepresentation.RSS_MEDIA_TYPE.equals( mediaType, true ) )
                {
                    feed.setFeedType( RSS_2_0 );
                }
                else
                {
                    feed.setFeedType( RSS_2_0 );

                    mediaType = FeedRepresentation.RSS_MEDIA_TYPE;
                }

                feed.setLink( request.getResourceRef().toString() );

                FeedRepresentation representation = new FeedRepresentation( mediaType, feed );

                return representation;
            }
            else
            {
                throw new ResourceException( Status.SERVER_ERROR_NOT_IMPLEMENTED, "Not implemented." );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Channel source not found!", e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    protected SyndFeed getFeed( Context context, Request request, String channelKey, Long from, Integer count )
        throws IOException,
            ComponentLookupException
    {
        FeedSource src = feeds.get( channelKey );

        return src.getFeed( from, count );
    }

    protected abstract String getChannelKey( Request request );
}
