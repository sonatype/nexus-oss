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

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.usermanagement.User;

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
        return getSecuritySystem().isAnonymousAccessEnabled() && getSecuritySystem().getAnonymousUsername().equals( username );
    }

    protected void validateUserContainment( User user )
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
