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
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.realms.tools.InvalidConfigurationException;
import org.sonatype.security.realms.tools.dao.SecurityUser;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserListPlexusResource" )
public class UserListPlexusResource
    extends AbstractUserPlexusResource
{

    public UserListPlexusResource()
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
        return "/users";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:users]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        UserListResourceResponse result = new UserListResourceResponse();

        for ( SecurityUser user : getPlexusSecurity().listUsers() )
        {
            UserResource res = securityToRestModel( user, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserResourceRequest requestResource = (UserResourceRequest) payload;
        UserResourceResponse result = null;

        if ( requestResource != null )
        {
            UserResource resource = requestResource.getData();

            SecurityUser user = restToSecurityModel( null, resource );

            try
            {
                validateUserContainment( user );
                
                String password = resource.getPassword();
                getPlexusSecurity().createUser( user, password );

                result = new UserResourceResponse();

                // Update the status, as that may have changed
                resource.setStatus( user.getStatus() );

                resource.setResourceURI( createChildReference( request, this, resource.getUserId() ).toString() );
                
                resource.setUserManaged( true );

                result.setData( resource );

            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return result;
    }

}
