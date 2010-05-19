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
package org.sonatype.security.realms.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.validator.SecurityConfigurationValidator;
import org.sonatype.security.realms.validator.SecurityValidationContext;
import org.sonatype.security.usermanagement.StringDigester;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager;

@Component( role = ConfigurationManager.class, hint = "default" )
public class DefaultConfigurationManager
    extends AbstractLogEnabled
    implements ConfigurationManager
{

    @Requirement(hint="file")
    private SecurityModelConfigurationSource configurationSource;
    
    @Requirement
    private SecurityConfigurationValidator validator;
    
    @Requirement( role = PrivilegeDescriptor.class )
    private List<PrivilegeDescriptor> privilegeDescriptors;
    
    @Requirement( role = SecurityConfigurationCleaner.class )
    private SecurityConfigurationCleaner configCleaner;

    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;

    private ReentrantLock lock = new ReentrantLock();

    public List<CPrivilege> listPrivileges()
    {
        return Collections.unmodifiableList( getConfiguration().getPrivileges() );
    }

    public List<CRole> listRoles()
    {
        return Collections.unmodifiableList( getConfiguration().getRoles() );
    }

    public List<CUser> listUsers()
    {
        return Collections.unmodifiableList( getConfiguration().getUsers() );
    }

    public void createPrivilege( CPrivilege privilege )
        throws InvalidConfigurationException
    {
        createPrivilege( privilege, initializeContext() );
    }

    public void createPrivilege( CPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validatePrivilege( context, privilege, false );

        if ( vr.isValid() )
        {
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createRole( CRole role )
        throws InvalidConfigurationException
    {
        createRole( role, initializeContext() );
    }

    public void createRole( CRole role, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateRole( context, role, false );

        if ( vr.isValid() )
        {
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createUser( CUser user, Set<String> roles )
        throws InvalidConfigurationException
    {
        createUser( user, null, roles, initializeContext() );
    }

    public void createUser( CUser user, String password, Set<String> roles )
        throws InvalidConfigurationException
    {
        createUser( user, password, roles, initializeContext() );
    }

    public void createUser( CUser user, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, roles, context );
    }

    public void createUser( CUser user, String password, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // set the password if its not null
        if ( password != null && password.trim().length() > 0 )
        {
            user.setPassword( StringDigester.getSha1Digest( password ) );
        }

        ValidationResponse vr = validator.validateUser( context, user, roles, false );

        if ( vr.isValid() )
        {
            getConfiguration().addUser( user );
            this.createOrUpdateUserRoleMapping( this.buildUserRoleMapping( user.getId(), roles ) );

        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    private void createOrUpdateUserRoleMapping( CUserRoleMapping roleMapping )
    {
        // delete first, ask questions later
        // we are always updating, its possible that this object could have already existed, because we cannot fully
        // sync with external realms.
        try
        {
            this.deleteUserRoleMapping( roleMapping.getUserId(), roleMapping.getSource() );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // it didn't exist, thats ok.
        }

        // now add it
        this.getConfiguration().addUserRoleMapping( roleMapping );

    }

    private CUserRoleMapping buildUserRoleMapping( String userId, Set<String> roles )
    {
        CUserRoleMapping roleMapping = new CUserRoleMapping();

        roleMapping.setUserId( userId );
        roleMapping.setSource( SecurityXmlUserManager.SOURCE );
        roleMapping.setRoles( new ArrayList<String>( roles ) );

        return roleMapping;

    }

    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        deletePrivilege( id, true );
    }
    
    public void deletePrivilege( String id, boolean clean )
        throws NoSuchPrivilegeException
    {
        boolean found = false;
    
        for ( Iterator<CPrivilege> iter = getConfiguration().getPrivileges().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }
    
        if ( !found )
        {
            throw new NoSuchPrivilegeException( id );
        }
     
        if ( clean )
        {
            cleanRemovedPrivilege( id );
        }
    }

    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        deleteRole( id, true );
    }
    
    protected void deleteRole( String id, boolean clean )
        throws NoSuchRoleException
    {
        boolean found = false;

        for ( Iterator<CRole> iter = getConfiguration().getRoles().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new NoSuchRoleException( id );
        }
        
        if ( clean )
        {
            cleanRemovedRole( id );
        }
    }

    public void deleteUser( String id )
        throws UserNotFoundException
    {
        boolean found = false;

        for ( Iterator<CUser> iter = getConfiguration().getUsers().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new UserNotFoundException( id );
        }
        
        // delete the user role mapping for this user too
        try
        {
            this.deleteUserRoleMapping( id, SecurityXmlUserManager.SOURCE );
        }
        catch ( NoSuchRoleMappingException e )
        {
            this.getLogger().debug( "User role mapping for user: "+ id +" source: "+ SecurityXmlUserManager.SOURCE + " could not be deleted because it does not exist." );
        }
    }

    public CPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        for ( CPrivilege privilege : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return privilege;
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    public CRole readRole( String id )
        throws NoSuchRoleException
    {
        for ( CRole role : (List<CRole>) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return role;
            }
        }

        throw new NoSuchRoleException( id );
    }

    public CUser readUser( String id )
        throws UserNotFoundException
    {
        for ( CUser user : (List<CUser>) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return user;
            }
        }

        throw new UserNotFoundException( id );
    }

    public void updatePrivilege( CPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        updatePrivilege( privilege, initializeContext() );
    }

    public void updatePrivilege( CPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validatePrivilege( context, privilege, true );

        if ( vr.isValid() )
        {
            deletePrivilege( privilege.getId(), false );
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateRole( CRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        updateRole( role, initializeContext() );
    }

    public void updateRole( CRole role, SecurityValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateRole( context, role, true );

        if ( vr.isValid() )
        {
            deleteRole( role.getId(), false );
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateUser( CUser user, Set<String> roles )
        throws InvalidConfigurationException,
            UserNotFoundException
    {
        updateUser( user, roles, initializeContext() );
    }

    public void updateUser( CUser user, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException,
            UserNotFoundException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateUser( context, user, roles, true );

        if ( vr.isValid() )
        {
            deleteUser( user.getId() );
            getConfiguration().addUser( user );
            this.createOrUpdateUserRoleMapping( this.buildUserRoleMapping( user.getId(), roles ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public String getPrivilegeProperty( CPrivilege privilege, String key )
    {
        if ( privilege != null && privilege.getProperties() != null )
        {
            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
            {
                if ( prop.getKey().equals( key ) )
                {
                    return prop.getValue();
                }
            }
        }

        return null;
    }

    public void createUserRoleMapping( CUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException
    {
        this.createUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    public void createUserRoleMapping( CUserRoleMapping userRoleMapping, SecurityValidationContext context )
        throws InvalidConfigurationException
    {

        if ( context == null )
        {
            context = this.initializeContext();
        }

        try
        {
            // this will throw a NoSuchRoleMappingException, if there isn't one
            this.readUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() );

            ValidationResponse vr = new ValidationResponse();
            vr.addValidationError( new ValidationMessage( "*", "User Role Mapping for user '"
                + userRoleMapping.getUserId() + "' already exists." ) );

            throw new InvalidConfigurationException( vr );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // expected
        }

        ValidationResponse vr = validator.validateUserRoleMapping( context, userRoleMapping, false );

        if ( vr.getValidationErrors().size() > 0 )
        {
            throw new InvalidConfigurationException( vr );
        }

        getConfiguration().addUserRoleMapping( userRoleMapping );
    }

    private CUserRoleMapping readCUserRoleMapping( String userId, String source ) throws NoSuchRoleMappingException
    {
        for ( CUserRoleMapping userRoleMapping : (List<CUserRoleMapping>) getConfiguration().getUserRoleMappings() )
        {   
            if (  StringUtils.equals( userRoleMapping.getUserId(), userId ) && StringUtils.equals( userRoleMapping.getSource(), source ))
            {
                return userRoleMapping;
            }
        }

        throw new NoSuchRoleMappingException( "No User Role Mapping for user: " + userId );
    }
    

    public CUserRoleMapping readUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        return this.readCUserRoleMapping( userId, source );
    }

    public void updateUserRoleMapping( CUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        this.updateUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    public void updateUserRoleMapping( CUserRoleMapping userRoleMapping, SecurityValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        if ( context == null )
        {
            context = this.initializeContext();
        }

        if ( this.readUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() ) == null )
        {
            ValidationResponse vr = new ValidationResponse();
            vr.addValidationError( new ValidationMessage( "*", "No User Role Mapping found for user '" + userRoleMapping.getUserId()
                + "'." ) );

            throw new InvalidConfigurationException( vr );
        }

        ValidationResponse vr = validator.validateUserRoleMapping( context, userRoleMapping, true );

        if ( vr.getValidationErrors().size() > 0 )
        {
            throw new InvalidConfigurationException( vr );
        }

        this.deleteUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() );
        getConfiguration().addUserRoleMapping( userRoleMapping );
    }

    public List<CUserRoleMapping> listUserRoleMappings()
    {
        return Collections.unmodifiableList( getConfiguration().getUserRoleMappings() );
    }

    public void deleteUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        boolean found = false;

        for ( Iterator<CUserRoleMapping> iter = getConfiguration().getUserRoleMappings().iterator(); iter.hasNext(); )
        {
            CUserRoleMapping userRoleMapping = iter.next();
            if ( userRoleMapping.getUserId().equals( userId )
                && ( StringUtils.equals( userRoleMapping.getSource(), source )) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new NoSuchRoleMappingException( "No User Role Mapping for user: " + userId );
        }
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return getPrivilegeProperty( readPrivilege( id ), key );
    }

    public void clearCache()
    {
        // Just to make sure we aren't fiddling w/ save/loading process
        lock.lock();
        configuration = null;
        lock.unlock();
    }

    public void save()
    {
        lock.lock();

        try
        {
            this.configurationSource.storeConfiguration();
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while storing configuration file", e );
        }
        finally
        {
            lock.unlock();
        }
    }

    private Configuration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }

        lock.lock();

        try
        {
            this.configurationSource.loadConfiguration();

            configuration = this.configurationSource.getConfiguration();
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Invalid Configuration", e );
        }
        finally
        {  
            lock.unlock();
        }

        return configuration;
    }

    public SecurityValidationContext initializeContext()
    {
        SecurityValidationContext context = new SecurityValidationContext();

        context.addExistingUserIds();
        context.addExistingRoleIds();
        context.addExistingPrivilegeIds();

        for ( CUser user : listUsers() )
        {
            context.getExistingUserIds().add( user.getId() );

            context.getExistingEmailMap().put( user.getId(), user.getEmail() );
        }

        for ( CRole role : listRoles() )
        {
            context.getExistingRoleIds().add( role.getId() );

            ArrayList<String> containedRoles = new ArrayList<String>();

            containedRoles.addAll( role.getRoles() );

            context.getRoleContainmentMap().put( role.getId(), containedRoles );

            context.getExistingRoleNameMap().put( role.getId(), role.getName() );
        }

        for ( CPrivilege priv : listPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        for ( CUserRoleMapping roleMappings : listUserRoleMappings() )
        {
            context.getExistingUserRoleMap().put( roleMappings.getUserId(), roleMappings.getRoles() );
        }

        return context;
    }
    
    public List<PrivilegeDescriptor> listPrivilegeDescriptors()
    {
        return Collections.unmodifiableList( privilegeDescriptors );
    }
    
    public void cleanRemovedPrivilege( String privilegeId )
    {
        configCleaner.privilegeRemoved( getConfiguration(), privilegeId );
    }
    
    public void cleanRemovedRole( String roleId )
    {
        configCleaner.roleRemoved( getConfiguration(), roleId );
    }
}
