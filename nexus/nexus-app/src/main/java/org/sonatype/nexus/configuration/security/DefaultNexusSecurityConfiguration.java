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
package org.sonatype.nexus.configuration.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.runtime.SecurityRuntimeConfigurationBuilder;
import org.sonatype.nexus.configuration.security.source.SecurityConfigurationSource;
import org.sonatype.nexus.configuration.security.validator.SecurityConfigurationValidator;
import org.sonatype.nexus.configuration.security.validator.SecurityValidationContext;
import org.sonatype.nexus.configuration.security.validator.SecurityValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.smtp.SmtpClient;

/**
 * The class DefaultNexusSecurityConfiguration is responsible for config management. It actually keeps in sync Nexus
 * internal state with persisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus
 * current state and Nexus user config.
 * 
 * @author cstamas
 * @plexus.component
 */

public class DefaultNexusSecurityConfiguration
    extends AbstractLogEnabled
    implements NexusSecurityConfiguration
{
    /**
     * The nexus configuration. Used to initialize the list of repo targets
     * 
     * @plexus.requirement
     */
    private NexusConfiguration nexusConfiguration;

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The configuration source.
     * 
     * @plexus.requirement role-hint="file"
     */
    private SecurityConfigurationSource configurationSource;

    /**
     * The smtp client for sending mails.
     * 
     * @plexus.requirement
     */
    private SmtpClient smtpClient;

    /**
     * The config validator.
     * 
     * @plexus.requirement
     */
    private SecurityConfigurationValidator configurationValidator;

    /**
     * @plexus.requirement
     */
    private PasswordGenerator pwGenerator;

    /**
     * The runtime configuration builder.
     * 
     * @plexus.requirement
     */
    private SecurityRuntimeConfigurationBuilder runtimeConfigurationBuilder;

    /**
     * @plexus.requirement
     */
    private PrivilegeInheritanceManager privInheritance;

    /** The config event listeners. */
    private CopyOnWriteArrayList<ConfigurationChangeListener> configurationChangeListeners = new CopyOnWriteArrayList<ConfigurationChangeListener>();

    public void startService()
        throws StartingException
    {
        try
        {
            loadConfiguration( true );

            notifyConfigurationChangeListeners();

            getLogger().info( "Started Nexus Security" );
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "FAILED Starting Nexus Security", e );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            getLogger().error( "FAILED Starting Nexus Security", e );
        }
    }

    public void stopService()
        throws StoppingException
    {
        getLogger().info( "Stopped Nexus Security" );
    }

    public void loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        loadConfiguration( false );
    }

    public void loadConfiguration( boolean force )
        throws ConfigurationException,
            IOException
    {
        if ( force || configurationSource.getConfiguration() == null )
        {
            getLogger().debug( "Loading Nexus Security Configuration..." );

            configurationSource.loadConfiguration();

            // and register things
            runtimeConfigurationBuilder.initialize( this );

            notifyConfigurationChangeListeners();
        }
    }

    public void applyConfiguration()
        throws IOException
    {
        getLogger().info( "Applying Nexus Security Configuration..." );

        notifyConfigurationChangeListeners();
    }

    public void saveConfiguration()
        throws IOException
    {
        configurationSource.storeConfiguration();
    }

    protected void applyAndSaveConfiguration()
        throws IOException
    {
        applyConfiguration();

        saveConfiguration();
    }

    public void addConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        configurationChangeListeners.add( listener );
    }

    public void removeConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        configurationChangeListeners.remove( listener );
    }

    public void notifyConfigurationChangeListeners()
    {
        notifyConfigurationChangeListeners( new ConfigurationChangeEvent( this ) );
    }

    public void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt )
    {
        for ( ConfigurationChangeListener l : configurationChangeListeners )
        {
            try
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Notifying component about config change: " + l.getClass().getName() );
                }

                l.onConfigurationChange( evt );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in listener", e );
            }
        }
    }

    public Configuration getConfiguration()
    {
        return configurationSource.getConfiguration();
    }

    public SecurityConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return configurationSource.getConfigurationAsStream();
    }

    public boolean isInstanceUpgraded()
    {
        // TODO: this is not quite true: we might keep model ver but upgrade JARs of Nexus only in a release
        // we should store the nexus version somewhere in working storage and trigger some household stuff
        // if version changes.
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationUpgraded()
    {
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationDefaulted()
    {
        return configurationSource.isConfigurationDefaulted();
    }

    /**
     * User CRUD
     */
    public Collection<CUser> listUsers()
    {
        return new ArrayList<CUser>( getConfiguration().getUsers() );
    }

    private String generateNewPassword( CUser settings )
    {
        String password = pwGenerator.generatePassword( 10, 10 );

        settings.setPassword( pwGenerator.hashPassword( password ) );

        // settings.setStatus( CUser.STATUS_EXPIRED );

        return password;
    }

    public void createUser( CUser settings )
        throws ConfigurationException,
            IOException
    {
        // On create we need to generate a new password, and email the user their new password

        String password = generateNewPassword( settings );

        ValidationResponse vr = configurationValidator.validateUser( initializeContext(), settings, false );

        if ( vr.isValid() )
        {
            // TODO: anything needs to be done for the runtime configuration?
            getConfiguration().getUsers().add( settings );

            applyAndSaveConfiguration();

            smtpClient.sendEmailAsync( settings.getEmail(), null, "Nexus: New user account created.", "User Account "
                + settings.getUserId()
                + " has been created.  Another email will be sent shortly containing your password." );
            smtpClient.sendEmailAsync(
                settings.getEmail(),
                null,
                "Nexus: New user account created.",
                "Your new password is " + password );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public CUser readUser( String id )
        throws NoSuchUserException
    {
        List<CUser> users = getConfiguration().getUsers();

        for ( CUser user : users )
        {
            if ( user.getUserId().equals( id ) )
            {
                return user;
            }
        }

        throw new NoSuchUserException( id );
    }

    public void updateUser( CUser settings )
        throws NoSuchUserException,
            ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateUser( initializeContext(), settings, true );

        if ( vr.isValid() )
        {
            List<CUser> users = getConfiguration().getUsers();

            for ( int i = 0; i < users.size(); i++ )
            {
                CUser user = users.get( i );

                if ( user.getUserId().equals( settings.getUserId() ) )
                {
                    users.remove( i );

                    users.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }

            throw new NoSuchUserException( settings.getUserId() );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void deleteUser( String id )
        throws NoSuchUserException,
            IOException
    {
        List<CUser> users = getConfiguration().getUsers();

        for ( Iterator<CUser> i = users.iterator(); i.hasNext(); )
        {
            CUser user = i.next();

            if ( user.getUserId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchUserException( id );
    }

    public void resetPassword( String id )
        throws IOException,
            NoSuchUserException
    {
        CUser user = readUser( id );

        // Not currently supported
        // user.setStatus( CUser.STATUS_EXPIRED );

        String password = generateNewPassword( user );

        applyAndSaveConfiguration();

        smtpClient.sendEmailAsync(
            user.getEmail(),
            null,
            "Nexus: User account notification.",
            "Your password has been reset.  Your new password is: " + password );
    }

    public void forgotPassword( String userId, String email )
        throws IOException,
            NoSuchUserException,
            NoSuchEmailException
    {
        CUser user = readUser( userId );

        if ( user.getEmail().equals( email ) )
        {
            resetPassword( userId );
        }
        else
        {
            throw new NoSuchEmailException( email );
        }
    }

    public void forgotUserId( String email )
        throws IOException,
            NoSuchEmailException
    {
        List<CUser> users = getConfiguration().getUsers();

        ArrayList<String> userIds = new ArrayList<String>();

        for ( Iterator<CUser> i = users.iterator(); i.hasNext(); )
        {
            CUser user = i.next();

            if ( user.getEmail().equals( email ) )
            {
                userIds.add( user.getUserId() );
            }

        }

        if ( userIds.size() > 0 )
        {
            StringBuffer sb = new StringBuffer( "Your email is associated with the following Nexus User Id(s):\n " );

            for ( String userId : userIds )
            {
                sb.append( "\n" );
                
                sb.append( " o " );
                
                sb.append( "'" );

                sb.append( userId );

                sb.append( "'" );
            }

            smtpClient.sendEmailAsync( email, null, "Nexus: User account notification.", sb.toString() );
        }
        else
        {
            throw new NoSuchEmailException( email );
        }
    }

    public void changePassword( String userId, String oldPassword, String newPassword )
        throws IOException,
            NoSuchUserException,
            InvalidCredentialsException
    {
        CUser user = readUser( userId );

        String validate = pwGenerator.hashPassword( oldPassword );

        if ( !validate.equals( user.getPassword() ) )
        {
            throw new InvalidCredentialsException();
        }

        user.setStatus( CUser.STATUS_ACTIVE );
        user.setPassword( pwGenerator.hashPassword( newPassword ) );
        
        applyAndSaveConfiguration();
    }

    /**
     * Role CRUD
     */
    public Collection<CRole> listRoles()
    {
        return new ArrayList<CRole>( getConfiguration().getRoles() );
    }

    public void createRole( CRole settings )
        throws ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateRole( initializeContext(), settings, false );

        if ( vr.isValid() )
        {
            // TODO: anything needs to be done for the runtime configuration?
            getConfiguration().getRoles().add( settings );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public CRole readRole( String id )
        throws NoSuchRoleException
    {
        List<CRole> roles = getConfiguration().getRoles();

        for ( CRole role : roles )
        {
            if ( role.getId().equals( id ) )
            {
                return role;
            }
        }

        throw new NoSuchRoleException( id );
    }

    public void updateRole( CRole settings )
        throws NoSuchRoleException,
            ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateRole( initializeContext(), settings, true );

        if ( vr.isValid() )
        {
            List<CRole> roles = getConfiguration().getRoles();

            for ( int i = 0; i < roles.size(); i++ )
            {
                CRole role = roles.get( i );

                if ( role.getId().equals( settings.getId() ) )
                {
                    roles.remove( i );

                    roles.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }

            throw new NoSuchRoleException( settings.getId() );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void deleteRole( String id )
        throws NoSuchRoleException,
            IOException
    {
        List<CRole> roles = getConfiguration().getRoles();

        for ( Iterator<CRole> i = roles.iterator(); i.hasNext(); )
        {
            CRole role = i.next();

            if ( role.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchRoleException( id );
    }

    /**
     * Application Privilege CRUD
     */
    public Collection<CApplicationPrivilege> listApplicationPrivileges()
    {
        return new ArrayList<CApplicationPrivilege>( getConfiguration().getApplicationPrivileges() );
    }

    public void createApplicationPrivilege( CApplicationPrivilege settings )
        throws ConfigurationException,
            IOException
    {
        addInheritedPrivileges( settings );

        ValidationResponse vr = configurationValidator.validateApplicationPrivilege(
            initializeContext(),
            settings,
            false );

        if ( vr.isValid() )
        {
            // TODO: anything needs to be done for the runtime configuration?
            getConfiguration().getApplicationPrivileges().add( settings );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public CApplicationPrivilege readApplicationPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        List<CApplicationPrivilege> privs = getConfiguration().getApplicationPrivileges();

        for ( CApplicationPrivilege priv : privs )
        {
            if ( priv.getId().equals( id ) )
            {
                return priv;
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    public void updateApplicationPrivilege( CApplicationPrivilege settings )
        throws NoSuchPrivilegeException,
            ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateApplicationPrivilege(
            initializeContext(),
            settings,
            true );

        if ( vr.isValid() )
        {
            List<CApplicationPrivilege> privs = getConfiguration().getApplicationPrivileges();

            for ( int i = 0; i < privs.size(); i++ )
            {
                CApplicationPrivilege priv = privs.get( i );

                if ( priv.getId().equals( settings.getId() ) )
                {
                    privs.remove( i );

                    privs.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }

            throw new NoSuchPrivilegeException( settings.getId() );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void deleteApplicationPrivilege( String id )
        throws NoSuchPrivilegeException,
            IOException
    {
        List<CApplicationPrivilege> privs = getConfiguration().getApplicationPrivileges();

        for ( Iterator<CApplicationPrivilege> i = privs.iterator(); i.hasNext(); )
        {
            CApplicationPrivilege priv = i.next();

            if ( priv.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    /**
     * Repository Target Privilege CRUD
     */
    public Collection<CRepoTargetPrivilege> listRepoTargetPrivileges()
    {
        return new ArrayList<CRepoTargetPrivilege>( getConfiguration().getRepositoryTargetPrivileges() );
    }

    private ValidationResponse crossValidateRepoTargetPrivilege( CRepoTargetPrivilege settings )
    {
        ValidationResponse vr = new SecurityValidationResponse();

        CRepositoryTarget target = nexusConfiguration.readRepositoryTarget( settings.getRepositoryTargetId() );

        // Invalid target ID
        if ( target == null )
        {
            ValidationMessage error = new ValidationMessage(
                "repositoryTargetId",
                "Privilege ID '" + settings.getId() + "' has invalid repository target ID '"
                    + settings.getRepositoryTargetId() + "'",
                "Repository Target doesn't exist" );
            vr.addValidationError( error );
        }
        else
        {
            if ( !StringUtils.isEmpty( settings.getRepositoryId() ) )
            {
                try
                {
                    CRepository repo = nexusConfiguration.readRepository( settings.getRepositoryId() );

                    // Invalid Repo/Target content types
                    if ( !repo.getType().equals( target.getContentClass() ) )
                    {
                        ValidationMessage error = new ValidationMessage(
                            "repositoryId",
                            "Privilege ID '" + settings.getId()
                                + "' has repository and repository target of different types",
                            "Content type differs between repository and target." );
                        vr.addValidationError( error );
                    }
                }
                // Invalid repo selection
                catch ( NoSuchRepositoryException e )
                {
                    ValidationMessage error = new ValidationMessage( "repositoryId", "Privilege ID '"
                        + settings.getId() + "' has invalid repository ID '" + settings.getRepositoryId() + "'", e
                        .getMessage() );
                    vr.addValidationError( error );
                }
            }
            else if ( !StringUtils.isEmpty( settings.getGroupId() ) )
            {
                try
                {
                    ContentClass content = repositoryRegistry.getRepositoryGroupContentClass( settings.getGroupId() );

                    // Invalid group/target content types
                    if ( !content.getId().equals( target.getContentClass() ) )
                    {
                        ValidationMessage error = new ValidationMessage(
                            "repositoryGroupId",
                            "Privilege ID '" + settings.getId()
                                + "' has repository group and repository target of different types",
                            "Content type differs between repository group and target." );
                        vr.addValidationError( error );
                    }
                }
                // Invalid group selection
                catch ( NoSuchRepositoryGroupException e )
                {
                    ValidationMessage error = new ValidationMessage( "repositoryGroupId", "Privilege ID '"
                        + settings.getId() + "' has invalid repository group ID '" + settings.getGroupId() + "'", e
                        .getMessage() );
                    vr.addValidationError( error );
                }
            }
            else
            {
                // All is well
            }
        }

        return vr;
    }

    public void createRepoTargetPrivilege( CRepoTargetPrivilege settings )
        throws ConfigurationException,
            IOException
    {
        addInheritedPrivileges( settings );

        ValidationResponse vr = configurationValidator.validateRepoTargetPrivilege(
            initializeContext(),
            settings,
            false );

        vr.append( crossValidateRepoTargetPrivilege( settings ) );

        if ( vr.isValid() )
        {
            // TODO: anything needs to be done for the runtime configuration?
            getConfiguration().getRepositoryTargetPrivileges().add( settings );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    private void addInheritedPrivileges( CPrivilege settings )
    {
        Set<String> inheritedMethods = privInheritance.getInheritedMethods( settings.getMethod() );

        StringBuffer buf = new StringBuffer();

        for ( String method : inheritedMethods )
        {
            buf.append( method );
            buf.append( "," );
        }

        if ( buf.length() > 0 )
        {
            buf.setLength( buf.length() - 1 );
            settings.setMethod( buf.toString() );
        }
    }

    public CRepoTargetPrivilege readRepoTargetPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        List<CRepoTargetPrivilege> privs = getConfiguration().getRepositoryTargetPrivileges();

        for ( CRepoTargetPrivilege priv : privs )
        {
            if ( priv.getId().equals( id ) )
            {
                return priv;
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    public void updateRepoTargetPrivilege( CRepoTargetPrivilege settings )
        throws NoSuchPrivilegeException,
            ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator
            .validateRepoTargetPrivilege( initializeContext(), settings, true );

        vr.append( crossValidateRepoTargetPrivilege( settings ) );

        if ( vr.isValid() )
        {
            List<CRepoTargetPrivilege> privs = getConfiguration().getRepositoryTargetPrivileges();

            for ( int i = 0; i < privs.size(); i++ )
            {
                CRepoTargetPrivilege priv = privs.get( i );

                if ( priv.getId().equals( settings.getId() ) )
                {
                    privs.remove( i );

                    privs.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }

            throw new NoSuchPrivilegeException( settings.getId() );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void deleteRepoTargetPrivilege( String id )
        throws NoSuchPrivilegeException,
            IOException
    {
        List<CRepoTargetPrivilege> privs = getConfiguration().getRepositoryTargetPrivileges();

        for ( Iterator<CRepoTargetPrivilege> i = privs.iterator(); i.hasNext(); )
        {
            CRepoTargetPrivilege priv = i.next();

            if ( priv.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    private SecurityValidationContext initializeContext()
    {
        SecurityValidationContext context = new SecurityValidationContext();

        context.addExistingUserIds();
        context.addExistingRoleIds();
        context.addExistingPrivilegeIds();

        for ( CUser user : listUsers() )
        {
            context.getExistingUserIds().add( user.getUserId() );
            context.getExistingEmailMap().put( user.getUserId(), user.getEmail() );
        }

        for ( CRole role : listRoles() )
        {
            context.getExistingRoleIds().add( role.getId() );

            ArrayList<String> containedRoles = new ArrayList<String>();

            containedRoles.addAll( role.getRoles() );

            context.getRoleContainmentMap().put( role.getId(), containedRoles );
        }

        for ( CPrivilege priv : listApplicationPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        for ( CPrivilege priv : listRepoTargetPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        return context;
    }
}
