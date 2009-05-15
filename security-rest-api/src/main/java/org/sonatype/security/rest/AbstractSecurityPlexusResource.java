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
package org.sonatype.security.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationContext;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserStatus;

public abstract class AbstractSecurityPlexusResource
    extends AbstractPlexusResource
{

    @Requirement
    private SecuritySystem securitySystem;

    protected static final String DEFAULT_SOURCE = "default";

    @Requirement
    protected ReferenceFactory referenceFactory;

    protected SecuritySystem getSecuritySystem()
    {
        return securitySystem;
    }

    protected ErrorResponse getErrorResponse( String id, String msg )
    {
        ErrorResponse ner = new ErrorResponse();
        ErrorMessage ne = new ErrorMessage();
        ne.setId( id );
        ne.setMsg( msg );
        ner.addError( ne );
        return ner;
    }

    protected void handleInvalidConfigurationException( InvalidConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        ErrorResponse errorResponse;

        ValidationResponse<?> vr = e.getValidationResponse();

        if ( vr != null && vr.getValidationErrors().size() > 0 )
        {
            ValidationMessage vm = vr.getValidationErrors().get( 0 );
            errorResponse = getErrorResponse( vm.getKey(), vm.getShortMessage() );
        }
        else
        {
            errorResponse = getErrorResponse( "*", e.getMessage() );
        }

        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", errorResponse );
    }

    protected UserResource securityToRestModel( User user, Request request )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmailAddress() );
        resource.setName( user.getName() );
        resource.setStatus( user.getStatus().name() );
        resource.setUserId( user.getUserId() );
        resource.setResourceURI( this.createChildReference( request, resource.getUserId() ).toString() );
        resource.setUserManaged( !user.isReadOnly() );

        for ( RoleIdentifier role : user.getRoles() )
        {
            resource.addRole( role.getRoleId() );
        }

        return resource;
    }

    protected User restToSecurityModel( User user, UserResource resource )
        throws InvalidConfigurationException
    {
        if ( user == null )
        {
            user = new DefaultUser();
        }

        // validate users Status, converting to an ENUM throws an exception, so we need to explicitly check it
        this.checkUsersStatus( resource.getStatus() );

        user.setEmailAddress( resource.getEmail() );
        user.setName( resource.getName() );
        user.setStatus( UserStatus.valueOf( resource.getStatus() ) );
        user.setUserId( resource.getUserId() );

        // set the users source
        user.setSource( DEFAULT_SOURCE );

        Set<RoleIdentifier> roles = new HashSet<RoleIdentifier>();
        for ( String roleId : (List<String>) resource.getRoles() )
        {
            roles.add( new RoleIdentifier( DEFAULT_SOURCE, roleId ) );
        }

        user.setRoles( roles );

        return user;
    }

    protected PlexusUserResource securityToRestModel( User user )
    {
        PlexusUserResource resource = new PlexusUserResource();

        resource.setUserId( user.getUserId() );
        resource.setSource( user.getSource() );
        resource.setName( user.getName() );
        resource.setEmail( user.getEmailAddress() );

        for ( RoleIdentifier role : user.getRoles() )
        {
            resource.addRole( this.securityToRestModel( role ) );
        }

        return resource;
    }

    protected PlexusRoleResource securityToRestModel( Role role )
    {
        PlexusRoleResource roleResource = new PlexusRoleResource();
        roleResource.setRoleId( role.getRoleId() );
        roleResource.setName( role.getName() );
        roleResource.setSource( role.getSource() );

        return roleResource;
    }

    // TODO: come back to this, we need to change the PlexusRoleResource
    protected PlexusRoleResource securityToRestModel( RoleIdentifier role )
    {
        // TODO: we need to get the name of the Role, this could be slow if a user has 100 roles from an external realm
        // (JDBC, LDAP), and no caching, we will need to come back to this.
        // We shouldn't be looking up the role name here anyway... this should get pushed up to the SecuritySystem.
        SecuritySystem securitySystem = this.getSecuritySystem();

        String roleName = role.getRoleId();

        // if this blows up we don't need to stop, we already have the ID, and really this should never happen because
        // we just looked up this info
        
        try
        {
            roleName = securitySystem.getAuthorizationManager( role.getSource() ).getRole( role.getRoleId() ).getName();
        }
        catch ( NoSuchRoleException e )
        {
            this.getLogger().error( "Failed to lookup the users Role: "+ role.getRoleId() +" source: "+ role.getSource() +" but the user has this role.", e);
        }
        catch ( NoSuchAuthorizationManager e )
        {
            this.getLogger().error( "Failed to lookup the users Role: "+ role.getRoleId() +" source: "+ role.getSource() +" but the user has this role.", e);
        }
        
        PlexusRoleResource roleResource = new PlexusRoleResource();
        roleResource.setRoleId( role.getRoleId() );
        roleResource.setName( roleName );
        roleResource.setSource( role.getSource() );

        return roleResource;
    }

    protected Reference getContextRoot( Request request )
    {
        return this.referenceFactory.getContextRoot( request );
    }

    protected Reference createChildReference( Request request, String childPath )
    {
        return this.referenceFactory.createChildReference( request, childPath );
    }

    protected void checkUsersStatus( String status )
        throws InvalidConfigurationException
    {
        boolean found = false;
        for ( UserStatus userStatus : UserStatus.values() )
        {
            if ( userStatus.name().equals( status ) )
            {
                found = true;
            }
        }

        if ( !found )
        {
            ValidationResponse<ValidationContext> response = new ValidationResponse<ValidationContext>();
            response.addValidationError( new ValidationMessage( "status", "Users status is not valid." ) );
            throw new InvalidConfigurationException( response );
        }
    }

}
