package org.sonatype.security.mock.authorization;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.Role;

@Component( role = AuthorizationManager.class, hint = "sourceB" )
public class MockAuthorizationManagerB
    implements AuthorizationManager
{

    public String getSourceId()
    {
        return "sourceB";
    }

    public Set<String> getPermissions()
    {
        Set<String> permissions = new HashSet<String>();

        permissions.add( "from-role:read" );
        permissions.add( "from-role:delete" );

        return permissions;
    }

    public Set<Role> getRoles()
    {
        Set<Role> roles = new HashSet<Role>();

        Role role1 = new Role( "test-role1", "Role 1", this.getSourceId() );
        role1.addPermission( "from-role1:read" );
        role1.addPermission( "from-role1:delete" );

        Role role2 = new Role( "test-role2", "Role 2", this.getSourceId() );
        role2.addPermission( "from-role2:read" );
        role2.addPermission( "from-role2:delete" );

        roles.add( role1 );
        roles.add( role2 );

        return roles;
    }

}
