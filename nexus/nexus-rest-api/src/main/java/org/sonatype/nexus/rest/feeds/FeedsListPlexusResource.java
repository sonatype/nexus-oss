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

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.FeedListResource;
import org.sonatype.nexus.rest.model.FeedListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource that lists existing feeds.
 * 
 * @author cstamas
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "feedList" )
public class FeedsListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement( role = FeedSource.class )
    private List<FeedSource> feeds;

    @Override
    public Object getPayloadInstance()
    {
        // RO resource, no payload
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/feeds";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:feeds]" );
    }

    @Override
    public Object get( Context context, Request req, Response res, Variant variant )
        throws ResourceException
    {
        FeedListResourceResponse response = new FeedListResourceResponse();

        List<FeedSource> sources = feeds;

        for ( FeedSource source : sources )
        {
            FeedListResource resource = new FeedListResource();

            resource.setResourceURI( createChildReference( req, source.getFeedKey() ).toString() );

            resource.setName( source.getFeedName() );

            response.addData( resource );
        }

        return response;
    }

}
