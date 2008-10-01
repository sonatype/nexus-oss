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
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
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
    @Requirement
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

    public void createPrivilege( CPrivilege privilege )
        throws InvalidConfigurationException
    {
        addInheritedPrivileges( privilege );
        manager.createPrivilege( privilege );
        save();
    }

    public void createRole( CRole role )
        throws InvalidConfigurationException
    {
        manager.createRole( role );
        save();
    }

    public void createUser( CUser user )
        throws InvalidConfigurationException
    {
        String password = generatePassword( user );
        manager.createUser( user );
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

    public String getPrivilegeProperty( CPrivilege privilege, String key )
    {
        return manager.getPrivilegeProperty( privilege, key );
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return manager.getPrivilegeProperty( id, key );
    }

    public List<CPrivilege> listPrivileges()
    {
        return manager.listPrivileges();
    }

    public List<CRole> listRoles()
    {
        return manager.listRoles();
    }

    public List<CUser> listUsers()
    {
        return manager.listUsers();
    }

    public CPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        return manager.readPrivilege( id );
    }

    public CRole readRole( String id )
        throws NoSuchRoleException
    {
        return manager.readRole( id );
    }

    public CUser readUser( String id )
        throws NoSuchUserException
    {
        return manager.readUser( id );
    }

    public void save()
    {
        manager.save();

        notifyProximityEventListeners( new ConfigurationChangeEvent( this ) );
    }

    public void updatePrivilege( CPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        manager.updatePrivilege( privilege );
        save();
    }

    public void updateRole( CRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        manager.updateRole( role );
        save();
    }

    public void updateUser( CUser user )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        manager.updateUser( user );
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
        CUser user = readUser( userId );

        String validate = pwGenerator.hashPassword( oldPassword );

        if ( !validate.equals( user.getPassword() ) )
        {
            throw new InvalidCredentialsException();
        }

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
        CUser user = readUser( userId );

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

        for ( CUser user : listUsers() )
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
        CUser user = readUser( userId );

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

    private void addInheritedPrivileges( CPrivilege privilege )
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

    private String generatePassword( CUser user )
    {
        String password = pwGenerator.generatePassword( 10, 10 );

        user.setPassword( pwGenerator.hashPassword( password ) );

        return password;
    }
}
