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
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserResetPlexusResource" )
public class UserResetPlexusResource
    extends AbstractUserPlexusResource
{

    public UserResetPlexusResource()
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
        return "/users_reset/{" + USER_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users_reset/*", "authcBasic,perms[security:usersreset]" );
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {

        String userId = request.getAttributes().get( USER_ID_KEY ).toString();

        try
        {
            if ( !isAnonymousUser( userId, request ) )
            {
                getSecuritySystem().resetPassword( userId );

                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            else
            {
                getLogger().debug( "Anonymous user password reset is blocked!" );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot reset password!" );
            }
        }
        catch ( UserNotFoundException e )
        {
            getLogger().debug( "Invalid userid: " + userId, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "User ID not found!" );
        }
    }

}
