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
package org.sonatype.security.authorization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.events.AuthorizationConfigurationChanged;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Default {@link AuthorizationManager}.
 */
@Singleton
@Typed(AuthorizationManager.class)
@Named("default")
public class AuthorizationManagerImpl
    implements AuthorizationManager
{
  public static final String SOURCE = "default";

  private final ConfigurationManager configuration;

  private final PrivilegeInheritanceManager privInheritance;

  private final EventBus eventBus;

  @Inject
  public AuthorizationManagerImpl(ConfigurationManager configuration,
                                  PrivilegeInheritanceManager privInheritance,
                                  EventBus eventBus)
  {
    this.configuration = configuration;
    this.privInheritance = privInheritance;
    this.eventBus = eventBus;
  }

  public String getSource() {
    return SOURCE;
  }

  protected Role toRole(CRole secRole) {
    Role role = new Role();

    role.setRoleId(secRole.getId());
    role.setVersion(secRole.getVersion());
    role.setName(secRole.getName());
    role.setSource(SOURCE);
    role.setDescription(secRole.getDescription());
    role.setReadOnly(secRole.isReadOnly());
    role.setPrivileges(Sets.newHashSet(secRole.getPrivileges()));
    role.setRoles(Sets.newHashSet(secRole.getRoles()));

    return role;
  }

  protected CRole toRole(Role role) {
    CRole secRole = new CRole();

    secRole.setId(role.getRoleId());
    secRole.setVersion(role.getVersion());
    secRole.setName(role.getName());
    secRole.setDescription(role.getDescription());
    secRole.setReadOnly(role.isReadOnly());
    // null check
    if (role.getPrivileges() != null) {
      secRole.setPrivileges(Sets.newHashSet(role.getPrivileges()));
    }
    else {
      secRole.setPrivileges(Sets.<String>newHashSet());
    }

    // null check
    if (role.getRoles() != null) {
      secRole.setRoles(Sets.newHashSet(role.getRoles()));
    }
    else {
      secRole.setRoles(Sets.<String>newHashSet());
    }

    return secRole;
  }

  protected CPrivilege toPrivilege(Privilege privilege) {
    CPrivilege secPriv = new CPrivilege();
    secPriv.setId(privilege.getId());
    secPriv.setVersion(privilege.getVersion());
    secPriv.setName(privilege.getName());
    secPriv.setDescription(privilege.getDescription());
    secPriv.setReadOnly(privilege.isReadOnly());
    secPriv.setType(privilege.getType());
    if (privilege.getProperties() != null) {
      secPriv.setProperties(Maps.newHashMap(privilege.getProperties()));
    }

    return secPriv;
  }

  protected Privilege toPrivilege(CPrivilege secPriv) {
    Privilege privilege = new Privilege();
    privilege.setId(secPriv.getId());
    privilege.setVersion(secPriv.getVersion());
    privilege.setName(secPriv.getName());
    privilege.setDescription(secPriv.getDescription());
    privilege.setReadOnly(secPriv.isReadOnly());
    privilege.setType(secPriv.getType());
    privilege.setProperties(Maps.newHashMap(secPriv.getProperties()));

    return privilege;
  }

  // //
  // ROLE CRUDS
  // //

  public Set<Role> listRoles() {
    Set<Role> roles = new HashSet<Role>();
    List<CRole> secRoles = this.configuration.listRoles();

    for (CRole CRole : secRoles) {
      roles.add(this.toRole(CRole));
    }

    return roles;
  }

  public Role getRole(String roleId)
      throws NoSuchRoleException
  {
    return this.toRole(this.configuration.readRole(roleId));
  }

  public Role addRole(Role role)
      throws InvalidConfigurationException
  {
    // the roleId of the secRole might change, so we need to keep the reference
    final CRole secRole = this.toRole(role);

    configuration.createRole(secRole);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();

    return this.toRole(secRole);
  }

  public Role updateRole(Role role)
      throws NoSuchRoleException, InvalidConfigurationException
  {
    final CRole secRole = this.toRole(role);

    configuration.updateRole(secRole);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();

    return this.toRole(secRole);
  }

  public void deleteRole(final String roleId)
      throws NoSuchRoleException
  {
    configuration.deleteRole(roleId);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();
  }

  // //
  // PRIVILEGE CRUDS
  // //

  public Set<Privilege> listPrivileges() {
    Set<Privilege> privileges = new HashSet<Privilege>();
    List<CPrivilege> secPrivs = this.configuration.listPrivileges();

    for (CPrivilege CPrivilege : secPrivs) {
      privileges.add(this.toPrivilege(CPrivilege));
    }

    return privileges;
  }

  public Privilege getPrivilege(String privilegeId)
      throws NoSuchPrivilegeException
  {
    return this.toPrivilege(this.configuration.readPrivilege(privilegeId));
  }

  public Privilege addPrivilege(Privilege privilege)
      throws InvalidConfigurationException
  {
    final CPrivilege secPriv = this.toPrivilege(privilege);
    // create implies read, so we need to add logic for that
    addInheritedPrivileges(secPriv);

    configuration.createPrivilege(secPriv);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();

    return this.toPrivilege(secPriv);
  }

  public Privilege updatePrivilege(Privilege privilege)
      throws NoSuchPrivilegeException, InvalidConfigurationException
  {
    final CPrivilege secPriv = this.toPrivilege(privilege);

    configuration.updatePrivilege(secPriv);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();

    return this.toPrivilege(secPriv);
  }

  public void deletePrivilege(final String privilegeId)
      throws NoSuchPrivilegeException
  {
    configuration.deletePrivilege(privilegeId);

    // notify any listeners that the config changed
    this.fireAuthorizationChangedEvent();
  }

  public boolean supportsWrite() {
    return true;
  }

  private void addInheritedPrivileges(CPrivilege privilege) {
    String methodProperty = privilege.getProperty(ApplicationPrivilegeMethodPropertyDescriptor.ID);

    if (methodProperty != null) {
      List<String> inheritedMethods = privInheritance.getInheritedMethods(methodProperty);

      StringBuffer buf = new StringBuffer();

      for (String method : inheritedMethods) {
        buf.append(method);
        buf.append(",");
      }

      if (buf.length() > 0) {
        buf.setLength(buf.length() - 1);

        privilege.setProperty(ApplicationPrivilegeMethodPropertyDescriptor.ID, buf.toString());
      }
    }
  }

  private void fireAuthorizationChangedEvent() {
    this.eventBus.post(new AuthorizationConfigurationChanged());
  }

}
