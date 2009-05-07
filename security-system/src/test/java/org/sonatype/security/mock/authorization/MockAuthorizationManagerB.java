package org.sonatype.security.mock.authorization;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

@Component( role = AuthorizationManager.class, hint = "sourceB" )
public class MockAuthorizationManagerB extends AbstractReadOnlyAuthorizationManager
{

    public String getSource()
    {
        return "sourceB";
    }

    public Set<String> listPermissions()
    {
        Set<String> permissions = new HashSet<String>();

        permissions.add( "from-role:read" );
        permissions.add( "from-role:delete" );

        return permissions;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();

        Role role1 = new Role( "test-role1", "Role 1", this.getSource() );
        role1.addPermission( "from-role1:read" );
        role1.addPermission( "from-role1:delete" );

        Role role2 = new Role( "test-role2", "Role 2", this.getSource() );
        role2.addPermission( "from-role2:read" );
        role2.addPermission( "from-role2:delete" );

        roles.add( role1 );
        roles.add( role2 );

        return roles;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return null;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        return null;
    }

    public Set<Privilege> listPrivileges()
    {
        return null;
    }

}
