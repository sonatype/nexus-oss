/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.nexus.jsecurity.NoSuchEmailException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserForgotIdPlexusResource" )
public class UserForgotIdPlexusResource
    extends AbstractUserPlexusResource
{

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
        return "/users_forgotid/{" + USER_EMAIL_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users_forgotid/*", "authcBasic,perms[nexus:usersforgotid]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String email = request.getAttributes().get( USER_EMAIL_KEY ).toString();
        
        if ( isAnonymousEmail( email ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Cannot recover the username for anonymous users" );
        }
        
        try
        {
            getNexusSecurity().forgotUsername( email );

            response.setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( NoSuchEmailException e )
        {
            getLogger().debug( "Invalid email received: " + email, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Email address not found!" );

        }
        // don't return anything because we are setting the status to 202
        return null;
    }
    
    private boolean isAnonymousEmail( String email )
    {
        String anonymousEmail = "";

        try
        {
            anonymousEmail = getNexusSecurity().readUser( getNexus().getAnonymousUsername() ).getEmail();
        }
        catch ( NoSuchUserException e )
        {
            getLogger().warn( "Could not read anonymous user with id '" + getNexus().getAnonymousUsername() + "'.", e );

            return false;
        }

        return anonymousEmail.equals( email );
    }

}
