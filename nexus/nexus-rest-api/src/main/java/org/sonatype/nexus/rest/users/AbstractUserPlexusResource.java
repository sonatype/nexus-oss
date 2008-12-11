/**
 * Sonatype Nexus™ [Open Source Version].
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

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

public abstract class AbstractUserPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String USER_ID_KEY = "userId";

    public static final String USER_EMAIL_KEY = "email";

    private static final String ROLE_VALIDATION_ERROR = "The user cannot have zero roles!";

    @Requirement
    private NexusSecurity nexusSecurity;

    protected NexusSecurity getNexusSecurity()
    {
        return nexusSecurity;
    }

    protected boolean validateFields( UserResource resource, Representation representation )
        throws PlexusResourceException
    {
        if ( resource.getRoles() == null || resource.getRoles().size() == 0 )
        {
            getLogger().info( "The userId (" + resource.getUserId() + ") cannot have 0 roles!" );

            throw new PlexusResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                ROLE_VALIDATION_ERROR,
                getNexusErrorResponse( "users", ROLE_VALIDATION_ERROR ) );
        }

        return true;
    }

    protected UserResource nexusToRestModel( SecurityUser user, Request request )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmail() );
        resource.setName( user.getName() );
        resource.setStatus( user.getStatus() );
        resource.setUserId( user.getId() );
        resource.setResourceURI( this.createChildReference( request, resource.getUserId() ).toString() );
        resource.setUserManaged( !user.isReadOnly() );

        for ( String roleId : (List<String>) user.getRoles() )
        {
            resource.addRole( roleId );
        }

        return resource;
    }

    protected SecurityUser restToNexusModel( SecurityUser user, UserResource resource )
    {
        if ( user == null )
        {
            user = new SecurityUser();
        }

        user.setEmail( resource.getEmail() );
        user.setName( resource.getName() );
        user.setStatus( resource.getStatus() );
        user.setId( resource.getUserId() );

        user.getRoles().clear();
        for ( String roleId : (List<String>) resource.getRoles() )
        {
            user.addRole( roleId );
        }

        return user;
    }

    protected boolean isAnonymousUser( String username, Request request )
        throws ResourceException
    {
        return getNexus().isAnonymousAccessEnabled() && getNexus().getAnonymousUsername().equals( username );
    }

}
