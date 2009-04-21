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

import java.util.List;

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.UserResource;

public abstract class AbstractUserPlexusResource
    extends AbstractSecurityPlexusResource
{
    public static final String USER_ID_KEY = "userId";

    public static final String USER_EMAIL_KEY = "email";

    private static final String ROLE_VALIDATION_ERROR = "The user cannot have zero roles!";

    protected boolean validateFields( UserResource resource, Representation representation )
        throws PlexusResourceException
    {
        if ( resource.getRoles() == null || resource.getRoles().size() == 0 )
        {
            getLogger().info( "The userId (" + resource.getUserId() + ") cannot have 0 roles!" );

            throw new PlexusResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                ROLE_VALIDATION_ERROR,
                getErrorResponse( "users", ROLE_VALIDATION_ERROR ) );
        }

        return true;
    }

    protected boolean isAnonymousUser( String username, Request request )
        throws ResourceException
    {
        return getPlexusSecurity().isAnonymousAccessEnabled() && getPlexusSecurity().getAnonymousUsername().equals( username );
    }

    protected void validateUserContainment( SecurityUser user )
        throws ResourceException
    {
        if ( user.getRoles().size() == 0 )
        {
            throw new PlexusResourceException( 
                Status.CLIENT_ERROR_BAD_REQUEST, 
                "Configuration error.", 
                getErrorResponse( 
                    "roles", 
                    "User requires one or more roles." ) );
        }
    }
}
