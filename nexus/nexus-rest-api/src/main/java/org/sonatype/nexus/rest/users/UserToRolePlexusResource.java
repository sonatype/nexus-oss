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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.nexus.rest.model.UserToRoleResource;
import org.sonatype.nexus.rest.model.UserToRoleResourceRequest;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

@Component( role = PlexusResource.class, hint = "UserToRolePlexusResource" )
public class UserToRolePlexusResource
    extends AbstractUserPlexusResource
{

    public static final String SOURCE_ID_KEY = "sourceId";

    @Requirement(hint="additinalRoles")
    private PlexusUserManager userManager;

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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:users]" );
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
     * 
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#put(org.restlet.Context, org.restlet.data.Request,
     *      org.restlet.data.Response, java.lang.Object)
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
        // FIXME: add source
        if ( this.userManager.getUser( userId, sourceId ) == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "User with id '" + userId + "' not found." );
        }

        // get the dto
        UserToRoleResource userToRole = mappingRequest.getData();

        SecurityUserRoleMapping roleMapping = this.restToNexusModel( userToRole );
        
        if ( roleMapping.getRoles().size() == 0 )
        {
            throw new PlexusResourceException( 
                Status.CLIENT_ERROR_BAD_REQUEST, 
                "Configuration error.", 
                getNexusErrorResponse( 
                    "roles", 
                    "User requires one or more roles." ) );
        }
        // this seems a bit odd, but here is why the PUT does both create and update.
        // the users are stored in some LDAP server somewhere, but the role mapping is stored locally
        // this resource is trying to mimic the normal nexus resource, in the future we may add write
        // support for ldap users ( I don't know why), so this is setting us up for that.
        // or the way I look at it, we are updating the Users Roles... so its an update.
        try
        {
            // this will throw if we cannot find the user, in that case we will create one.
            getNexusSecurity().readUserRoleMapping( roleMapping.getUserId(), roleMapping.getSource() );
            getNexusSecurity().updateUserRoleMapping( roleMapping );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // do create
            try
            {
                getNexusSecurity().createUserRoleMapping( roleMapping );
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
     * 
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#delete(org.restlet.Context,
     *      org.restlet.data.Request, org.restlet.data.Response)
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
            getNexusSecurity().deleteUserRoleMapping( userId, source );
        }
        catch ( NoSuchRoleMappingException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "User with id '" + userId + "' not found." );
        }
    }

    private SecurityUserRoleMapping restToNexusModel( UserToRoleResource restRoleMapping )
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
