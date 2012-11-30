/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.users;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.inject.BeanEntry;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.PlexusComponentListResource;
import org.sonatype.security.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.security.usermanagement.UserManager;

/**
 * REST resource for listing the types of {@link UserManager} that are configured in the system. Each
 * {@link UserManager} manages a list of users from a spesific source.
 * 
 * @author bdemers
 */
@Singleton
@Typed( PlexusResource.class )
@Named( "UserLocatorComponentListPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( UserLocatorComponentListPlexusResource.RESOURCE_URI )
public class UserLocatorComponentListPlexusResource
    extends AbstractPlexusResource
{
    public static final String RESOURCE_URI = "/components/userLocators";

    @Inject
    private Iterable<BeanEntry<Named, UserManager>> userManagers;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:componentsuserlocatortypes]" );
    }

    /**
     * Retrieves a list of User Managers.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PlexusComponentListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusComponentListResourceResponse result = new PlexusComponentListResourceResponse();

        if ( userManagers != null )
        {
            for ( BeanEntry<Named, UserManager> entry : userManagers )
            {
                String hint = entry.getKey().value();
                String description = entry.getDescription();

                PlexusComponentListResource resource = new PlexusComponentListResource();
                resource.setRoleHint( hint );
                resource.setDescription( ( StringUtils.isNotEmpty( description ) ) ? description : hint );

                // add it to the collection
                result.addData( resource );
            }
        }

        if ( result.getData().isEmpty() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        return result;
    }
}
