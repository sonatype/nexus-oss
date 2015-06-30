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
package org.sonatype.nexus.internal.security.model;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.NexusInitializedEvent;
import org.sonatype.nexus.common.app.NexusStoppingEvent;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.CRole;
import org.sonatype.nexus.security.config.CUser;
import org.sonatype.nexus.security.config.CUserRoleMapping;
import org.sonatype.nexus.security.config.SecurityConfiguration;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.nexus.security.privilege.NoSuchPrivilegeException;
import org.sonatype.nexus.security.role.NoSuchRoleException;
import org.sonatype.nexus.security.user.NoSuchRoleMappingException;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.valueOf;

/**
 * Default {@link SecurityConfigurationSource} implementation using Orient db as store.
 *
 * TODO remove EventSubscriber and replace with component lifecycle (NEXUS-7303)
 *
 * @since 3.0
 */
@Named
@Singleton
public class OrientSecurityConfigurationSource
    extends LifecycleSupport
    implements SecurityConfigurationSource, EventSubscriber
{
  /**
   * Security database.
   */
  private final Provider<DatabaseInstance> databaseInstance;

  /**
   * The defaults configuration source.
   */
  private final SecurityConfigurationSource securityDefaults;

  private final CUserEntityAdapter userEntityAdapter;

  private final CRoleEntityAdapter roleEntityAdapter;

  private final CPrivilegeEntityAdapter privilegeEntityAdapter;

  private final CUserRoleMappingEntityAdapter userRoleMappingEntityAdapter;

  /**
   * The configuration.
   */
  private SecurityConfiguration configuration;

  @Inject
  public OrientSecurityConfigurationSource(final @Named("security") Provider<DatabaseInstance> databaseInstance,
                                           final @Named("static") SecurityConfigurationSource defaults,
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
      userEntityAdapter.register(db, new Runnable()
      {
        @Override
        public void run() {
          List<CUser> users = securityDefaults.getConfiguration().getUsers();
          if (users != null && !users.isEmpty()) {
            log.info("Initializing default users");
            for (CUser user : users) {
              userEntityAdapter.create(db, user);
            }
          }
        }
      });
      roleEntityAdapter.register(db, new Runnable()
      {
        @Override
        public void run() {
          List<CRole> roles = securityDefaults.getConfiguration().getRoles();
          if (roles != null && !roles.isEmpty()) {
            log.info("Initializing default roles");
            for (CRole role : roles) {
              roleEntityAdapter.create(db, role);
            }
          }
        }
      });
      privilegeEntityAdapter.register(db, new Runnable()
      {
        @Override
        public void run() {
          List<CPrivilege> privileges = securityDefaults.getConfiguration().getPrivileges();
          if (privileges != null && !privileges.isEmpty()) {
            log.info("Initializing default privileges");
            for (CPrivilege privilege : privileges) {
              privilegeEntityAdapter.create(db, privilege);
            }
          }
        }
      });
      userRoleMappingEntityAdapter.register(db, new Runnable()
      {
        @Override
        public void run() {
          List<CUserRoleMapping> mappings = securityDefaults.getConfiguration().getUserRoleMappings();
          if (mappings != null && !mappings.isEmpty()) {
            log.info("Initializing default user/role mappings");
            for (CUserRoleMapping mapping : mappings) {
              userRoleMappingEntityAdapter.create(db, mapping);
            }
          }
        }
      });
    }
  }

  @Override
  public SecurityConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public SecurityConfiguration loadConfiguration() {
    configuration = new CachingOrientSecurityConfiguration();
    return getConfiguration();
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

  private class OrientSecurityConfiguration
      implements SecurityConfiguration
  {
    private ConcurrentModificationException concurrentlyModified(final String type, final String value) {
      throw new ConcurrentModificationException(type + " '" + value + "' updated in the meantime");
    }

    @Override
    public List<CUser> getUsers() {
      log.trace("Retrieving all users");

      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(userEntityAdapter.get(db));
      }
    }

    @Override
    public CUser getUser(final String id) {
      checkNotNull(id, "user id");
      log.trace("Retrieving user {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userEntityAdapter.get(db, id);
        if (document != null) {
          return userEntityAdapter.read(document);
        }
        return null;
      }
    }

    @Override
    public void addUser(final CUser user, final Set<String> roles) {
      checkNotNull(user, "user");
      checkNotNull(user.getId(), "user id");
      log.trace("Adding user {}", user.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        userEntityAdapter.create(db, user);
        addUserRoleMapping(mapping(user.getId(), roles));
      }
    }

    @Override
    public void updateUser(final CUser user, final Set<String> roles) throws UserNotFoundException {
      checkNotNull(user, "user");
      checkNotNull(user.getId(), "user id");
      log.trace("Updating user {}", user.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userEntityAdapter.get(db, user.getId());
        if (document == null) {
          throw new UserNotFoundException(user.getId());
        }
        if (user.getVersion() != null && !Objects.equals(user.getVersion(), valueOf(document.getVersion()))) {
          throw concurrentlyModified("User", user.getId());
        }
        userEntityAdapter.write(document, user);

        CUserRoleMapping mapping = mapping(user.getId(), roles);
        try {
          updateUserRoleMapping(mapping);
        }
        catch (NoSuchRoleMappingException e) {
          addUserRoleMapping(mapping);
        }
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("User", user.getId());
      }
    }

    @Override
    public boolean removeUser(final String id) {
      checkNotNull(id, "user id");
      log.trace("Removing user {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        if (userEntityAdapter.delete(db, id)) {
          removeUserRoleMapping(id, UserManager.DEFAULT_SOURCE);
          return true;
        }
        return false;
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("User", id);
      }
    }

    @Override
    public List<CUserRoleMapping> getUserRoleMappings() {
      log.trace("Retrieving all user/role mappings");

      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(userRoleMappingEntityAdapter.get(db));
      }
    }

    @Override
    public CUserRoleMapping getUserRoleMapping(final String userId, final String source) {
      checkNotNull(userId, "user id");
      checkNotNull(source, "source");
      log.trace("Retrieving user/role mappings of {}/{}", userId, source);

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userRoleMappingEntityAdapter.get(db, userId, source);
        if (document != null) {
          return userRoleMappingEntityAdapter.read(document);
        }
        return null;
      }
    }

    @Override
    public void addUserRoleMapping(final CUserRoleMapping mapping) {
      checkNotNull(mapping, "mapping");
      checkNotNull(mapping.getUserId(), "user id");
      checkNotNull(mapping.getSource(), "source");
      log.trace("Adding user/role mappings for {}/{}", mapping.getUserId(), mapping.getSource());

      try (ODatabaseDocumentTx db = openDb()) {
        userRoleMappingEntityAdapter.create(db, mapping);
      }
    }

    @Override
    public void updateUserRoleMapping(final CUserRoleMapping mapping) throws NoSuchRoleMappingException {
      checkNotNull(mapping, "mapping");
      checkNotNull(mapping.getUserId(), "user id");
      checkNotNull(mapping.getSource(), "source");
      log.trace("Updating user/role mappings for {}/{}", mapping.getUserId(), mapping.getSource());

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userRoleMappingEntityAdapter.get(db, mapping.getUserId(), mapping.getSource());
        if (document == null) {
          throw new NoSuchRoleMappingException(mapping.getUserId());
        }
        if (mapping.getVersion() != null && !Objects.equals(mapping.getVersion(), valueOf(document.getVersion()))) {
          throw concurrentlyModified("User-role mapping", mapping.getUserId());
        }
        userRoleMappingEntityAdapter.write(document, mapping);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("User-role mapping", mapping.getUserId());
      }
    }

    @Override
    public boolean removeUserRoleMapping(final String userId, final String source) {
      checkNotNull(userId, "user id");
      checkNotNull(source, "source");
      log.trace("Removing user/role mappings for {}/{}", userId, source);

      try (ODatabaseDocumentTx db = openDb()) {
        return userRoleMappingEntityAdapter.delete(db, userId, source);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("User-role mapping", userId);
      }
    }

    @Override
    public List<CPrivilege> getPrivileges() {
      log.trace("Retrieving all privileges");

      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(privilegeEntityAdapter.get(db));
      }
    }

    @Override
    public CPrivilege getPrivilege(final String id) {
      checkNotNull(id, "privilege id");
      log.trace("Retrieving privilege {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = privilegeEntityAdapter.get(db, id);
        if (document != null) {
          return privilegeEntityAdapter.read(document);
        }
        return null;
      }
    }

    @Override
    public void addPrivilege(final CPrivilege privilege) {
      checkNotNull(privilege, "privilege");
      checkNotNull(privilege.getId(), "privilege id");
      log.trace("Adding privilege {}", privilege.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        privilegeEntityAdapter.create(db, privilege);
      }
    }

    @Override
    public void updatePrivilege(final CPrivilege privilege) throws NoSuchPrivilegeException {
      checkNotNull(privilege, "privilege");
      checkNotNull(privilege.getId(), "privilege id");
      log.trace("Updating privilege {}", privilege.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = privilegeEntityAdapter.get(db, privilege.getId());
        if (document == null) {
          throw new NoSuchPrivilegeException(privilege.getId());
        }
        if (privilege.getVersion() != null && !Objects.equals(privilege.getVersion(), valueOf(document.getVersion()))) {
          throw concurrentlyModified("Privilege", privilege.getId());
        }
        privilegeEntityAdapter.write(document, privilege);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("Privilege", privilege.getId());
      }
    }

    @Override
    public boolean removePrivilege(final String id) {
      checkNotNull(id, "privilege id");
      log.trace("Removing privilege {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        return privilegeEntityAdapter.delete(db, id);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("Privilege", id);
      }
    }

    @Override
    public List<CRole> getRoles() {
      log.trace("Retrieving all roles");

      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(roleEntityAdapter.get(db));
      }
    }

    @Override
    public CRole getRole(final String id) {
      checkNotNull(id, "role id");
      log.trace("Retrieving role {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = roleEntityAdapter.get(db, id);
        if (document != null) {
          return roleEntityAdapter.read(document);
        }
        return null;
      }
    }

    @Override
    public void addRole(final CRole role) {
      checkNotNull(role, "role");
      checkNotNull(role.getId(), "role id");
      log.trace("Adding role {}", role.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        roleEntityAdapter.create(db, role);
      }
    }

    @Override
    public void updateRole(final CRole role) throws NoSuchRoleException {
      checkNotNull(role, "role");
      checkNotNull(role.getId(), "role id");
      log.trace("Updating role {}", role.getId());

      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = roleEntityAdapter.get(db, role.getId());
        if (document == null) {
          throw new NoSuchRoleException(role.getId());
        }
        if (role.getVersion() != null && !Objects.equals(role.getVersion(), valueOf(document.getVersion()))) {
          throw concurrentlyModified("Role", role.getId());
        }
        roleEntityAdapter.write(document, role);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("Role", role.getId());
      }
    }

    @Override
    public boolean removeRole(final String id) {
      checkNotNull(id, "role id");
      log.trace("Removing role {}", id);

      try (ODatabaseDocumentTx db = openDb()) {
        return roleEntityAdapter.delete(db, id);
      }
      catch (OConcurrentModificationException e) {
        throw concurrentlyModified("Role", id);
      }
    }

    private CUserRoleMapping mapping(final String userId, final Set<String> roles) {
      CUserRoleMapping mapping = new CUserRoleMapping();
      mapping.setUserId(userId);
      mapping.setSource(UserManager.DEFAULT_SOURCE);
      mapping.setRoles(roles);
      return mapping;
    }
  }

  private class CachingOrientSecurityConfiguration
      extends OrientSecurityConfiguration
  {
    private final ConcurrentMap<String, CUserRoleMapping> userRoleMappings;

    private CachingOrientSecurityConfiguration() {
      userRoleMappings = Maps.newConcurrentMap();
      for (CUserRoleMapping mapping : super.getUserRoleMappings()) {
        userRoleMappings.put(userRoleMappingKey(mapping.getUserId(), mapping.getSource()), mapping);
      }
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
      super.addUserRoleMapping(mapping);
      userRoleMappings.put(userRoleMappingKey(mapping.getUserId(), mapping.getSource()), mapping);
    }

    @Override
    public void updateUserRoleMapping(final CUserRoleMapping mapping) throws NoSuchRoleMappingException {
      super.updateUserRoleMapping(mapping);
      userRoleMappings.put(userRoleMappingKey(mapping.getUserId(), mapping.getSource()), mapping);
    }

    @Override
    public boolean removeUserRoleMapping(final String userId, final String source) {
      boolean found = super.removeUserRoleMapping(userId, source);
      userRoleMappings.remove(userRoleMappingKey(userId, source));
      return found;
    }

    private String userRoleMappingKey(final String userId, final String source) {
      return userId + "|" + source;
    }
  }
}
