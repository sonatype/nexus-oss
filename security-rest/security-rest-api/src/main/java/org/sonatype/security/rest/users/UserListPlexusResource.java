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
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;
import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

/**
 * REST resource for listing and creating users.
 * 
 * @author tstevens
 */
@Singleton
@Typed( PlexusResource.class )
@Named( "UserListPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( UserListPlexusResource.RESOURCE_URI )
public class UserListPlexusResource
    extends AbstractUserPlexusResource
{

    public static final String RESOURCE_URI = "/users";

    public UserListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:users]" );
    }

    /**
     * Retrieves the list of users.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = UserListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        UserListResourceResponse result = new UserListResourceResponse();

        for ( User user : getSecuritySystem().searchUsers( new UserSearchCriteria( null, null, DEFAULT_SOURCE ) ) )
        {
            UserResource res = securityToRestModel( user, request, true );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    /**
     * Creates a user.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = UserResourceRequest.class, output = UserResourceResponse.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserResourceRequest requestResource = (UserResourceRequest) payload;
        UserResourceResponse result = null;

        if ( requestResource != null )
        {
            UserResource resource = requestResource.getData();

            try
            {
                User user = restToSecurityModel( null, resource );

                validateUserContainment( user );

                String password = resource.getPassword();
                getSecuritySystem().addUser( user, password );

                result = new UserResourceResponse();

                // Update the status, as that may have changed
                resource.setStatus( user.getStatus().name() );

                resource.setResourceURI( createChildReference( request, resource.getUserId() ).toString() );

                result.setData( resource );

            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
            catch ( NoSuchUserManagerException e )
            {
                ErrorResponse errorResponse = getErrorResponse( "*", e.getMessage() );
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Unable to create user.",
                                                   errorResponse );
            }
        }
        return result;
    }

}
