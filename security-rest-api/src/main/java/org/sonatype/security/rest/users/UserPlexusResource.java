/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.SecurityUtils;
import org.jsecurity.subject.Subject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserPlexusResource" )
public class UserPlexusResource
    extends AbstractUserPlexusResource
{

    public UserPlexusResource()
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
        return "/users/{" + USER_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users/*", "authcBasic,perms[security:users]" );
    }

    protected String getUserId( Request request )
    {
        return request.getAttributes().get( USER_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {

        UserResourceResponse result = new UserResourceResponse();

        try
        {
            result.setData( securityToRestModel( getPlexusSecurity().readUser( getUserId( request ) ), request ) );

        }
        catch ( NoSuchUserException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserResourceRequest resourceRequest = (UserResourceRequest) payload;
        UserResourceResponse result = null;

        if ( resourceRequest != null )
        {
            UserResource resource = resourceRequest.getData();

            // the password can not be set on update, The only way to set a password is using the users_setpw resource
            if ( StringUtils.isNotEmpty( resource.getPassword() ) )
            {
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, this.getErrorResponse(
                    "*",
                    "Updating a users password using this URI is not allowed." ) );
            }

            try
            {
                SecurityUser user = restToSecurityModel( getPlexusSecurity().readUser( resource.getUserId() ), resource );

                validateUserContainment( user );
                
                getPlexusSecurity().updateUser( user );

                result = new UserResourceResponse();

                result.setData( resourceRequest.getData() );
                
                result.getData().setUserManaged( !user.isReadOnly() );

                result.getData().setResourceURI( createChildReference( request, this, resource.getUserId() ).toString() );

            }
            catch ( NoSuchUserException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            // not allowed to delete system Anonymous user
            if ( isAnonymousUser( getUserId( request ), request ) )
            {
                String error = "The user with user ID ["
                    + getUserId( request )
                    + "] cannot be deleted, since it is marked user used for Anonymous access in Server Administration. To delete this user, disable anonymous access or, change the anonymous username and password to another valid values!";

                getLogger()
                    .info(
                        "Anonymous user cannot be deleted! Unset the Allow Anonymous access first in Server Administration!" );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, error );
            }

            // not allowed to delete the current user himself
            if ( isCurrentUser( request ) )
            {
                String error = "The user with user ID [" + getUserId( request )
                    + "] cannot be deleted, as that is the user currently logged into the application.";

                getLogger().info(
                    "The user with user ID [" + getUserId( request )
                        + "] cannot be deleted, as that is the user currently logged into the application." );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, error );
            }

            getPlexusSecurity().deleteUser( getUserId( request ) );

            response.setStatus( Status.SUCCESS_NO_CONTENT );

        }
        catch ( NoSuchUserException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

    protected boolean isCurrentUser( Request request )
    {
        Subject subject = SecurityUtils.getSubject();
        if ( subject == null )
        {
            return false; // not the current user because there is no current user
        }

        return subject.getPrincipal().equals( getUserId( request ) );
    }

}
