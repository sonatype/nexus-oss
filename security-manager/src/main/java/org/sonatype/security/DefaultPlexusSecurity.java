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
package org.sonatype.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.realm.AuthenticatingRealm;
import org.jsecurity.realm.CachingRealm;
import org.jsecurity.realm.Realm;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.ConfigurationException;
import org.sonatype.security.model.source.SecurityConfigurationSource;
import org.sonatype.security.realms.XmlAuthenticatingRealm;
import org.sonatype.security.realms.XmlAuthorizingRealm;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.InvalidConfigurationException;
import org.sonatype.security.realms.tools.NoSuchPrivilegeException;
import org.sonatype.security.realms.tools.NoSuchRoleException;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;
import org.sonatype.security.realms.tools.NoSuchUserException;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.realms.tools.dao.SecurityRole;
import org.sonatype.security.realms.tools.dao.SecurityUser;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.realms.validator.ValidationContext;
import org.sonatype.security.email.NoSuchEmailException;
import org.sonatype.security.email.SecurityEmailer;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;
import org.sonatype.security.events.SecurityEvent;
import org.sonatype.security.events.SecurityEventHandler;
import org.sonatype.security.locators.RealmLocator;

@Component( role = PlexusSecurity.class )
public class DefaultPlexusSecurity
    implements PlexusSecurity, Startable
{
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager manager;

    @Requirement( hint = "file" )
    private SecurityConfigurationSource configSource;

    @Requirement
    private PrivilegeInheritanceManager privInheritance;

    @Requirement
    private PasswordGenerator pwGenerator;

    @Requirement
    private SecurityEmailer emailer;

    @Requirement
    private RealmLocator realmLocator;
    
    @Requirement
    private Logger logger;
    
    private List<SecurityEventHandler> eventHandlers = new ArrayList<SecurityEventHandler>();

    public void start()
        throws StartingException
    {
        // Do this simply to upgrade the configuration if necessary
        try
        {
            clearCache();
            configSource.loadConfiguration();
            getLogger().info( "Security Configuration loaded properly." );
        }
        catch ( ConfigurationException e )
        {
            getLogger().fatalError( "Security Configuration is invalid!!!", e );
        }
        catch ( IOException e )
        {
            getLogger().fatalError( "Security Configuration is invalid!!!", e );
        }

        getLogger().info( "Started Plexus Security" );
    }

    public void stop()
        throws StoppingException
    {
        this.eventHandlers.clear();
        getLogger().info( "Stopped Plexus Security" );
    }

    public void clearCache()
    {
        manager.clearCache();
    }

    public void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException
    {
        createPrivilege( privilege, null );
    }

    public void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException
    {
        addInheritedPrivileges( privilege );
        manager.createPrivilege( privilege, context );
        save();
    }

    public void createRole( SecurityRole role )
        throws InvalidConfigurationException
    {
        createRole( role, null );
    }

    public void createRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException
    {
        manager.createRole( role, context );
        save();
    }

    public void createUser( SecurityUser user )
        throws InvalidConfigurationException
    {
        createUser( user, null, null );
    }

    public void createUser( SecurityUser user, String password )
        throws InvalidConfigurationException
    {
        createUser( user, password, null );
    }

    public void createUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, context );
    }

    public void createUser( SecurityUser user, String password, ValidationContext context )
        throws InvalidConfigurationException
    {
        // if the password passed in is not null, hash it and use it, else, just generate one.
        if ( StringUtils.isEmpty( password ) )
        {
            password = generatePassword( user );
        }
        else
        {
            user.setPassword( pwGenerator.hashPassword( password ) );
        }

        manager.createUser( user, context );
        emailer.sendNewUserCreated( user.getEmail(), user.getId(), password );
        save();

    }

    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        manager.deletePrivilege( id );
        save();
    }

    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        manager.deleteRole( id );
        save();
    }

    public void deleteUser( String id )
        throws NoSuchUserException
    {
        manager.deleteUser( id );
        save();
    }

    public String getPrivilegeProperty( SecurityPrivilege privilege, String key )
    {
        return manager.getPrivilegeProperty( privilege, key );
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return manager.getPrivilegeProperty( id, key );
    }

    public List<SecurityPrivilege> listPrivileges()
    {
        return manager.listPrivileges();
    }

    public List<SecurityRole> listRoles()
    {
        return manager.listRoles();
    }

    public List<SecurityUser> listUsers()
    {
        return manager.listUsers();
    }

    public SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        return manager.readPrivilege( id );
    }

    public SecurityRole readRole( String id )
        throws NoSuchRoleException
    {
        return manager.readRole( id );
    }

    public SecurityUser readUser( String id )
        throws NoSuchUserException
    {
        return manager.readUser( id );
    }

    public void save()
    {
        manager.save();

        // some components might need to do something when the config changes. (clear caches etc)
        this.notifyEventHandlers( new SecurityConfigurationChangedEvent() );

        for ( Realm realm : realmLocator.getRealms() )
        {            
            if ( XmlAuthenticatingRealm.class.isAssignableFrom( realm.getClass() ) )
            {
                ( (XmlAuthenticatingRealm) realm ).getConfigurationManager().clearCache();
            }

            if ( XmlAuthorizingRealm.class.isAssignableFrom( realm.getClass() ) )
            {
                ( (XmlAuthorizingRealm) realm ).getAuthorizationCache().clear();
            }
        }
    }

    public void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        updatePrivilege( privilege, null );
    }

    public void updatePrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        manager.updatePrivilege( privilege, context );
        save();
    }

    public void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        updateRole( role, null );
    }

    public void updateRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        manager.updateRole( role, context );
        save();
    }

    public void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        updateUser( user, null );
    }

    public void updateUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        manager.updateUser( user, context );
        save();
    }

    public void changePassword( String userId, String oldPassword, String newPassword )
        throws NoSuchUserException,
            InvalidCredentialsException
    {
        SecurityUser user = readUser( userId );

        String validate = pwGenerator.hashPassword( oldPassword );

        if ( !validate.equals( user.getPassword() ) )
        {
            throw new InvalidCredentialsException();
        }

        // set the password
        changePassword( user, newPassword );
    }

    public void changePassword( String userId, String newPassword )
        throws NoSuchUserException
    {
        SecurityUser user = readUser( userId );
        // set the password
        changePassword( user, newPassword );
    }

    public void changePassword( SecurityUser user, String newPassword )
        throws NoSuchUserException
    {
        user.setPassword( pwGenerator.hashPassword( newPassword ) );

        try
        {
            updateUser( user );
        }
        catch ( InvalidConfigurationException e )
        {
            // Just changing password, can't get into this state
        }
    }

    public void forgotPassword( String userId, String email )
        throws NoSuchUserException,
            NoSuchEmailException
    {
        SecurityUser user = readUser( userId );

        if ( !user.getEmail().equals( email ) )
        {
            throw new NoSuchEmailException( email );
        }

        resetPassword( userId );
    }

    public void forgotUsername( String email, String... ignoredUserIds )
        throws NoSuchEmailException
    {
        List<String> userIds = new ArrayList<String>();

        for ( SecurityUser user : listUsers() )
        {
            if ( Arrays.asList( ignoredUserIds ).contains( user.getId() ) )
            {
                continue;
            }

            if ( user.getEmail().equals( email ) )
            {
                userIds.add( user.getId() );
            }
        }

        if ( userIds.size() > 0 )
        {
            emailer.sendForgotUsername( email, userIds );
        }
        else
        {
            throw new NoSuchEmailException( email );
        }
    }

    public void resetPassword( String userId )
        throws NoSuchUserException
    {
        SecurityUser user = readUser( userId );

        String password = generatePassword( user );

        emailer.sendResetPassword( user.getEmail(), password );

        try
        {
            updateUser( user );
        }
        catch ( InvalidConfigurationException e )
        {
            // cant get here, just reseting password
        }
    }

    private void addInheritedPrivileges( SecurityPrivilege privilege )
    {
        CProperty methodProperty = null;

        for ( CProperty property : (List<CProperty>) privilege.getProperties() )
        {
            if ( property.getKey().equals( "method" ) )
            {
                methodProperty = property;
                break;
            }
        }

        if ( methodProperty != null )
        {
            List<String> inheritedMethods = privInheritance.getInheritedMethods( methodProperty.getValue() );

            StringBuffer buf = new StringBuffer();

            for ( String method : inheritedMethods )
            {
                buf.append( method );
                buf.append( "," );
            }

            if ( buf.length() > 0 )
            {
                buf.setLength( buf.length() - 1 );

                methodProperty.setValue( buf.toString() );
            }
        }
    }

    private String generatePassword( SecurityUser user )
    {
        String password = pwGenerator.generatePassword( 10, 10 );

        user.setPassword( pwGenerator.hashPassword( password ) );

        return password;
    }

    public ValidationContext initializeContext()
    {
        return null;
    }

    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException
    {
        this.manager.createUserRoleMapping( userRoleMapping, context );
        save();
    }

    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException
    {
        this.manager.createUserRoleMapping( userRoleMapping );
        save();
    }

    public void deleteUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        this.manager.deleteUserRoleMapping( userId, source );
        save();
    }

    public List<SecurityUserRoleMapping> listUserRoleMappings()
    {
        return this.manager.listUserRoleMappings();
    }

    public SecurityUserRoleMapping readUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        return this.manager.readUserRoleMapping( userId, source );
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        this.manager.updateUserRoleMapping( userRoleMapping, context );
        save();
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        this.manager.updateUserRoleMapping( userRoleMapping );
        save();
    }

    public List<PrivilegeDescriptor> listPrivilegeDescriptors()
    {
        return this.manager.listPrivilegeDescriptors();
    }

    public void cleanRemovedPrivilege( String privilegeId )
    {
        this.manager.cleanRemovedPrivilege( privilegeId );
    }

    public void cleanRemovedRole( String roleId )
    {
        this.manager.cleanRemovedRole( roleId );
    }
    
    protected Logger getLogger()
    {
        return this.logger;
    }

    public String getAnonymousUsername()
    {
        return "anonymous";
    }

    public boolean isAnonymousAccessEnabled()
    {
        return true;
    }

    public boolean isSecurityEnabled()
    {
        return true;
    }

    public void addSecurityEventHandler( SecurityEventHandler eventHandler )
    {
        this.eventHandlers.add( eventHandler );
    }

    public boolean removeSecurityEventHandler( SecurityEventHandler eventHandler )
    {
        return this.eventHandlers.remove( eventHandler );
    }
    
    protected void notifyEventHandlers( SecurityEvent event )
    {
        for ( SecurityEventHandler handler : this.eventHandlers )
        {
            try
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Notifying component about security event: " + handler.getClass().getName() );
                }

                handler.handleEvent( event );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in SecurityEventHandler: "+ handler.getClass().getName(), e );
            }
        }
    }
}
