package org.sonatype.nexus.jsecurity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.source.SecurityConfigurationSource;
import org.sonatype.nexus.email.NexusEmailer;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;

@Component( role = NexusSecurity.class )
public class DefaultNexusSecurity
    extends AbstractLogEnabled
    implements NexusSecurity
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
    private NexusEmailer emailer;

    private List<EventListener> listeners = new ArrayList<EventListener>();
    
    public void startService()
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

        getLogger().info( "Started Nexus Security" );
    }

    public void stopService()
        throws StoppingException
    {
        getLogger().info( "Stopped Nexus Security" );
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
        if( StringUtils.isEmpty( password ))
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

        notifyProximityEventListeners( new ConfigurationChangeEvent( this ) );
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

    public void addProximityEventListener( EventListener listener )
    {
        listeners.add( listener );
    }

    public void removeProximityEventListener( EventListener listener )
    {
        listeners.remove( listener );
    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        for ( EventListener l : listeners )
        {
            try
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Notifying component about security config change: " + l.getClass().getName() );
                }

                l.onProximityEvent( evt );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in listener", e );
            }
        }
    }

    /**
     * @deprecated Use save()
     */
    public void saveConfiguration()
        throws IOException
    {
        // Don't use this for security
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
    
    public void changePassword( String userId, String newPassword ) throws NoSuchUserException
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

    public void forgotUsername( String email )
        throws NoSuchEmailException
    {
        List<String> userIds = new ArrayList<String>();

        for ( SecurityUser user : listUsers() )
        {
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
            Set<String> inheritedMethods = privInheritance.getInheritedMethods( methodProperty.getValue() );

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
}
