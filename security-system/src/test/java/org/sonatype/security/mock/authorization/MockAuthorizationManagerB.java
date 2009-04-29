package org.sonatype.security.mock.authorization;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

@Component( role = AuthorizationManager.class, hint = "sourceB" )
public class MockAuthorizationManagerB
    implements AuthorizationManager
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

    public Privilege getPrivilege()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role getRole( String roleId )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Privilege> listPrivileges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege addPrivilege( Privilege privilege )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role addRole( Role role )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        
    }

    public void deleteRole( String roleId )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub
        
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege upatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
