package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;

/**
 * A PlexusRoleLocator is used if an external Realm wants to use its Group/Roles in Nexus. For example, your realm might
 * already contain a group for all of your developers. Exposing these roles will allow Nexus to map your Realms roles to
 * Nexus roles more easily.
 */
//This class must have a role of 'PlexusRoleLocator', and the hint, must match the result of getSource() and the hint
//of the corresponding Realm.
@Component( role = PlexusRoleLocator.class, hint = "Simple", description = "Simple Role Locator" )
public class SimpleRoleLocator
    implements PlexusRoleLocator
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

    public Set<PlexusRole> listRoles()
    {
        Set<PlexusRole> roles = new HashSet<PlexusRole>();
        for ( String roleId : this.listRoleIds() )
        {
            roles.add( this.toPlexusRole( roleId ) );
        }

        return roles;
    }

    private PlexusRole toPlexusRole( String roleId )
    {
        PlexusRole role = new PlexusRole();
        role.setRoleId( roleId );
        role.setSource( this.getSource() );
        role.setName( "Role " + roleId );

        return role;
    }

}
