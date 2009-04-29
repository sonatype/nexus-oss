package org.sonatype.security.authorization;

import java.util.Set;

public interface AuthorizationManager
{
    public String getSource();

    // ROLE CRUDS
    public Set<Role> listRoles();

    public Role getRole( String roleId )
        throws NoSuchRoleException;

    public Role addRole( Role role );

    public Role updateRole( Role role )
        throws NoSuchRoleException;

    public void deleteRole( String roleId )
        throws NoSuchRoleException;

    
    // Privilege CRUDS
    public Set<Privilege> listPrivileges();

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException;

    public Privilege addPrivilege( Privilege privilege );

    public Privilege upatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException;

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException;
}
