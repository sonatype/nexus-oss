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

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.rest.model.UserToRoleResource;
import org.sonatype.security.rest.model.UserToRoleResourceRequest;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

@Component( role = PlexusResource.class, hint = "UserToRolePlexusResource" )
public class UserToRolePlexusResource
    extends AbstractUserPlexusResource
{

    public static final String SOURCE_ID_KEY = "sourceId";

    @Requirement
    private SecuritySystem securitySystem;

    public UserToRolePlexusResource()
    {
        this.setModifiable( true );
        this.setReadable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserToRoleResourceRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/user_to_roles/{" + SOURCE_ID_KEY + "}/{" + USER_ID_KEY + "}";
    }

    protected String getUserId( Request request )
    {
        return request.getAttributes().get( USER_ID_KEY ).toString();
    }

    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#put(org.restlet.Context, org.restlet.data.Request,
     * org.restlet.data.Response, java.lang.Object)
     */
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserToRoleResourceRequest mappingRequest = (UserToRoleResourceRequest) payload;

        if ( mappingRequest.getData() == null )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "User Role Mapping was not found in the Request." );
        }

        String userId = this.getUserId( request );
        String sourceId = this.getSourceId( request );

        // check if the user exists
        try
        {
            if ( this.securitySystem.getUser( userId, sourceId ) == null )
            {

            }
        }
        catch ( UserNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "User with id '" + userId + "' not found." );
        }
        catch ( NoSuchUserManager e )
        {
            this.getLogger().warn(  e.getMessage(), e );
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "User with id '" + userId + "' not found." );
        }

        // get the dto
        UserToRoleResource userToRole = mappingRequest.getData();

        SecurityUserRoleMapping roleMapping = this.restToSecurityModel( userToRole );

        if ( roleMapping.getRoles().size() == 0 )
        {
            throw new PlexusResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Configuration error.",
                getErrorResponse( "roles", "User requires one or more roles." ) );
        }
        // this seems a bit odd, but here is why the PUT does both create and update.
        // the users are stored in some LDAP server somewhere, but the role mapping is stored locally
        // this resource is trying to mimic the normal plexus resource, in the future we may add write
        // support for ldap users ( I don't know why), so this is setting us up for that.
        // or the way I look at it, we are updating the Users Roles... so its an update.
        try
        {
            // this will throw if we cannot find the user, in that case we will create one.
            getConfigurationManager().readUserRoleMapping( roleMapping.getUserId(), roleMapping.getSource() );
            getConfigurationManager().updateUserRoleMapping( roleMapping );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // do create
            try
            {
                getConfigurationManager().createUserRoleMapping( roleMapping );
            }
            catch ( InvalidConfigurationException e1 )
            {
                this.handleInvalidConfigurationException( e1 );
            }
        }
        catch ( InvalidConfigurationException e )
        {
            this.handleInvalidConfigurationException( e );
        }

        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#delete(org.restlet.Context,
     * org.restlet.data.Request, org.restlet.data.Response)
     */
    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        // get the userId
        String userId = this.getUserId( request );
        String source = this.getSourceId( request );

        try
        {
            getConfigurationManager().deleteUserRoleMapping( userId, source );
        }
        catch ( NoSuchRoleMappingException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "User with id '" + userId + "' not found." );
        }
    }

    private SecurityUserRoleMapping restToSecurityModel( UserToRoleResource restRoleMapping )
    {
        SecurityUserRoleMapping roleMapping = new SecurityUserRoleMapping();
        roleMapping.setUserId( restRoleMapping.getUserId() );
        roleMapping.setSource( restRoleMapping.getSource() );

        for ( String role : (List<String>) restRoleMapping.getRoles() )
        {
            roleMapping.addRole( role );
        }

        return roleMapping;
    }
}
