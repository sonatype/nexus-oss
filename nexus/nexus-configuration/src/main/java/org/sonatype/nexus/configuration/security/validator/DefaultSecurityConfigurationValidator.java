/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
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
    @SuppressWarnings( "unchecked" )
    public ValidationResponse validateModel( ValidationRequest request )
    {
        ValidationResponse response = new SecurityValidationResponse();

        Configuration model = (Configuration) request.getConfiguration();
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        List<CApplicationPrivilege> appPrivs = model.getApplicationPrivileges();
        
        if ( appPrivs != null )
        {
            for ( CApplicationPrivilege priv : appPrivs )
            {
                response.append( validateApplicationPrivilege( context, priv ) );
            }
        }
        
        List<CRepoTargetPrivilege> targetPrivs = model.getRepositoryTargetPrivileges();
        
        if ( targetPrivs != null )
        {
            for ( CRepoTargetPrivilege priv : targetPrivs )
            {
                response.append( validateRepoTargetPrivilege( context, priv ) );
            }
        }
        
        List<CRole> roles = model.getRoles();
        
        if ( roles != null )
        {
            for ( CRole role : roles )
            {
                response.append( validateRole( context, role ) );
            }
        }
        
        response.append( validateRoleContainment( context ) );
        
        List<CUser> users = model.getUsers();
        
        if ( users != null )
        {
            for ( CUser user : users )
            {
                response.append( validateUser( context, user ) );
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
    
    public ValidationResponse validatePrivilege( SecurityValidationContext ctx, CPrivilege privilege )
    {
        ValidationResponse response = new SecurityValidationResponse();
        
        if ( ctx != null )
        {
            response.setContext( ctx );
        }
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        Random rnd = new Random();
        
        List<String> existingIds = context.getExistingPrivilegeIds();
        
        if ( existingIds == null )
        {
            context.addExistingPrivilegeIds();
            
            existingIds = context.getExistingPrivilegeIds();
        }

        if ( StringUtils.isEmpty( privilege.getId() )
            || "0".equals( privilege.getId() )
            || ( existingIds.contains( privilege.getId() ) ) )
        {
            String newId = Long.toHexString( System.currentTimeMillis() + rnd.nextInt( 2008 ) );

            response.addValidationWarning( "Fixed wrong privilege ID from '" + 
                                           privilege.getId() + "' to '" + newId + "'" );
            
            privilege.setId( newId );

            response.setModified( true );
        }
        
        if ( !CPrivilege.METHOD_CREATE.equals( privilege.getMethod() ) 
            && !CPrivilege.METHOD_DELETE.equals( privilege.getMethod() )
            && !CPrivilege.METHOD_READ.equals( privilege.getMethod() )
            && !CPrivilege.METHOD_UPDATE.equals( privilege.getMethod() ) )
        {
            response.addValidationError( "Privilege ID '" + privilege.getId() + "' Method is wrong! (Allowed values are: " 
                                         + CPrivilege.METHOD_CREATE + ", "
                                         + CPrivilege.METHOD_DELETE + ", " 
                                         + CPrivilege.METHOD_READ + " and " 
                                         + CPrivilege.METHOD_UPDATE + ")" );
        }
        
        if ( StringUtils.isEmpty( privilege.getName() ) )
        {
            response.addValidationError( "Privilege ID '" + privilege.getId() + "' requires a name." );
        }
        
        existingIds.add( privilege.getId() );
        
        return response;
    }
    
    public ValidationResponse validateRepoTargetPrivilege( SecurityValidationContext ctx, CRepoTargetPrivilege privilege )
    {
        ValidationResponse response = new SecurityValidationResponse();
        
        if ( ctx != null )
        {
            response.setContext( ctx );
        }
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        response.append( validatePrivilege( context, privilege ) );
        
        if ( StringUtils.isEmpty( privilege.getRepositoryTargetId() ) )
        {
            response.addValidationError( "Privilege ID '" + privilege.getId() + "' requires a repositoryTargetId." );
        }
        
        if ( !StringUtils.isEmpty( privilege.getRepositoryId() )
            && !StringUtils.isEmpty( privilege.getGroupId() ) )
        {
            response.addValidationError( "Privilege ID '" + privilege.getId() + "' cannot be assigned to both a group and repository." 
                + "  Either assign a group, a repository or neither (which assigns to ALL repositories).");
        }
        
        return response;
    }
    
    public ValidationResponse validateApplicationPrivilege( SecurityValidationContext ctx, CApplicationPrivilege privilege )
    {
        ValidationResponse response = new SecurityValidationResponse();
        
        if ( ctx != null )
        {
            response.setContext( ctx );
        }
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        response.append( validatePrivilege( context, privilege ) );
        
        if ( StringUtils.isEmpty( privilege.getPath() ) )
        {
            response.addValidationError( "Privilege ID '" + privilege.getId() + "' Application path cannot be empty." );
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
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
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
            if ( containedRoleId.equals( baseRoleId ) )
            {
                response.addValidationError( "Role ID '" + baseRoleId + "' contains itself through Role ID '" + roleId + "'.  This is not valid." );
                break;
            }
            
            response.append( isRecursive( baseRoleId, containedRoleId, ctx ) );
        }
        
        return response;
    }
    
    public ValidationResponse validateRole( SecurityValidationContext ctx, CRole role )
    {
        ValidationResponse response = new SecurityValidationResponse();
        
        if ( ctx != null )
        {
            response.setContext( ctx );
        }
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        Random rnd = new Random();
        
        List<String> existingIds = context.getExistingRoleIds();
        
        if ( existingIds == null )
        {
            context.addExistingRoleIds();
            
            existingIds = context.getExistingRoleIds();
        }

        if ( StringUtils.isEmpty( role.getId() )
            || "0".equals( role.getId() )
            || ( existingIds.contains( role.getId() ) ) )
        {
            String newId = Long.toHexString( System.currentTimeMillis() + rnd.nextInt( 2008 ) );

            response.addValidationWarning( "Fixed wrong role ID from '" + 
                                           role.getId() + "' to '" + newId + "'" );
            
            role.setId( newId );

            response.setModified( true );
        }
        
        if ( StringUtils.isEmpty( role.getName() ) )
        {
            response.addValidationError( "Role ID '" + role.getId() + "' requires a name." );
        }
        
        if ( 1 > role.getSessionTimeout() )
        {
            response.addValidationWarning( "Role ID '" + role.getId() + "' fixed invalid session timeout from '" + 
                                           role.getSessionTimeout() + "' to '60'." );
            
            role.setSessionTimeout( 60 );
            
            response.setModified( true );
        }
        
        if ( context.getExistingPrivilegeIds() != null )
        {
            List<String> privIds = role.getPrivileges();
            
            for ( String privId : privIds )
            {
                if ( !context.getExistingPrivilegeIds().contains( privId ) )
                {
                    response.addValidationError( "Role ID '" + role.getId() + "' Invalid privilege id '" + privId + "' found." );
                }
            }
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
                response.addValidationError( "Role ID '" + role.getId() + "' Contains itself, this is not valid." );
            }
            else if ( context.getRoleContainmentMap() != null )
            {                
                containedRoles.add( roleId );
            }
        }
        
        existingIds.add( role.getId() );
        
        return response;
    }
    
    public ValidationResponse validateUser( SecurityValidationContext ctx, CUser user )
    {
        ValidationResponse response = new SecurityValidationResponse();
        
        if ( ctx != null )
        {
            response.setContext( ctx );
        }
        
        SecurityValidationContext context = ( SecurityValidationContext ) response.getContext();
        
        List<String> existingIds = context.getExistingUserIds();
        
        if ( existingIds == null )
        {
            context.addExistingUserIds();
            
            existingIds = context.getExistingUserIds();
        }

        if ( StringUtils.isEmpty( user.getUserId() )
            || existingIds.contains( user.getUserId() ) )
        {
            response.addValidationError( "User ID '" + user.getUserId() + "' is invalid.  It is either empty or already in use." );
        }
        
        if ( StringUtils.isEmpty( user.getEmail() ) )
        {
            response.addValidationError( "User ID '" + user.getUserId() + "' has no email address" );
        }
        
        if ( !CUser.STATUS_ACTIVE.equals( user.getStatus() ) 
            && !CUser.STATUS_DISABLED.equals( user.getStatus() )
            && !CUser.STATUS_LOCKED.equals( user.getStatus() ) )
        {
            response.addValidationError( "User ID '" + user.getUserId() + "' has invalid status '" + user.getStatus() + 
                                         "'.  (Allowed values are: " + CUser.STATUS_ACTIVE + ", "
                                         + CUser.STATUS_DISABLED + " and " + CUser.STATUS_LOCKED + ")" );
        }
        
        if ( context.getExistingRoleIds() != null )
        {
            List<String> roleIds = user.getRoles();
            
            for ( String roleId : roleIds )
            {
                if ( !context.getExistingRoleIds().contains( roleId ) )
                {
                    response.addValidationError( "User ID '" + user.getUserId() + "' Invalid role id '" + roleId + "' found." );
                }
            }
        }
        
        existingIds.add( user.getUserId() );
        
        return response;
    }
}
