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
import java.util.logging.Level;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A base Resource class for RSS feeds to be published over restlet.org. It overrides the getRepresentation() method,
 * the user only needs to implement getChannel() method of ChannelSource interface.
 * 
 * @author cstamas
 */
public abstract class AbstractFeedResourceHandler
    extends AbstractNexusResourceHandler
{

    private static final String RSS_2_0 = "rss_2.0";

    private static final String ATOM_1_0 = "atom_1.0";

    private static final String DEFAULT_FEED_TYPE = RSS_2_0;

    public AbstractFeedResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().add( new Variant( FeedRepresentation.RSS_MEDIA_TYPE ) );

        getVariants().add( new Variant( FeedRepresentation.ATOM_MEDIA_TYPE ) );

        getVariants().add( new Variant( MediaType.APPLICATION_XML ) );

        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        
        getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
    }

    public Representation getRepresentationHandler( Variant variant )
    {
        try
        {
            if ( !MediaType.APPLICATION_JSON.equals( variant.getMediaType(), true ) )
            {
                SyndFeed feed = getFeed( getChannelKey() );

                if ( FeedRepresentation.ATOM_MEDIA_TYPE.equals( variant.getMediaType(), true ) )
                {
                    feed.setFeedType( ATOM_1_0 );
                }
                else if ( FeedRepresentation.RSS_MEDIA_TYPE.equals( variant.getMediaType(), true ) )
                {
                    feed.setFeedType( RSS_2_0 );
                }
                else
                {
                    feed.setFeedType( DEFAULT_FEED_TYPE );
                }

                feed.setLink( getRequest().getResourceRef().toString() );

                FeedRepresentation representation = new FeedRepresentation( variant.getMediaType(), feed );

                return representation;
            }
            else
            {
                getResponse().setStatus( Status.SERVER_ERROR_NOT_IMPLEMENTED, "Not implemented." );
                
                return null;
            }
        }
        catch ( ComponentLookupException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Channel source not found!" );

            getLogger().log( Level.SEVERE, "Got ComponentLookupException!", e );

            return null;
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );

            return null;
        }

    }

    protected SyndFeed getFeed( String channelKey )
        throws IOException,
            ComponentLookupException
    {
        FeedSource src = (FeedSource) getPlexusContainer().lookup( FeedSource.ROLE, channelKey );

        return src.getFeed();
    }

    protected abstract String getChannelKey();

}
