/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.PermissionFactory;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.StaticSecurityResource;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;

/**
 * The default implementation of the RolePermissionResolver which reads roles from {@link StaticSecurityResource}s to
 * resolve a role into a collection of permissions. This class allows Realm implementations to no know what/how there
 * roles are used.
 *
 * @author Brian Demers
 */
@Singleton
@Typed(RolePermissionResolver.class)
@Named("default")
public class XmlRolePermissionResolver
    implements RolePermissionResolver
{
  private final ConfigurationManager configuration;

  private final List<PrivilegeDescriptor> privilegeDescriptors;

  private final PermissionFactory permissionFactory;

  @Inject
  public XmlRolePermissionResolver(@Named("default") ConfigurationManager configuration,
                                   List<PrivilegeDescriptor> privilegeDescriptors,
                                   @Named("caching") PermissionFactory permissionFactory)
  {
    this.configuration = configuration;
    this.privilegeDescriptors = privilegeDescriptors;
    this.permissionFactory = permissionFactory;
  }

  public Collection<Permission> resolvePermissionsInRole(final String roleString) {
    final LinkedList<String> rolesToProcess = new LinkedList<String>();
    rolesToProcess.add(roleString); // initial role
    final Set<String> processedRoleIds = new LinkedHashSet<String>();
    final Set<Permission> permissions = new LinkedHashSet<Permission>();
    while (!rolesToProcess.isEmpty()) {
      final String roleId = rolesToProcess.removeFirst();
      if (!processedRoleIds.contains(roleId)) {
        try {
          final CRole role = configuration.readRole(roleId);
          processedRoleIds.add(roleId);

          // process the roles this role has recursively
          rolesToProcess.addAll(role.getRoles());
          // add the permissions this role has
          final List<String> privilegeIds = role.getPrivileges();
          for (String privilegeId : privilegeIds) {
            Set<Permission> set = getPermissions(privilegeId);
            permissions.addAll(set);
          }
        }
        catch (NoSuchRoleException e) {
          // skip
        }
      }
    }
    return permissions;
  }

  protected Set<Permission> getPermissions(final String privilegeId) {
    try {
      final CPrivilege privilege = configuration.readPrivilege(privilegeId);
      for (PrivilegeDescriptor descriptor : privilegeDescriptors) {
        final String permission = descriptor.buildPermission(privilege);
        if (permission != null) {
          return Collections.singleton(permissionFactory.create(permission));
        }
      }
      return Collections.emptySet();
    }
    catch (NoSuchPrivilegeException e) {
      return Collections.emptySet();
    }
  }
}
