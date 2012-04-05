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
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.UserForgotPasswordRequest;
import org.sonatype.security.rest.model.UserForgotPasswordResource;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * REST resource to reset a users password. Default implementations will generate and email a user password to the user.
 * 
 * @author tstevens
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "UserForgotPasswordPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( UserForgotPasswordPlexusResource.RESOURCE_URI )
public class UserForgotPasswordPlexusResource
    extends AbstractUserPlexusResource
{
    public static final String RESOURCE_URI = "/users_forgotpw";

    public UserForgotPasswordPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserForgotPasswordRequest();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:usersforgotpw]" );
    }

    /**
     * Reset a user's password.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = UserForgotPasswordRequest.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserForgotPasswordRequest forgotPasswordRequest = (UserForgotPasswordRequest) payload;

        if ( forgotPasswordRequest != null )
        {
            UserForgotPasswordResource resource = forgotPasswordRequest.getData();

            try
            {
                if ( !isAnonymousUser( resource.getUserId(), request ) )
                {
                    getSecuritySystem().forgotPassword( resource.getUserId(), resource.getEmail() );

                    response.setStatus( Status.SUCCESS_ACCEPTED );
                }
                else
                {
                    response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot forget password" );

                    getLogger().debug( "Anonymous user forgot password is blocked" );
                }
            }
            catch ( UserNotFoundException e )
            {
                getLogger().debug( "Invalid Username", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid Username" );
            }
            catch ( InvalidConfigurationException e )
            {
                // this should never happen
                getLogger().warn( "Failed to set password!", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Failed to set password!." );
            }
        }
        // return null because the status is 202
        return null;
    }

}
