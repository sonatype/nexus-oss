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

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;
import org.sonatype.security.usermanagement.UserManagerImpl;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.valueOf;

/**
 * Default {@link SecurityModelConfigurationSource} implementation using Orient db as store.
 *
 * TODO remove EventSubscriber and replace with component lifecycle (NEXUS-7303)
 *
 * @since 3.0
 */
@Named
@Singleton
public class OrientSecurityModelConfigurationSource
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
  public OrientSecurityModelConfigurationSource(final @Named("security") Provider<DatabaseInstance> databaseInstance,
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
  public SecurityModelConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public SecurityModelConfiguration loadConfiguration() {
    configuration = new OrientSecurityModelConfiguration();
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

  private class OrientSecurityModelConfiguration
      implements SecurityModelConfiguration
  {

    @Override
    public List<CUser> getUsers() {
      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(userEntityAdapter.get(db));
      }
    }

    @Override
    public CUser getUser(final String id) {
      checkNotNull(id, "user id");
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
      try (ODatabaseDocumentTx db = openDb()) {
        userEntityAdapter.create(db, user);

        CUserRoleMapping mapping = new CUserRoleMapping();
        mapping.setUserId(user.getId());
        mapping.setSource(UserManagerImpl.SOURCE);
        mapping.setRoles(roles);
        userRoleMappingEntityAdapter.create(db, mapping);
      }
    }

    @Override
    public void updateUser(final CUser user, final Set<String> roles) throws UserNotFoundException {
      checkNotNull(user, "user");
      checkNotNull(user.getId(), "user id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userEntityAdapter.get(db, user.getId());
        if (document == null) {
          throw new UserNotFoundException("User " + user.getId() + " not found");
        }
        if (user.getVersion() != null && !Objects.equals(user.getVersion(), valueOf(document.getVersion()))) {
          throw new ConcurrentModificationException(
              "User " + user.getId() + " updated in the mean time"
          );
        }
        userEntityAdapter.write(document, user);

        CUserRoleMapping mapping = new CUserRoleMapping();
        mapping.setUserId(user.getId());
        mapping.setSource(UserManagerImpl.SOURCE);
        mapping.setRoles(roles);
        document = userRoleMappingEntityAdapter.get(db, mapping.getUserId(), mapping.getSource());
        if (document != null) {
          userRoleMappingEntityAdapter.write(document, mapping);
        }
        else {
          userRoleMappingEntityAdapter.create(db, mapping);
        }
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "User " + user.getId() + " updated in the mean time"
        );
      }
    }

    @Override
    public boolean removeUser(final String id) {
      checkNotNull(id, "user id");
      try (ODatabaseDocumentTx db = openDb()) {
        if (userEntityAdapter.delete(db, id)) {
          userRoleMappingEntityAdapter.delete(db, id, UserManagerImpl.SOURCE);
          return true;
        }
        return false;
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "User " + id + " updated in the mean time"
        );
      }
    }

    @Override
    public List<CUserRoleMapping> getUserRoleMappings() {
      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(userRoleMappingEntityAdapter.get(db));
      }
    }

    @Override
    public CUserRoleMapping getUserRoleMapping(final String userId, final String source) {
      checkNotNull(userId, "user id");
      checkNotNull(source, "source");
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
      try (ODatabaseDocumentTx db = openDb()) {
        userRoleMappingEntityAdapter.create(db, mapping);
      }
    }

    @Override
    public void updateUserRoleMapping(final CUserRoleMapping mapping) throws NoSuchRoleMappingException {
      checkNotNull(mapping, "mapping");
      checkNotNull(mapping.getUserId(), "user id");
      checkNotNull(mapping.getSource(), "source");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = userRoleMappingEntityAdapter.get(db, mapping.getUserId(), mapping.getSource());
        if (document == null) {
          throw new NoSuchRoleMappingException("User " + mapping.getUserId() + " role mappings not found");
        }
        if (mapping.getVersion() != null && !Objects.equals(mapping.getVersion(), valueOf(document.getVersion()))) {
          throw new ConcurrentModificationException(
              "User " + mapping.getUserId() + " role mappings updated in the mean time"
          );
        }
        userRoleMappingEntityAdapter.write(document, mapping);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "User " + mapping.getUserId() + " role mappings updated in the mean time"
        );
      }
    }

    @Override
    public boolean removeUserRoleMapping(final String userId, final String source) {
      checkNotNull(userId, "user id");
      checkNotNull(source, "source");
      try (ODatabaseDocumentTx db = openDb()) {
        return userRoleMappingEntityAdapter.delete(db, userId, source);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "User " + userId + " role mappings updated in the mean time"
        );
      }
    }

    @Override
    public List<CPrivilege> getPrivileges() {
      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(privilegeEntityAdapter.get(db));
      }
    }

    @Override
    public CPrivilege getPrivilege(final String id) {
      checkNotNull(id, "privilege id");
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
      try (ODatabaseDocumentTx db = openDb()) {
        privilegeEntityAdapter.create(db, privilege);
      }
    }

    @Override
    public void updatePrivilege(final CPrivilege privilege) throws NoSuchPrivilegeException {
      checkNotNull(privilege, "privilege");
      checkNotNull(privilege.getId(), "privilege id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = privilegeEntityAdapter.get(db, privilege.getId());
        if (document == null) {
          throw new NoSuchPrivilegeException("Privilege " + privilege.getId() + " not found");
        }
        if (privilege.getVersion() != null && !Objects.equals(privilege.getVersion(), valueOf(document.getVersion()))) {
          throw new ConcurrentModificationException(
              "Privilege " + privilege.getId() + " updated in the mean time"
          );
        }
        privilegeEntityAdapter.write(document, privilege);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "Privilege " + privilege.getId() + " updated in the mean time"
        );
      }
    }

    @Override
    public boolean removePrivilege(final String id) {
      checkNotNull(id, "privilege id");
      try (ODatabaseDocumentTx db = openDb()) {
        return privilegeEntityAdapter.delete(db, id);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "Privilege " + id + " updated in the mean time"
        );
      }
    }

    @Override
    public List<CRole> getRoles() {
      try (ODatabaseDocumentTx db = openDb()) {
        return Lists.newArrayList(roleEntityAdapter.get(db));
      }
    }

    @Override
    public CRole getRole(final String id) {
      checkNotNull(id, "role id");
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
      try (ODatabaseDocumentTx db = openDb()) {
        roleEntityAdapter.create(db, role);
      }
    }

    @Override
    public void updateRole(final CRole role) throws NoSuchRoleException {
      checkNotNull(role, "role");
      checkNotNull(role.getId(), "role id");
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument document = roleEntityAdapter.get(db, role.getId());
        if (document == null) {
          throw new NoSuchRoleException("Role " + role.getId() + " not found");
        }
        if (role.getVersion() != null && !Objects.equals(role.getVersion(), valueOf(document.getVersion()))) {
          throw new ConcurrentModificationException(
              "Role " + role.getId() + " updated in the mean time"
          );
        }
        roleEntityAdapter.write(document, role);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "Role " + role.getId() + " updated in the mean time"
        );
      }
    }

    @Override
    public boolean removeRole(final String id) {
      checkNotNull(id, "role id");
      try (ODatabaseDocumentTx db = openDb()) {
        return roleEntityAdapter.delete(db, id);
      }
      catch (OConcurrentModificationException e) {
        throw new ConcurrentModificationException(
            "Role " + id + " updated in the mean time"
        );
      }
    }

  }

}
