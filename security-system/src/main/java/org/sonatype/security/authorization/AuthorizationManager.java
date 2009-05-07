package org.sonatype.security.authorization;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

public interface AuthorizationManager
{
    public String getSource();
    
    boolean supportsWrite();

    // ROLE CRUDS
    public Set<Role> listRoles();

    public Role getRole( String roleId )
        throws NoSuchRoleException;

    public Role addRole( Role role ) throws InvalidConfigurationException;

    public Role updateRole( Role role )
        throws NoSuchRoleException, InvalidConfigurationException;

    public void deleteRole( String roleId )
        throws NoSuchRoleException;

    
    // Privilege CRUDS
    public Set<Privilege> listPrivileges();

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException;

    public Privilege addPrivilege( Privilege privilege ) throws InvalidConfigurationException;

    public Privilege updatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException, InvalidConfigurationException;

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException;
}
