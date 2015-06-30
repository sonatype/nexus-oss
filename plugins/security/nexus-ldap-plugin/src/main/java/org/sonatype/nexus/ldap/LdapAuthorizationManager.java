/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ldap.internal.LdapConstants;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapGroupException;
import org.sonatype.nexus.ldap.internal.realms.LdapManager;
import org.sonatype.nexus.security.authz.AbstractReadOnlyAuthorizationManager;
import org.sonatype.nexus.security.privilege.NoSuchPrivilegeException;
import org.sonatype.nexus.security.privilege.Privilege;
import org.sonatype.nexus.security.role.NoSuchRoleException;
import org.sonatype.nexus.security.role.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Named(LdapConstants.USER_SOURCE)
public class LdapAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

  private static final Logger logger = LoggerFactory.getLogger(LdapAuthorizationManager.class);

  private final LdapManager ldapManager;

  @Inject
  public LdapAuthorizationManager(final LdapManager ldapManager) {
    this.ldapManager = checkNotNull(ldapManager);
  }

  @Override
  public String getSource() {
    return LdapConstants.USER_SOURCE;
  }

  public Set<String> listRoleIds() {
    Set<String> result = null;
    try {
      result = ldapManager.getAllGroups();
    }
    catch (LdapDAOException e) {
      this.logger.debug("Problem getting list of LDAP Groups: " + e.getMessage(), e);
    }
    return result;
  }

  @Override
  public Set<Role> listRoles() {
    Set<Role> result = new TreeSet<Role>();
    try {
      for (String roleId : ldapManager.getAllGroups()) {
        Role role = new Role();
        role.setName(roleId);
        role.setRoleId(roleId);
        role.setSource(this.getSource());
        result.add(role);
      }
    }
    catch (LdapDAOException e) {
      this.logger.debug("Problem getting list of LDAP Groups: " + e.getMessage(), e);
    }
    return result;

  }

  @Override
  public Role getRole(String roleId) throws NoSuchRoleException {
    try {
      String roleName = this.ldapManager.getGroupName(roleId);

      if (roleName == null) {
        throw new NoSuchRoleException(roleId);
      }

      Role role = new Role();
      role.setName(roleId);
      role.setRoleId(roleId);
      role.setSource(this.getSource());

      return role;
    }
    catch (LdapDAOException | NoSuchLdapGroupException e) {
      throw new NoSuchRoleException(roleId, e);
    }
  }

  @Override
  public Set<Privilege> listPrivileges() {
    return null;
  }

  @Override
  public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
    return null;
  }
}
