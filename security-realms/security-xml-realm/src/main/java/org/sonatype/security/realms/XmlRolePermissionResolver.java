package org.sonatype.security.realms;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.StaticSecurityResource;

/**
 * The default implementation of the RolePermissionResolver which reads roles from {@link StaticSecurityResource}s to
 * resolve a role into a collection of permissions. This class allows Realm implementations to no know what/how there
 * roles are used.
 * 
 * @author Brian Demers
 */
@Component( role = RolePermissionResolver.class )
public class XmlRolePermissionResolver
    implements RolePermissionResolver
{

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement( role = PrivilegeDescriptor.class )
    private List<PrivilegeDescriptor> privilegeDescriptors;

    public Collection<Permission> resolvePermissionsInRole( String roleString )
    {

        LinkedList<String> rolesToProcess = new LinkedList<String>();

        rolesToProcess.add( roleString ); // inital role

        Set<String> roleIds = new LinkedHashSet<String>();
        Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            String roleId = rolesToProcess.removeFirst();
            if ( !roleIds.contains( roleId ) )
            {
                CRole role;
                try
                {
                    role = configuration.readRole( roleId );
                    roleIds.add( roleId );

                    // process the roles this role has
                    rolesToProcess.addAll( role.getRoles() );

                    // add the permissions this role has
                    List<String> privilegeIds = role.getPrivileges();
                    for ( String privilegeId : privilegeIds )
                    {
                        Set<Permission> set = getPermissions( privilegeId );
                        permissions.addAll( set );
                    }
                }
                catch ( NoSuchRoleException e )
                {
                    // skip
                }
            }
        }

        return permissions;
    }

    protected Set<Permission> getPermissions( String privilegeId )
    {
        try
        {
            CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );

            for ( PrivilegeDescriptor descriptor : privilegeDescriptors )
            {
                String permission = descriptor.buildPermission( privilege );

                if ( permission != null )
                {
                    return Collections.singleton( (Permission) new WildcardPermission( permission ) );
                }
            }

            return Collections.emptySet();
        }
        catch ( NoSuchPrivilegeException e )
        {
            return Collections.emptySet();
        }
    }

    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }

}
