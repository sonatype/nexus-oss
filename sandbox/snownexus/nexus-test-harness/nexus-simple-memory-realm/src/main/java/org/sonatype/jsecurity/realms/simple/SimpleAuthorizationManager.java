package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

/**
 * A RoleLocator is used if an external Realm wants to use its Group/Roles in Nexus. For example, your realm might
 * already contain a group for all of your developers. Exposing these roles will allow Nexus to map your Realms roles to
 * Nexus roles more easily.
 */
// This class must have a role of 'RoleLocator', and the hint, must match the result of getSource() and the hint
// of the corresponding Realm.
@Component( role = AuthorizationManager.class, hint = "Simple", description = "Simple Role Locator" )
public class SimpleAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

    public static final String SOURCE = "Simple";

    public String getSource()
    {
        return SOURCE;
    }

    public Set<String> listRoleIds()
    {
        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role-xyz" );
        roleIds.add( "role-abc" );
        roleIds.add( "role-123" );

        return roleIds;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();
        for ( String roleId : this.listRoleIds() )
        {
            roles.add( this.toRole( roleId ) );
        }

        return roles;
    }

    private Role toRole( String roleId )
    {
        Role role = new Role();
        role.setRoleId( roleId );
        role.setSource( this.getSource() );
        role.setName( "Role " + roleId );

        return role;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return null;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        for ( Role role : this.listRoles() )
        {
            if ( role.getRoleId().equals( roleId ) )
            {
                return role;
            }
        }
        throw new NoSuchRoleException( "Role '" + roleId + "' not found." );
    }

    public Set<Privilege> listPrivileges()
    {
        return null;
    }

}
