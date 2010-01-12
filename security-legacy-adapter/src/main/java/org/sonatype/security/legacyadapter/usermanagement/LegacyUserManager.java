package org.sonatype.security.legacyadapter.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

/**
 * Wraps up an old PlexusUserLocator to the new UserManager class. This assumes there is only ONE PlexusUserLoctor in
 * the system.
 */
@Component( role = UserManager.class, hint = "legacy" )
public class LegacyUserManager
    extends AbstractReadOnlyUserManager
    implements Initializable
{
    private static final String ROLE = "legacy";

    @Requirement( role = PlexusUserLocator.class )
    private Map<String, PlexusUserLocator> userLocators;

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer plexusContainer;

    public String getAuthenticationRealmName()
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            return ROLE;
        }

        // more then likely it is the source, if not we will add this class to the bottom of the list.
        return this.getUserLocator().getSource();
    }

    public String getSource()
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            return ROLE;
        }
        return this.getUserLocator().getSource();
    }

    public User getUser( String userId )
        throws UserNotFoundException
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            throw new UserNotFoundException( userId, "LegacyUserManager is not configured, it should be removed." );
        }

        PlexusUser plexusUser = this.getUserLocator().getUser( userId );
        if ( plexusUser == null )
        {
            throw new UserNotFoundException( userId );
        }

        return this.toUser( plexusUser );
    }

    public Set<String> listUserIds()
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            return Collections.emptySet();
        }

        return this.getUserLocator().listUserIds();
    }

    public Set<User> listUsers()
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            return Collections.emptySet();
        }

        Set<User> users = new HashSet<User>();

        for ( PlexusUser plexusUser : this.getUserLocator().listUsers() )
        {
            users.add( this.toUser( plexusUser ) );
        }

        return users;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        if ( this.getUserLocator() == null )
        {
            this.logger.debug( "LegacyUserManager is not configured." );
            return Collections.emptySet();
        }

        PlexusUserSearchCriteria pCriteria = new PlexusUserSearchCriteria();
        pCriteria.setUserId( criteria.getUserId() );
        pCriteria.setOneOfRoleIds( criteria.getOneOfRoleIds() );

        Set<User> users = new HashSet<User>();
        for ( PlexusUser plexusUser : this.getUserLocator().searchUsers( pCriteria ) )
        {
            users.add( this.toUser( plexusUser ) );
        }

        return users;
    }

    private PlexusUserLocator getUserLocator()
    {
        if ( this.userLocators.isEmpty() )
        {
            return null;
        }

        PlexusUserLocator plexusUserLocator = this.userLocators.values().iterator().next();
        this.logger.debug( "Found legacy user locator: " + plexusUserLocator.getSource() );
        return plexusUserLocator;
    }

    private User toUser( PlexusUser plexusUser )
    {
        DefaultUser user = new DefaultUser();
        user.setEmailAddress( plexusUser.getEmailAddress() );
        user.setName( plexusUser.getName() );
        user.setReadOnly( true );
        user.setSource( plexusUser.getSource() );
        user.setStatus( UserStatus.active );
        user.setUserId( plexusUser.getUserId() );

        // now for the roles
        for ( PlexusRole pRole : plexusUser.getRoles() )
        {
            user.addRole( new RoleIdentifier( pRole.getSource(), pRole.getRoleId() ) );
        }

        return user;
    }

    public void initialize()
        throws InitializationException
    {
        if ( this.getUserLocator() == null )
        {
            return;
        }

        String newHint = this.getUserLocator().getSource();

        ComponentDescriptor<UserManager> componentDescriptor = this.plexusContainer.getComponentDescriptor(
            UserManager.class,
            UserManager.class.getName(),
            "legacy" );
        componentDescriptor.setRoleHint( newHint );
        try
        {
            System.out.println( "component:\n" + componentDescriptor );
            this.plexusContainer.addComponentDescriptor( componentDescriptor );
            // remove this one, because we replaced it with a new one
            this.plexusContainer.release( this );
        }
        catch ( CycleDetectedInComponentGraphException e )
        {
            this.logger.error( "Failed to load legacy security adapter.", e );
        }
        catch ( ComponentLifecycleException e )
        {
            this.logger.error(
                "Failed to remove temporary legacy UserManager component used to bootstrap the security-legacy-adapter for: "
                    + newHint,
                e );
        }

    }
}
