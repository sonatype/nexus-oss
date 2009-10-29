package org.sonatype.security.legacyadapter.authorization;

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
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.UserManager;

/**
 * Wraps up an old RoleLocator to the new AuthorizationManager class. This assumes there is only ONE
 * AuthorizationManager in the system.
 */
@Component( role = AuthorizationManager.class, hint = "legacy" )
public class LegacyAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
    implements Initializable
{
    @Requirement( role = PlexusRoleLocator.class )
    Map<String, PlexusRoleLocator> roleLocators;

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer plexusContainer;

    private static final String ROLE = "legacy";

    public String getSource()
    {
        if ( this.getPlexusRoleLocator() == null )
        {
            this.logger.warn( "LegacyAuthorizationManager is not configured, it should be removed." );
            return ROLE;
        }

        return this.getPlexusRoleLocator().getSource();
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        if ( this.getPlexusRoleLocator() == null )
        {
            this.logger.warn( "LegacyAuthorizationManager is not configured, it should be removed." );
            throw new NoSuchRoleException( "LegacyAuthorizationManager is not configured, it should be removed." );
        }

        for ( PlexusRole plexusRole : this.getPlexusRoleLocator().listRoles() )
        {
            return this.toRole( plexusRole );
        }

        throw new NoSuchRoleException( "Failed to find role :" + roleId + " from legacy PlexusRoleLocator: "
            + this.getPlexusRoleLocator().getSource() );
    }

    public Set<Role> listRoles()
    {
        if ( this.getPlexusRoleLocator() == null )
        {
            this.logger.warn( "LegacyAuthorizationManager is not configured, it should be removed." );
            return Collections.emptySet();
        }

        Set<Role> roles = new HashSet<Role>();
        for ( PlexusRole plexusRole : this.getPlexusRoleLocator().listRoles() )
        {
            roles.add( this.toRole( plexusRole ) );
        }

        return roles;
    }

    public Set<Privilege> listPrivileges()
    {
        // null is fine here
        return null;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // null is fine here
        return null;
    }

    private PlexusRoleLocator getPlexusRoleLocator()
    {
        if ( this.roleLocators.isEmpty() )
        {
            return null;
        }

        PlexusRoleLocator plexusRoleLocator = this.roleLocators.values().iterator().next();
        this.logger.debug( "Found legacy role locator: " + plexusRoleLocator.getSource() );
        return plexusRoleLocator;
    }

    private Role toRole( PlexusRole plexusRole )
    {
        Role role = new Role();
        role.setDescription( plexusRole.getName() );
        role.setName( plexusRole.getName() );
        role.setReadOnly( true );
        role.setSessionTimeout( 60 ); // no longer used
        role.setSource( plexusRole.getSource() );
        role.setRoleId( plexusRole.getRoleId() );

        return role;
    }

    public void initialize()
        throws InitializationException
    {
        if ( this.getPlexusRoleLocator() == null )
        {
            return;
        }

        String newHint = this.getPlexusRoleLocator().getSource();

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
