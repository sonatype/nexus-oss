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
import org.sonatype.security.email.NoSuchEmailException;
import org.sonatype.security.usermanagement.UserNotFoundException;

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
        return new PathProtectionDescriptor( "/users_forgotid/*", "authcBasic,perms[security:usersforgotid]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String email = request.getAttributes().get( USER_EMAIL_KEY ).toString();

        try
        {
            getSecuritySystem().forgotUsername( email );

            response.setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( UserNotFoundException e )
        {
            getLogger().debug( "Invalid email received: " + email, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Email address not found!" );
        }
        // don't return anything because we are setting the status to 202
        return null;
    }

}
