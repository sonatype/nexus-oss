package org.sonatype.security.realms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;

public class PermissionUtil
{

    static Set<Permission> buildPermissions( Collection<PrivilegeDescriptor> privilegeDescriptors, CPrivilege privilege )
    {

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
}
