/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;
import org.sonatype.security.usermanagement.UserManagerImpl;
import org.sonatype.security.usermanagement.UserNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Memory based {@link SecurityModelConfiguration}.
 */
public class Configuration
    implements SecurityModelConfiguration, Serializable, Cloneable
{

  private final ConcurrentMap<String, CUser> users;

  private final ConcurrentMap<String, CRole> roles;

  private final ConcurrentMap<String, CPrivilege> privileges;

  private final ConcurrentMap<String, CUserRoleMapping> userRoleMappings;

  public Configuration() {
    users = Maps.newConcurrentMap();
    roles = Maps.newConcurrentMap();
    privileges = Maps.newConcurrentMap();
    userRoleMappings = Maps.newConcurrentMap();
  }

  @Override
  public List<CUser> getUsers() {
    return ImmutableList.copyOf(users.values());
  }

  @Override
  public CUser getUser(final String id) {
    checkNotNull(id, "user id");
    return users.get(id);
  }

  private void addUser(final CUser user) {
    checkNotNull(user, "user");
    checkNotNull(user.getId(), "user id");
    checkState(
        users.putIfAbsent(user.getId(), user) == null,
        user.getId() + " already exists"
    );
  }

  @Override
  public void addUser(final CUser user, final Set<String> roles) {
    addUser(user);

    CUserRoleMapping mapping = new CUserRoleMapping();
    mapping.setUserId(user.getId());
    mapping.setSource(UserManagerImpl.SOURCE);
    mapping.setRoles(roles);
    addUserRoleMapping(mapping);
  }

  public void setUsers(final Collection<CUser> users) {
    this.users.clear();
    if (users != null) {
      for (CUser user : users) {
        addUser(user);
      }
    }
  }

  @Override
  public void updateUser(final CUser user, final Set<String> roles) throws UserNotFoundException {
    checkNotNull(user, "user");
    checkNotNull(user.getId(), "user id");
    if (users.replace(user.getId(), user) == null) {
      throw new UserNotFoundException("User " + user.getId() + " not found");
    }

    CUserRoleMapping mapping = new CUserRoleMapping();
    mapping.setUserId(user.getId());
    mapping.setSource(UserManagerImpl.SOURCE);
    mapping.setRoles(roles);
    try {
      updateUserRoleMapping(mapping);
    }
    catch (NoSuchRoleMappingException e) {
      addUserRoleMapping(mapping);
    }
  }

  @Override
  public boolean removeUser(final String id) {
    checkNotNull(id, "user id");
    if (users.remove(id) != null) {
      removeUserRoleMapping(id, UserManagerImpl.SOURCE);
      return true;
    }
    return false;
  }

  @Override
  public List<CUserRoleMapping> getUserRoleMappings() {
    return ImmutableList.copyOf(userRoleMappings.values());
  }

  @Override
  public CUserRoleMapping getUserRoleMapping(final String userId, final String source) {
    checkNotNull(userId, "user id");
    checkNotNull(source, "source");
    return userRoleMappings.get(userRoleMappingKey(userId, source));
  }

  @Override
  public void addUserRoleMapping(final CUserRoleMapping mapping) {
    checkNotNull(mapping, "mapping");
    checkNotNull(mapping.getUserId(), "user id");
    checkNotNull(mapping.getSource(), "source");
    checkState(
        userRoleMappings.putIfAbsent(userRoleMappingKey(mapping.getUserId(), mapping.getSource()), mapping) == null,
        mapping.getUserId() + " / " + mapping.getSource() + " already exists"
    );
  }

  public void setUserRoleMappings(final Collection<CUserRoleMapping> mappings) {
    this.userRoleMappings.clear();
    if (mappings != null) {
      for (CUserRoleMapping mapping : mappings) {
        addUserRoleMapping(mapping);
      }
    }
  }

  @Override
  public void updateUserRoleMapping(final CUserRoleMapping mapping) throws NoSuchRoleMappingException {
    checkNotNull(mapping, "mapping");
    checkNotNull(mapping.getUserId(), "user id");
    checkNotNull(mapping.getSource(), "source");
    if (userRoleMappings.replace(userRoleMappingKey(mapping.getUserId(), mapping.getSource()), mapping) == null) {
      throw new NoSuchRoleMappingException("User " + mapping.getUserId() + " role mappings not found");
    }
  }

  @Override
  public boolean removeUserRoleMapping(final String userId, final String source) {
    checkNotNull(userId, "user id");
    checkNotNull(source, "source");
    return userRoleMappings.remove(userRoleMappingKey(userId, source)) != null;
  }

  @Override
  public List<CPrivilege> getPrivileges() {
    return ImmutableList.copyOf(privileges.values());
  }

  @Override
  public CPrivilege getPrivilege(final String id) {
    checkNotNull(id, "privilege id");
    return privileges.get(id);
  }

  @Override
  public void addPrivilege(final CPrivilege privilege) {
    checkNotNull(privilege, "privilege");
    checkNotNull(privilege.getId(), "privilege id");
    checkState(privileges.putIfAbsent(privilege.getId(), privilege) == null, privilege.getId() + " already exists");
  }

  public void setPrivileges(final Collection<CPrivilege> privileges) {
    this.privileges.clear();
    if (privileges != null) {
      for (CPrivilege privilege : privileges) {
        addPrivilege(privilege);
      }
    }
  }

  @Override
  public void updatePrivilege(final CPrivilege privilege) throws NoSuchPrivilegeException {
    checkNotNull(privilege, "privilege");
    checkNotNull(privilege.getId(), "privilege id");
    if (privileges.replace(privilege.getId(), privilege) == null) {
      throw new NoSuchPrivilegeException("Privilege " + privilege.getId() + " not found");
    }
  }

  @Override
  public boolean removePrivilege(final String id) {
    checkNotNull(id, "privilege id");
    return privileges.remove(id) != null;
  }

  @Override
  public List<CRole> getRoles() {
    return ImmutableList.copyOf(roles.values());
  }

  @Override
  public CRole getRole(final String id) {
    checkNotNull(id, "role id");
    return roles.get(id);
  }

  @Override
  public void addRole(final CRole role) {
    checkNotNull(role, "role");
    checkNotNull(role.getId(), "role id");
    checkState(roles.putIfAbsent(role.getId(), role) == null, role.getId() + " already exists");
  }

  public void setRoles(final Collection<CRole> roles) {
    this.roles.clear();
    if (roles != null) {
      for (CRole role : roles) {
        addRole(role);
      }
    }
  }

  @Override
  public void updateRole(final CRole role) throws NoSuchRoleException {
    checkNotNull(role, "role");
    checkNotNull(role.getId(), "role id");
    if (roles.replace(role.getId(), role) == null) {
      throw new NoSuchRoleException("Role " + role.getId() + " not found");
    }
  }

  @Override
  public boolean removeRole(final String id) {
    checkNotNull(id, "role id");
    return roles.remove(id) != null;
  }

  @Override
  public Configuration clone() throws CloneNotSupportedException {
    Configuration copy = (Configuration) super.clone();

    copy.users.putAll(this.users);
    copy.roles.putAll(this.roles);
    copy.privileges.putAll(this.privileges);
    copy.userRoleMappings.putAll(this.userRoleMappings);

    return copy;

  }

  protected String userRoleMappingKey(final String userId, final String source) {
    return userId + "|" + source;
  }

}
