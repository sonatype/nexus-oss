/**
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * REST resource to email the user his/her user Id.
 * 
 * @author tstevens
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "UserForgotIdPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( UserForgotIdPlexusResource.RESOURCE_URI )
public class UserForgotIdPlexusResource
    extends AbstractUserPlexusResource
{

    public static final String RESOURCE_URI = "/users_forgotid/{" + USER_EMAIL_KEY + "}";

    public UserForgotIdPlexusResource()
    {
        this.setModifiable( true );
    }

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

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users_forgotid/*", "authcBasic,perms[security:usersforgotid]" );
    }

    /**
     * Email user his/her user Id.
     * 
     * @param email The email address of the user.
     */
    @Override
    @POST
    @ResourceMethodSignature( pathParams = { @PathParam( value = "email" ) } )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        final String email = getRequestAttribute( request, USER_EMAIL_KEY );

        try
        {
            getSecuritySystem().forgotUsername( email );

            response.setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( UserNotFoundException e )
        {
            getLogger().debug( "Invalid email received: " + email, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Email address not found!" );
        }
        // don't return anything because we are setting the status to 202
        return null;
    }

}
