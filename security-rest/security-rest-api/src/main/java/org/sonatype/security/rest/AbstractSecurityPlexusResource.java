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
package org.sonatype.security.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserStatus;

/**
 * Base class of SecurityPlexusResources. Contains error handling util methods and conversion between DTO and
 * persistence model.
 * 
 * @author bdemers
 */
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public abstract class AbstractSecurityPlexusResource
    extends AbstractPlexusResource
{

    @Inject
    private SecuritySystem securitySystem;

    protected static final String DEFAULT_SOURCE = "default";

    @Inject
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

        ValidationResponse vr = e.getValidationResponse();

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

    protected UserResource securityToRestModel( User user, Request request, boolean appendResourceId )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmailAddress() );
        resource.setFirstName( user.getFirstName() );
        resource.setLastName( user.getLastName() );
        resource.setStatus( user.getStatus().name() );
        resource.setUserId( user.getUserId() );

        String resourceId = "";
        if ( appendResourceId )
        {
            resourceId = resource.getUserId();
        }
        resource.setResourceURI( this.createChildReference( request, resourceId ).toString() );

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
        user.setFirstName( resource.getFirstName() );
        user.setLastName( resource.getLastName() );
        user.setStatus( UserStatus.valueOf( resource.getStatus() ) );
        user.setUserId( resource.getUserId() );

        // set the users source
        user.setSource( DEFAULT_SOURCE );

        Set<RoleIdentifier> roles = new HashSet<RoleIdentifier>();
        for ( String roleId : resource.getRoles() )
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
        resource.setFirstName( user.getFirstName() );
        resource.setLastName( user.getLastName() );
        resource.setEmail( user.getEmailAddress() );

        for ( RoleIdentifier role : user.getRoles() )
        {
            resource.addRole( this.securityToRestModel( role ) );
        }

        return resource;
    }

    protected PlexusRoleResource securityToRestModel( Role role )
    {
        if ( role == null )
        {
            return null;
        }

        PlexusRoleResource roleResource = new PlexusRoleResource();
        roleResource.setRoleId( role.getRoleId() );
        roleResource.setName( role.getName() );
        roleResource.setSource( role.getSource() );

        return roleResource;
    }

    protected List<PlexusUserResource> securityToRestModel( Set<User> users )
    {
        List<PlexusUserResource> restUsersList = new ArrayList<PlexusUserResource>();

        for ( User user : users )
        {
            restUsersList.add( securityToRestModel( user ) );
        }
        return restUsersList;
    }

    // TODO: come back to this, we need to change the PlexusRoleResource
    protected PlexusRoleResource securityToRestModel( RoleIdentifier role )
    {
        // TODO: We shouldn't be looking up the role name here anyway... this should get pushed up to the
        // SecuritySystem.
        String roleName = role.getRoleId();

        SecuritySystem securitySystem = this.getSecuritySystem();

        try
        {
            AuthorizationManager authzManager = securitySystem.getAuthorizationManager( DEFAULT_SOURCE );
            roleName = authzManager.getRole( role.getRoleId() ).getName();
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().warn(
                "Failed to lookup the users Role: " + role.getRoleId() + " source: " + role.getSource()
                    + " but the user has this role.", e );
        }
        catch ( NoSuchRoleException e )
        {
            // this is a Warning if the role's source is default, if its not, then we most of the time it would not be
            // found anyway.
            if ( DEFAULT_SOURCE.equals( role.getSource() ) )
            {
                this.getLogger().warn(
                    "Failed to lookup the users Role: " + role.getRoleId() + " source: " + role.getSource()
                        + " but the user has this role.", e );
            }
            else
            {
                this.getLogger().debug(
                    "Failed to lookup the users Role: " + role.getRoleId() + " source: " + role.getSource()
                        + " falling back to the roleId for the role's name." );
            }
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
            ValidationResponse response = new ValidationResponse();
            response.addValidationError( new ValidationMessage( "status", "Users status is not valid." ) );
            throw new InvalidConfigurationException( response );
        }
    }

    protected String getRequestAttribute( final Request request, final String key )
    {
        return getRequestAttribute( request, key, true );
    }

    protected String getRequestAttribute( final Request request, final String key, final boolean decode )
    {
        final String value = request.getAttributes().get( key ).toString();

        if ( decode )
        {
            try
            {
                return URLDecoder.decode( value, "UTF-8" );
            }
            catch ( UnsupportedEncodingException e )
            {
                getLogger().warn( "Failed to decode URL attribute.", e );
            }
        }

        return value;
    }

}
