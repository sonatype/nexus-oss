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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.feeds.sources.FeedSource;
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
@Component( role = PlexusResource.class, hint = "TimelineFeedList" )
@Path( FeedsListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class FeedsListPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/feeds";
    
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
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:feeds]" );
    }

    /**
     * Get the list of feeds available from the nexus server.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = FeedListResourceResponse.class )
    public Object get( Context context, Request req, Response res, Variant variant )
        throws ResourceException
    {
        FeedListResourceResponse response = new FeedListResourceResponse();

        List<FeedSource> sources = feeds;

        for ( FeedSource source : sources )
        {
            FeedListResource resource = new FeedListResource();

            resource.setResourceURI( createChildReference( req, this, source.getFeedKey() ).toString() );

            resource.setName( source.getFeedName() );

            response.addData( resource );
        }

        return response;
    }

}
