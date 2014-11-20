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
package org.sonatype.nexus.security.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link SecurityModelConfigurationSource} implementation using Orient db as store.
 *
 * TODO remove EventSubscriber and replace with component lifecycle (NEXUS-7303)
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultSecurityModelConfigurationSource
    extends LifecycleSupport
    implements SecurityModelConfigurationSource, EventSubscriber
{

  /**
   * Security database.
   */
  private final Provider<DatabaseInstance> databaseInstance;

  /**
   * The defaults configuration source.
   */
  private final SecurityModelConfigurationSource securityDefaults;

  private final CUserEntityAdapter userEntityAdapter;

  private final CRoleEntityAdapter roleEntityAdapter;

  private final CPrivilegeEntityAdapter privilegeEntityAdapter;

  private final CUserRoleMappingEntityAdapter userRoleMappingEntityAdapter;

  /**
   * The configuration.
   */
  private SecurityModelConfiguration configuration;

  @Inject
  public DefaultSecurityModelConfigurationSource(final @Named("security") Provider<DatabaseInstance> databaseInstance,
                                                 final @Named("static") SecurityModelConfigurationSource defaults,
                                                 final CUserEntityAdapter userEntityAdapter,
                                                 final CRoleEntityAdapter roleEntityAdapter,
                                                 final CPrivilegeEntityAdapter privilegeEntityAdapter,
                                                 final CUserRoleMappingEntityAdapter userRoleMappingEntityAdapter)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.securityDefaults = checkNotNull(defaults);
    this.userEntityAdapter = checkNotNull(userEntityAdapter);
    this.roleEntityAdapter = checkNotNull(roleEntityAdapter);
    this.privilegeEntityAdapter = checkNotNull(privilegeEntityAdapter);
    this.userRoleMappingEntityAdapter = checkNotNull(userRoleMappingEntityAdapter);
  }

  @Override
  protected void doStart() {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      userEntityAdapter.register(db);
      roleEntityAdapter.register(db);
      privilegeEntityAdapter.register(db);
      userRoleMappingEntityAdapter.register(db);
    }
  }

  @Override
  public SecurityModelConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public SecurityModelConfiguration loadConfiguration() {
    try (ODatabaseDocumentTx db = openDb()) {
      List<CUser> users = Lists.newArrayList(userEntityAdapter.getAll(db));
      List<CRole> roles = Lists.newArrayList(roleEntityAdapter.get(db));
      List<CPrivilege> privileges = Lists.newArrayList(privilegeEntityAdapter.get(db));
      List<CUserRoleMapping> userRoleMappings = Lists.newArrayList(userRoleMappingEntityAdapter.get(db));
      if (users.isEmpty() && roles.isEmpty() && privileges.isEmpty() && userRoleMappings.isEmpty()) {
        saveConfiguration(securityDefaults.loadConfiguration());
        users = Lists.newArrayList(userEntityAdapter.getAll(db));
        roles = Lists.newArrayList(roleEntityAdapter.get(db));
        privileges = Lists.newArrayList(privilegeEntityAdapter.get(db));
        userRoleMappings = Lists.newArrayList(userRoleMappingEntityAdapter.get(db));
      }
      Configuration config = new Configuration();
      config.setUsers(users);
      config.setRoles(roles);
      config.setPrivileges(privileges);
      config.setUserRoleMappings(userRoleMappings);
      configuration = new OrientSecurityModelConfiguration(config);
      return getConfiguration();
    }
  }

  private void saveConfiguration(final SecurityModelConfiguration configuration) {
    checkNotNull(configuration, "Missing configuration");
    try (ODatabaseDocumentTx db = openDb()) {
      saveUsers(db, configuration.getUsers());
      saveRoles(db, configuration.getRoles());
      savePrivileges(db, configuration.getPrivileges());
      saveUserRoleMappings(db, configuration.getUserRoleMappings());
    }
  }

  /**
   * Replace users.
   */
  private void saveUsers(final ODatabaseDocumentTx db, final List<CUser> users) {
    userEntityAdapter.deleteAll(db);
    if (users != null) {
      for (CUser user : users) {
        userEntityAdapter.create(db, user);
      }
    }
  }

  /**
   * Replace roles.
   */
  private void saveRoles(final ODatabaseDocumentTx db, final List<CRole> roles) {
    roleEntityAdapter.deleteAll(db);
    if (roles != null) {
      for (CRole role : roles) {
        roleEntityAdapter.create(db, role);
      }
    }
  }

  /**
   * Replace privileges.
   */
  private void savePrivileges(final ODatabaseDocumentTx db, final List<CPrivilege> privileges) {
    privilegeEntityAdapter.deleteAll(db);
    if (privileges != null) {
      for (CPrivilege privilege : privileges) {
        privilegeEntityAdapter.create(db, privilege);
      }
    }
  }

  /**
   * Replace user role mappings.
   */
  private void saveUserRoleMappings(final ODatabaseDocumentTx db, final List<CUserRoleMapping> userRoleMappings) {
    userRoleMappingEntityAdapter.deleteAll(db);
    if (userRoleMappings != null) {
      for (CUserRoleMapping userRoleMapping : userRoleMappings) {
        userRoleMappingEntityAdapter.create(db, userRoleMapping);
      }
    }
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  /**
   * Start itself on nexus start.
   * TODO remove this and replace with component lifecycle (NEXUS-7303)
   */
  @Subscribe
  public void on(final NexusInitializedEvent event) throws Exception {
    start();
  }

  /**
   * Stop itself on nexus shutdown.
   * TODO remove this and replace with component lifecycle (NEXUS-7303)
   */
  @Subscribe
  public void on(final NexusStoppingEvent event) throws Exception {
    stop();
  }

  private class OrientSecurityModelConfiguration
      implements SecurityModelConfiguration
  {

    private final SecurityModelConfiguration delegate;

    public OrientSecurityModelConfiguration(final SecurityModelConfiguration delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    public List<CUser> getUsers() {
      return delegate.getUsers();
    }

    @Override
    public CUser getUser(final String id) {
      return delegate.getUser(id);
    }

    @Override
    public void addUser(final CUser user, final Set<String> roles) {
      try (ODatabaseDocumentTx db = openDb()) {
        userEntityAdapter.create(db, user);

        CUserRoleMapping mapping = new CUserRoleMapping();
        mapping.setUserId(user.getId());
        mapping.setSource(SecurityXmlUserManager.SOURCE);
        mapping.setRoles(new ArrayList<>(roles));
        userRoleMappingEntityAdapter.create(db, mapping);

        delegate.addUser(user, roles);
      }
    }

    @Override
    public void setUsers(final Collection<CUser> users) {
      delegate.setUsers(users);
    }

    @Override
    public void updateUser(final CUser user, final Set<String> roles) {
      checkNotNull(user, "user");
      checkNotNull(user.getId(), "user id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userEntityAdapter.get(db, user.getId());
        checkState(document != null, user.getId() + " not found");
        userEntityAdapter.write(document, user);

        CUserRoleMapping mapping = new CUserRoleMapping();
        mapping.setUserId(user.getId());
        mapping.setSource(SecurityXmlUserManager.SOURCE);
        mapping.setRoles(new ArrayList<>(roles));
        document = userRoleMappingEntityAdapter.get(db, mapping.getUserId(), mapping.getSource());
        checkState(document != null, mapping.getUserId() + " / " + mapping.getSource() + " not found");
        userRoleMappingEntityAdapter.write(document, mapping);

        delegate.updateUser(user, roles);
      }
    }

    @Override
    public boolean removeUser(final String id) {
      try (ODatabaseDocumentTx db = openDb()) {
        userEntityAdapter.delete(db, id);
        userRoleMappingEntityAdapter.delete(db, id, SecurityXmlUserManager.SOURCE);
        return delegate.removeUser(id);
      }
    }

    @Override
    public List<CUserRoleMapping> getUserRoleMappings() {
      return delegate.getUserRoleMappings();
    }

    @Override
    public CUserRoleMapping getUserRoleMapping(final String userId, final String source) {
      return delegate.getUserRoleMapping(userId, source);
    }

    @Override
    public void addUserRoleMapping(final CUserRoleMapping mapping) {
      try (ODatabaseDocumentTx db = openDb()) {
        userRoleMappingEntityAdapter.create(db, mapping);
        delegate.addUserRoleMapping(mapping);
      }
    }

    @Override
    public void setUserRoleMappings(final Collection<CUserRoleMapping> mappings) {
      delegate.setUserRoleMappings(mappings);
    }

    @Override
    public void updateUserRoleMapping(final CUserRoleMapping mapping) {
      checkNotNull(mapping, "mapping");
      checkNotNull(mapping.getUserId(), "user id");
      checkNotNull(mapping.getSource(), "source");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userRoleMappingEntityAdapter.get(db, mapping.getUserId(), mapping.getSource());
        checkState(document != null, mapping.getUserId() + " / " + mapping.getSource() + " not found");
        userRoleMappingEntityAdapter.write(document, mapping);
        delegate.updateUserRoleMapping(mapping);
      }
    }

    @Override
    public boolean removeUserRoleMapping(final String userId, final String source) {
      try (ODatabaseDocumentTx db = openDb()) {
        userRoleMappingEntityAdapter.delete(db, userId, source);
        return delegate.removeUserRoleMapping(userId, source);
      }
    }

    @Override
    public List<CPrivilege> getPrivileges() {
      return delegate.getPrivileges();
    }

    @Override
    public CPrivilege getPrivilege(final String id) {
      return delegate.getPrivilege(id);
    }

    @Override
    public void addPrivilege(final CPrivilege privilege) {
      try (ODatabaseDocumentTx db = openDb()) {
        privilegeEntityAdapter.create(db, privilege);
        delegate.addPrivilege(privilege);
      }
    }

    @Override
    public void setPrivileges(final Collection<CPrivilege> privileges) {
      delegate.setPrivileges(privileges);
    }

    @Override
    public void updatePrivilege(final CPrivilege privilege) {
      checkNotNull(privilege, "privilege");
      checkNotNull(privilege.getId(), "privilege id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = privilegeEntityAdapter.get(db, privilege.getId());
        checkState(document != null, privilege.getId() + " not found");
        privilegeEntityAdapter.write(document, privilege);
        delegate.updatePrivilege(privilege);
      }
    }

    @Override
    public boolean removePrivilege(final String id) {
      try (ODatabaseDocumentTx db = openDb()) {
        privilegeEntityAdapter.delete(db, id);
        return delegate.removePrivilege(id);
      }
    }

    @Override
    public List<CRole> getRoles() {
      return delegate.getRoles();
    }

    @Override
    public CRole getRole(final String id) {
      return delegate.getRole(id);
    }

    @Override
    public void addRole(final CRole role) {
      try (ODatabaseDocumentTx db = openDb()) {
        roleEntityAdapter.create(db, role);
        delegate.addRole(role);
      }
    }

    @Override
    public void updateRole(final CRole role) {
      checkNotNull(role, "role");
      checkNotNull(role.getId(), "role id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = roleEntityAdapter.get(db, role.getId());
        checkState(document != null, role.getId() + " not found");
        roleEntityAdapter.write(document, role);
        delegate.updateRole(role);
      }
    }

    @Override
    public void setRoles(final Collection<CRole> roles) {
      delegate.setRoles(roles);
    }

    @Override
    public boolean removeRole(final String id) {
      try (ODatabaseDocumentTx db = openDb()) {
        roleEntityAdapter.delete(db, id);
        return delegate.removeRole(id);
      }
    }

  }

}
