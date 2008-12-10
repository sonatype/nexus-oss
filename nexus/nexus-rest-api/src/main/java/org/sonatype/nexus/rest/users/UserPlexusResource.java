/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

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
        return new PathProtectionDescriptor( "/users/*", "authcBasic,perms[nexus:users]" );
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
            result.setData( nexusToRestModel( getNexusSecurity().readUser( getUserId( request ) ), request ) );

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
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, this.getNexusErrorResponse(
                    "*",
                    "Updating a users password using this URI is not allowed." ) );
            }

            try
            {
                SecurityUser user = restToNexusModel( getNexusSecurity().readUser( resource.getUserId() ), resource );

                getNexusSecurity().updateUser( user );

                result = new UserResourceResponse();

                result.setData( resourceRequest.getData() );
                
                result.getData().setUserManaged( !user.isReadOnly() );

                result.getData().setResourceURI( createChildReference( request, resource.getUserId() ).toString() );

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
            if ( !isAnonymousUser( getUserId( request ), request ) )
            {
                getNexusSecurity().deleteUser( getUserId( request ) );

                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            else
            {
                String error = "The user with user ID ["
                    + getUserId( request )
                    + "] cannot be deleted, since it is marked user used for Anonymous access in Server Administration. To delete this user, disable anonymous access or, change the anonymous username and password to another valid values!";
                // i changed this to be an errorResponse, TODO: check if this is valid
                // response.setEntity( new StringRepresentation( error, MediaType.TEXT_HTML ) );

                getLogger()
                    .info(
                        "Anonymous user cannot be deleted! Unset the Allow Anonymous access first in Server Administration!" );

                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, getNexusErrorResponse( "*", error ) );
            }
        }
        catch ( NoSuchUserException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
