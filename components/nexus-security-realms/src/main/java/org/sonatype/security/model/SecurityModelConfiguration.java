/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.model;

import java.util.Collection;
import java.util.List;

/**
 * @since 3.0
 */
public interface SecurityModelConfiguration
    extends org.sonatype.configuration.Configuration, java.io.Serializable, Cloneable
{
  List<CUser> getUsers();

  CUser getUser(String id);

  void addUser(CUser user);

  void setUsers(Collection<CUser> users);

  boolean removeUser(String id);

  List<CUserRoleMapping> getUserRoleMappings();

  CUserRoleMapping getUserRoleMapping(String userId, String source);

  void addUserRoleMapping(CUserRoleMapping mapping);

  void setUserRoleMappings(Collection<CUserRoleMapping> mappings);

  boolean removeUserRoleMapping(String userId, String source);

  List<CPrivilege> getPrivileges();

  CPrivilege getPrivilege(String id);

  void addPrivilege(CPrivilege privilege);

  void setPrivileges(Collection<CPrivilege> privileges);

  boolean removePrivilege(String id);

  List<CRole> getRoles();

  CRole getRole(String id);

  void addRole(CRole role);

  void setRoles(Collection<CRole> roles);

  boolean removeRole(String id);
}
