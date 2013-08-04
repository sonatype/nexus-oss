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

package org.sonatype.security.ldap.authorization;

import java.util.Set;
import java.util.TreeSet;

import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.realms.LdapManager;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = AuthorizationManager.class, hint = "LDAP")
public class LdapAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

  @Requirement
  private LdapManager ldapManager;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public String getSource() {
    return "LDAP";
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

  public Role getRole(String roleId)
      throws NoSuchRoleException
  {
    try {
      String roleName = this.ldapManager.getGroupName(roleId);

      if (roleName == null) {
        throw new NoSuchRoleException("Role: " + roleId + " was not found in LDAP.");
      }

      Role role = new Role();
      role.setName(roleId);
      role.setRoleId(roleId);
      role.setSource(this.getSource());

      return role;
    }
    catch (LdapDAOException e) {
      throw new NoSuchRoleException("Role: " + roleId + " was not found in LDAP.", e);
    }
    catch (NoSuchLdapGroupException e) {
      throw new NoSuchRoleException("Role: " + roleId + " was not found in LDAP.", e);
    }
  }

  public Set<Privilege> listPrivileges() {
    return null;
  }

  public Privilege getPrivilege(String privilegeId)
      throws NoSuchPrivilegeException
  {
    return null;
  }

}
