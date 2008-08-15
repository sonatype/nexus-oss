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
package org.sonatype.nexus.rest.users;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.NoSuchUserException;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceResponse;

public class UserResourceHandler
    extends AbstractUserResourceHandler
{
    private String userId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public UserResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.userId = getRequest().getAttributes().get( USER_ID_KEY ).toString();
    }

    protected String getUserId()
    {
        return this.userId;
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the User resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        UserResourceResponse response = new UserResourceResponse();

        try
        {
            response.setData( nexusToRestModel( getNexusSecurityConfiguration().readUser( getUserId() ) ) );

            return serialize( variant, response );
        }
        catch ( NoSuchUserException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

            return null;
        }
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a user.
     */
    public void put( Representation representation )
    {
        UserResourceRequest request = (UserResourceRequest) deserialize( new UserResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            UserResource resource = request.getData();

            try
            {
                CUser user = restToNexusModel(
                    getNexusSecurityConfiguration().readUser( resource.getUserId() ),
                    resource );

                getNexusSecurityConfiguration().updateUser( user );

                UserResourceResponse response = new UserResourceResponse();

                response.setData( request.getData() );

                getResponse().setEntity( serialize( representation, response ) );
            }
            catch ( NoSuchUserException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e, representation );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a user.
     */
    public void delete()
    {
        try
        {
            if ( !isAnonymousUser( getUserId() ) )
            {
                getNexusSecurityConfiguration().deleteUser( getUserId() );
            }
            else
            {
                getResponse()
                    .setEntity(
                        new StringRepresentation(
                            "The user with user ID ["
                                + getUserId()
                                + "] cannot be deleted, since it is marked user used for Anonymous access in Server Administration. To delete this user, disable anonymous access or, change the anonymous username and password to another valid values!",
                            MediaType.TEXT_HTML ) );

                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );

                getLogger()
                    .log(
                        Level.INFO,
                        "Anonymous user cannot be deleted! Unset the Allow Anonymous access first in Server Administration!" );
            }
        }
        catch ( NoSuchUserException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );
        }
    }
}
