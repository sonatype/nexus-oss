package org.sonatype.security.mock;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

@Singleton
@Named( value = "Mock" )
@Typed( value = AuthorizationManager.class )
public class MockAuthorizationManager extends AbstractReadOnlyAuthorizationManager
{

    public String getSource()
    {
        return "Mock";
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();
        
        roles.add( new Role( "mockrole1", "MockRole1", "Mock Role1", "Mock", true, null, null ) );
        roles.add( new Role( "mockrole2", "MockRole2", "Mock Role2", "Mock", true, null, null ) );
        roles.add( new Role( "mockrole3", "MockRole3", "Mock Role3", "Mock", true, null, null ) );
        
        return roles;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        for ( Role role : this.listRoles() )
        {
            if( roleId.equals( role.getRoleId() ) )
            {
                return role;
            }
        }
        throw new NoSuchRoleException( "Role: "+ roleId + " could not be found." );
    }

    public Set<Privilege> listPrivileges()
    {
        return new HashSet<Privilege>();
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        throw new NoSuchPrivilegeException( "Privilege: "+ privilegeId + " could not be found." );
    }

}
