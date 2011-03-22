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
package org.sonatype.security.rest.authentication;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.security.rest.model.AuthenticationLoginResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author cstamas
 */
public abstract class AbstractLoginPlexusResource
    extends AbstractUIPermissionCalculatingPlexusResource
{
    public static final String RESOURCE_URI = "/authentication/login";
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        AuthenticationLoginResource resource = new AuthenticationLoginResource();

        resource.setClientPermissions( getClientPermissionsForCurrentUser( request ) );

        AuthenticationLoginResourceResponse result = new AuthenticationLoginResourceResponse();

        String username = resource.getClientPermissions().getLoggedInUsername();

        if ( StringUtils.isNotEmpty( username ) )
        {
            // look up the realm of the user
            try
            {
                User user = this.getSecuritySystem().getUser( username );
                String source = ( user != null ) ? user.getSource() : null;
                resource.getClientPermissions().setLoggedInUserSource( source );
            }
            catch ( UserNotFoundException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }

        result.setData( resource );

        return result;
    }

}
