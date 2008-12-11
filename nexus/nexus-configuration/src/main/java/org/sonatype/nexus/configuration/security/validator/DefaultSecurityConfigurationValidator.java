/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.configuration.security.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.ConfigurationIdGenerator;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The default configuration validator provider. It checks the model for semantical validity.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultSecurityConfigurationValidator
    extends AbstractLogEnabled
    implements SecurityConfigurationValidator
{
    /**
     * @plexus.requirement
     */
    private ConfigurationIdGenerator idGenerator;
    
    @SuppressWarnings( "unchecked" )
    public ValidationResponse validateModel( ValidationRequest request )
    {
        ValidationResponse response = new SecurityValidationResponse();

        Configuration model = (Configuration) request.getConfiguration();

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        List<CApplicationPrivilege> appPrivs = model.getApplicationPrivileges();

        if ( appPrivs != null )
        {
            for ( CApplicationPrivilege priv : appPrivs )
            {
                response.append( validateApplicationPrivilege( context, priv, false ) );
            }
        }

        List<CRepoTargetPrivilege> targetPrivs = model.getRepositoryTargetPrivileges();

        if ( targetPrivs != null )
        {
            for ( CRepoTargetPrivilege priv : targetPrivs )
            {
                response.append( validateRepoTargetPrivilege( context, priv, false ) );
            }
        }

        List<CRole> roles = model.getRoles();

        if ( roles != null )
        {
            for ( CRole role : roles )
            {
                response.append( validateRole( context, role, false ) );
            }
        }

        response.append( validateRoleContainment( context ) );

        List<CUser> users = model.getUsers();

        if ( users != null )
        {
            for ( CUser user : users )
            {
                response.append( validateUser( context, user, false ) );
            }
        }

        // summary
        if ( response.getValidationErrors().size() > 0 || response.getValidationWarnings().size() > 0 )
        {
            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            getLogger().error( "Security configuration has validation errors/warnings" );

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            if ( response.getValidationErrors().size() > 0 )
            {
                getLogger().error( "The ERRORS:" );

                for ( ValidationMessage msg : response.getValidationErrors() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            if ( response.getValidationWarnings().size() > 0 )
            {
                getLogger().error( "The WARNINGS:" );

                for ( ValidationMessage msg : response.getValidationWarnings() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * *" );
        }
        else
        {
            getLogger().info( "Security configuration validated succesfully." );
        }

        return response;
    }

    public ValidationResponse validatePrivilege( SecurityValidationContext ctx, CPrivilege privilege, boolean update )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        List<String> existingIds = context.getExistingPrivilegeIds();

        if ( existingIds == null )
        {
            context.addExistingPrivilegeIds();

            existingIds = context.getExistingPrivilegeIds();
        }

        if ( !update
            && ( StringUtils.isEmpty( privilege.getId() ) || "0".equals( privilege.getId() ) || ( existingIds
                .contains( privilege.getId() ) ) ) )
        {
            String newId = idGenerator.generateId();

            ValidationMessage message = new ValidationMessage( "id", "Fixed wrong privilege ID from '"
                + privilege.getId() + "' to '" + newId + "'" );
            response.addValidationWarning( message );

            privilege.setId( newId );

            response.setModified( true );
        }

        // validate method
        // method is of form ('*' | 'read' | 'create' | 'update' | 'delete' [, method]* )
        // so, 'read' method is correct, but so is also 'create,update,delete'
        // '*' means ALL POSSIBLE value for this "field"
        if ( StringUtils.isEmpty( privilege.getMethod() ) )
        {
            response.addValidationError( "Method cannot be empty on a privilege!" );
        }
        else
        {
            String[] methods = null;

            if ( privilege.getMethod().contains( "," ) )
            {
                // it is a list of methods
                methods = privilege.getMethod().split( "," );
            }
            else
            {
                // it is a single method
                methods = new String[] { privilege.getMethod() };
            }

            boolean valid = true;

            for ( String method : methods )
            {
                if ( !CPrivilege.METHOD_CREATE.equals( method ) && !CPrivilege.METHOD_DELETE.equals( method )
                    && !CPrivilege.METHOD_READ.equals( method ) && !CPrivilege.METHOD_UPDATE.equals( method )
                    && !"*".equals( method ) )
                {
                    valid = false;

                    break;
                }
            }

            if ( !valid )
            {
                ValidationMessage message = new ValidationMessage( "method", "Privilege ID '" + privilege.getId()
                    + "' Method is wrong! (Allowed methods are: " + CPrivilege.METHOD_CREATE + ", "
                    + CPrivilege.METHOD_DELETE + ", " + CPrivilege.METHOD_READ + " and " + CPrivilege.METHOD_UPDATE
                    + ")", "Invalid method selected." );
                response.addValidationError( message );
            }

        }

        if ( StringUtils.isEmpty( privilege.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "Privilege ID '" + privilege.getId()
                + "' requires a name.", "Name is required." );
            response.addValidationError( message );
        }

        existingIds.add( privilege.getId() );

        return response;
    }

    public ValidationResponse validateRepoTargetPrivilege( SecurityValidationContext ctx,
        CRepoTargetPrivilege privilege, boolean update )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        response.append( validatePrivilege( context, privilege, update ) );

        if ( StringUtils.isEmpty( privilege.getRepositoryTargetId() ) )
        {
            ValidationMessage message = new ValidationMessage( "repositoryTargetId", "Privilege ID '"
                + privilege.getId() + "' requires a repositoryTargetId.", "Repository Target is required." );
            response.addValidationError( message );
        }

        if ( !StringUtils.isEmpty( privilege.getRepositoryId() ) && !StringUtils.isEmpty( privilege.getGroupId() ) )
        {
            ValidationMessage message = new ValidationMessage(
                "repositoryId",
                "Privilege ID '" + privilege.getId() + "' cannot be assigned to both a group and repository."
                    + "  Either assign a group, a repository or neither (which assigns to ALL repositories).",
                "Cannot select both a Repository and Repository Group." );
            response.addValidationError( message );
        }

        return response;
    }

    public ValidationResponse validateApplicationPrivilege( SecurityValidationContext ctx,
        CApplicationPrivilege privilege, boolean update )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        response.append( validatePrivilege( context, privilege, update ) );

        if ( StringUtils.isEmpty( privilege.getPermission() ) )
        {
            ValidationMessage message = new ValidationMessage( "permission", "Privilege ID '" + privilege.getId()
                + "' Application permission cannot be empty.", "Permission is required." );

            response.addValidationError( message );
        }

        return response;
    }

    public ValidationResponse validateRoleContainment( SecurityValidationContext ctx )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        if ( context.getExistingRoleIds() != null )
        {
            for ( String roleId : context.getExistingRoleIds() )
            {
                response.append( isRecursive( roleId, roleId, ctx ) );
            }
        }

        return response;
    }

    private ValidationResponse isRecursive( String baseRoleId, String roleId, SecurityValidationContext ctx )
    {
        ValidationResponse response = new SecurityValidationResponse();

        List<String> containedRoles = ctx.getRoleContainmentMap().get( roleId );

        for ( String containedRoleId : containedRoles )
        {
            // Only need to do this on the first level
            if ( baseRoleId.equals( roleId ) )
            {
                if ( !ctx.getExistingRoleIds().contains( roleId ) )
                {
                    ValidationMessage message = new ValidationMessage( "roles", "Role ID '" + baseRoleId
                        + "' contains an invalid role", "Role cannot contain invalid role ID '" + roleId + "'." );

                    response.addValidationError( message );
                }
            }

            if ( containedRoleId.equals( baseRoleId ) )
            {
                ValidationMessage message = new ValidationMessage(
                    "roles",
                    "Role ID '" + baseRoleId + "' contains itself through Role ID '" + roleId
                        + "'.  This is not valid.",
                    "Role cannot contain itself recursively (via role ID '" + roleId + "')." );

                response.addValidationError( message );

                break;
            }

            if ( ctx.getExistingRoleIds().contains( containedRoleId ) )
            {
                response.append( isRecursive( baseRoleId, containedRoleId, ctx ) );
            }
            // Only need to do this on the first level
            else if ( baseRoleId.equals( roleId ) )
            {
                ValidationMessage message = new ValidationMessage(
                    "roles",
                    "Role ID '" + roleId + "' contains an invalid role ID '" + containedRoleId + "'.",
                    "Role cannot contain invalid role ID '" + containedRoleId + "'." );

                response.addValidationError( message );
            }
        }

        return response;
    }

    public ValidationResponse validateRole( SecurityValidationContext ctx, CRole role, boolean update )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        List<String> existingIds = context.getExistingRoleIds();

        if ( existingIds == null )
        {
            context.addExistingRoleIds();

            existingIds = context.getExistingRoleIds();
        }

        if ( !update
            && ( StringUtils.isEmpty( role.getId() ) || "0".equals( role.getId() ) || ( existingIds.contains( role
                .getId() ) ) ) )
        {
            String newId = idGenerator.generateId();

            response.addValidationWarning( "Fixed wrong role ID from '" + role.getId() + "' to '" + newId + "'" );

            role.setId( newId );

            response.setModified( true );
        }

        if ( StringUtils.isEmpty( role.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "Role ID '" + role.getId()
                + "' requires a name.", "Name is required." );
            response.addValidationError( message );
        }

        if ( 1 > role.getSessionTimeout() )
        {
            ValidationMessage message = new ValidationMessage(
                "sessionTimeout",
                "Role ID '" + role.getId() + "' requires a Session Timeout greater than 0 minutes.",
                "Enter a session timeout greater than 0 minutes." );
            response.addValidationError( message );
        }

        // No roles or privs
        if ( role.getRoles().size() == 0 && role.getPrivileges().size() == 0 )
        {
            ValidationMessage message = new ValidationMessage( "privileges", "Role ID '" + role.getId()
                + "' is required to contain at least 1 role or privilege.", "One or more roles/privilegs are required." );
            response.addValidationError( message );
        }

        if ( context.getExistingPrivilegeIds() != null )
        {
            List<String> privIds = role.getPrivileges();

            for ( String privId : privIds )
            {
                if ( !context.getExistingPrivilegeIds().contains( privId ) )
                {
                    ValidationMessage message = new ValidationMessage(
                        "privileges",
                        "Role ID '" + role.getId() + "' Invalid privilege id '" + privId + "' found.",
                        "Role cannot contain invalid privilege ID '" + privId + "'." );
                    response.addValidationError( message );
                }
            }
        }

        // It is expected that a full context is built upon update
        if ( update )
        {
            response.append( isRecursive( role.getId(), role.getId(), context ) );
        }

        List<String> roleIds = role.getRoles();

        List<String> containedRoles = context.getRoleContainmentMap().get( role.getId() );

        if ( containedRoles == null )
        {
            containedRoles = new ArrayList();
            context.getRoleContainmentMap().put( role.getId(), containedRoles );
        }

        for ( String roleId : roleIds )
        {
            if ( roleId.equals( role.getId() ) )
            {
                ValidationMessage message = new ValidationMessage( "roles", "Role ID '" + role.getId()
                    + "' cannot contain itself.", "Role cannot contain itself." );
                response.addValidationError( message );
            }
            else if ( context.getRoleContainmentMap() != null )
            {
                containedRoles.add( roleId );
            }
        }

        existingIds.add( role.getId() );

        return response;
    }

    public ValidationResponse validateUser( SecurityValidationContext ctx, CUser user, boolean update )
    {
        ValidationResponse response = new SecurityValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        List<String> existingIds = context.getExistingUserIds();

        if ( existingIds == null )
        {
            context.addExistingUserIds();

            existingIds = context.getExistingUserIds();
        }

        Map<String, String> existingEmailMap = context.getExistingEmailMap();

        if ( !update && ( StringUtils.isEmpty( user.getUserId() ) || existingIds.contains( user.getUserId() ) ) )
        {
            ValidationMessage message = new ValidationMessage( "userId", "User ID '" + user.getUserId()
                + "' is invalid.  It is either empty or already in use.", "User Id is required and must be unique." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "User ID '" + user.getUserId()
                + "' has no Name.  This is a required field.", "Name is required." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getPassword() ) )
        {
            ValidationMessage message = new ValidationMessage( "password", "User ID '" + user.getUserId()
                + "' has no password.  This is a required field.", "Password is required." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getEmail() ) )
        {
            ValidationMessage message = new ValidationMessage( "email", "User ID '" + user.getUserId()
                + "' has no email address", "Email address is required." );
            response.addValidationError( message );
        }
        else
        {
            existingEmailMap.put( user.getUserId(), user.getEmail() );
        }

        if ( !CUser.STATUS_ACTIVE.equals( user.getStatus() ) && !CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            ValidationMessage message = new ValidationMessage(
                "status",
                "User ID '" + user.getUserId() + "' has invalid status '" + user.getStatus()
                    + "'.  (Allowed values are: " + CUser.STATUS_ACTIVE + " and " + CUser.STATUS_DISABLED + ")",
                "Invalid Status selected." );
            response.addValidationError( message );
        }

        if ( context.getExistingRoleIds() != null )
        {
            List<String> roleIds = user.getRoles();

            for ( String roleId : roleIds )
            {
                if ( !context.getExistingRoleIds().contains( roleId ) )
                {
                    ValidationMessage message = new ValidationMessage( "roles", "User ID '" + user.getUserId()
                        + "' Invalid role id '" + roleId + "' found.", "User cannot contain invalid role ID '" + roleId
                        + "'." );
                    response.addValidationError( message );
                }
            }
        }

        if ( user.getRoles().size() == 0 )
        {
            ValidationMessage message = new ValidationMessage( "roles", "User ID '" + user.getUserId()
                + "' has no roles assigned.", "User requires one or more roles." );
            response.addValidationError( message );
        }

        if ( !StringUtils.isEmpty( user.getUserId() ) )
        {
            existingIds.add( user.getUserId() );
        }

        return response;
    }
}
