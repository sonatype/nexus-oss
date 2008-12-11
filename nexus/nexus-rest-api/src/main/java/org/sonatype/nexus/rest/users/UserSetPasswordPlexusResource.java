/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "UserSetPasswordPlexusResource" )
public class UserSetPasswordPlexusResource
    extends AbstractUserPlexusResource
{

    public UserSetPasswordPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserChangePasswordRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/users_setpw";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:userssetpw]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserChangePasswordRequest changePasswordRequest = (UserChangePasswordRequest) payload;

        if ( changePasswordRequest != null )
        {
            UserChangePasswordResource resource = changePasswordRequest.getData();

            try
            {
                if ( !isAnonymousUser( resource.getUserId(), request ) )
                {
                    getNexusSecurity().changePassword( resource.getUserId(), resource.getNewPassword() );

                    response.setStatus( Status.SUCCESS_NO_CONTENT );
                }
                else
                {
                    response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot change password!" );

                    getLogger().debug( "Anonymous user password change is blocked!" );
                }
            }
            catch ( NoSuchUserException e )
            {
                getLogger().debug( "Invalid user ID!", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid credentials supplied." );

            }

        }
        // don't return anything because the status is a 204
        return null;
    }
}
