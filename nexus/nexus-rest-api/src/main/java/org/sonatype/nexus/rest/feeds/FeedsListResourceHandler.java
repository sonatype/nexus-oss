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
import java.util.logging.Level;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.FeedListResource;
import org.sonatype.nexus.rest.model.FeedListResourceResponse;

/**
 * A resource that lists existing feeds.
 * 
 * @author cstamas
 */
public class FeedsListResourceHandler
    extends AbstractNexusResourceHandler
{

    public FeedsListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        FeedListResourceResponse response = new FeedListResourceResponse();

        FeedListResource resource = new FeedListResource();

        try
        {
            List<FeedSource> sources = (List<FeedSource>) getPlexusContainer().lookupList( FeedSource.ROLE );

            for ( FeedSource source : sources )
            {
                resource = new FeedListResource();

                resource.setResourceURI( calculateSubReference( source.getFeedKey() ).toString() );

                resource.setName( source.getFeedName() );

                response.addData( resource );
            }

            return serialize( variant, response );
        }
        catch ( ComponentLookupException e )
        {
            getLogger().log( Level.SEVERE, "Got ComponentLookupException during ChannelSource lookup.", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
        }
    }
}
