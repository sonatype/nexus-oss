/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.PermissionFactory;
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
@Singleton
@Typed( RolePermissionResolver.class )
@Named( "default" )
public class XmlRolePermissionResolver
    implements RolePermissionResolver
{
    private final ConfigurationManager configuration;

    private final List<PrivilegeDescriptor> privilegeDescriptors;

    private final PermissionFactory permissionFactory;

    @Inject
    public XmlRolePermissionResolver( @Named( "default" ) ConfigurationManager configuration,
                                      List<PrivilegeDescriptor> privilegeDescriptors,
                                      @Named( "caching" ) PermissionFactory permissionFactory )
    {
        this.configuration = configuration;
        this.privilegeDescriptors = privilegeDescriptors;
        this.permissionFactory = permissionFactory;
    }

    public Collection<Permission> resolvePermissionsInRole( final String roleString )
    {
        final LinkedList<String> rolesToProcess = new LinkedList<String>();
        rolesToProcess.add( roleString ); // initial role
        final Set<String> processedRoleIds = new LinkedHashSet<String>();
        final Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            final String roleId = rolesToProcess.removeFirst();
            if ( !processedRoleIds.contains( roleId ) )
            {
                try
                {
                    final CRole role = configuration.readRole( roleId );
                    processedRoleIds.add( roleId );

                    // process the roles this role has recursively
                    rolesToProcess.addAll( role.getRoles() );
                    // add the permissions this role has
                    final List<String> privilegeIds = role.getPrivileges();
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

    protected Set<Permission> getPermissions( final String privilegeId )
    {
        try
        {
            final CPrivilege privilege = configuration.readPrivilege( privilegeId );
            for ( PrivilegeDescriptor descriptor : privilegeDescriptors )
            {
                final String permission = descriptor.buildPermission( privilege );
                if ( permission != null )
                {
                    return Collections.singleton( permissionFactory.create( permission ) );
                }
            }
            return Collections.emptySet();
        }
        catch ( NoSuchPrivilegeException e )
        {
            return Collections.emptySet();
        }
    }
}
