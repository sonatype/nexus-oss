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
package org.sonatype.nexus.ldap.internal.realms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.common.app.NexusStoppedEvent;
import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.ldap.internal.connector.DefaultLdapConnector;
import org.sonatype.nexus.ldap.internal.connector.FailoverLdapConnector;
import org.sonatype.nexus.ldap.internal.connector.LdapConnector;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapAuthConfiguration;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapGroupDAO;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapUser;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapUserDAO;
import org.sonatype.nexus.ldap.internal.connector.dao.NoLdapUserRolesFoundException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapGroupException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapUserException;
import org.sonatype.nexus.ldap.internal.events.LdapClearCacheEvent;
import org.sonatype.nexus.ldap.internal.persist.LdapConfigurationManager;
import org.sonatype.nexus.ldap.internal.persist.LdapServerNotFoundException;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.authc.AuthenticationException;
import static com.google.common.base.Preconditions.checkNotNull;

// TODO: this really should be threaded so we make multiple parallel requests

@Named
@Singleton
public class EnterpriseLdapManager
    extends ComponentSupport
    implements LdapManager
{
  private final LdapAuthenticator ldapAuthenticator;

  private final LdapUserDAO ldapUserManager;

  private final LdapGroupDAO ldapGroupManager;

  private final EventBus eventBus;

  private final LdapConfigurationManager ldapConfigurationManager;

  private final TrustStore trustStore;

  private List<LdapConnector> ldapConnectors = new ArrayList<LdapConnector>();

  @Inject
  public EnterpriseLdapManager(final LdapAuthenticator ldapAuthenticator,
                               final LdapUserDAO ldapUserManager,
                               final LdapGroupDAO ldapGroupManager,
                               final EventBus eventBus,
                               final LdapConfigurationManager ldapConfigurationManager,
                               final TrustStore trustStore)
  {
    this.ldapAuthenticator = checkNotNull(ldapAuthenticator);
    this.ldapUserManager = checkNotNull(ldapUserManager);
    this.ldapGroupManager = checkNotNull(ldapGroupManager);
    this.eventBus = checkNotNull(eventBus);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.trustStore = checkNotNull(trustStore);

    this.eventBus.register(this);
  }

  public LdapUser authenticateUserTest(String userId, String password, LdapConfiguration ldapServer)
      throws AuthenticationException, LdapServerNotFoundException,
             NoSuchLdapUserException
  {
    try {
      // first build the connector
      LdapConnector testLdapConnector =
          new DefaultLdapConnector(ldapServer.getId(), this.ldapUserManager, this.ldapGroupManager,
              LdapConnectionUtils.getLdapContextFactory(ldapServer, trustStore),
              this.getLdapAuthConfiguration(ldapServer));

      LdapUser user = testLdapConnector.getUser(userId);
      this.authenticateUser(user, password, testLdapConnector, ldapServer);
      return user;

    }
    catch (LdapDAOException e) {
      throw new AuthenticationException("Server: " + ldapServer.getName() + ", could not be accessed: "
          + e.getMessage(), e);
    }
  }

  @Override
  public LdapUser authenticateUser(String userId, String password)
      throws AuthenticationException
  {
    try {
      for (LdapConnector connector : this.getLdapConnectors()) {
        try {
          LdapUser ldapUser = connector.getUser(userId);

          // do the authentication
          authenticateUser(
              ldapUser,
              password,
              connector,
              ldapConfigurationManager.getLdapServerConfiguration(connector.getIdentifier()));

          return ldapUser;
        }
        catch (Exception e) {
          if (this.log.isDebugEnabled()) {
            this.log.debug("Failed to find user: " + userId, e);
          }
        }
      }
    }
    catch (LdapDAOException e) {
      throw new AuthenticationException("User: " + userId + " could not be authenticated.", e);
    }

    throw new AuthenticationException("User: " + userId + " could not be authenticated.");
  }

  @Override
  public SortedSet<String> getAllGroups()
      throws LdapDAOException
  {
    SortedSet<String> groupIds = new TreeSet<String>();

    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        groupIds.addAll(connector.getAllGroups());
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to get groups from ldap server.", e);
      }
    }

    return groupIds;
  }

  @Override
  public SortedSet<LdapUser> getAllUsers()
      throws LdapDAOException
  {
    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        users.addAll(connector.getAllUsers());
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to get users from ldap server.", e);
      }
    }

    return users;
  }

  @Override
  public String getGroupName(String groupId)
      throws LdapDAOException, NoSuchLdapGroupException
  {
    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        return connector.getGroupName(groupId);
      }
      catch (NoSuchLdapGroupException e) {
        this.log.debug("Failed to find group: " + groupId, e);
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to find group: " + groupId, e);
      }
    }
    throw new NoSuchLdapGroupException(groupId, groupId);
  }

  @Override
  public LdapUser getUser(String userId)
      throws NoSuchLdapUserException, LdapDAOException
  {
    LdapDAOException serverError = null;

    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        return connector.getUser(userId);
      }
      catch (NoSuchLdapUserException e) {
        this.log.debug("Failed to find user: " + userId, e);
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to find user: " + userId, e);
        serverError = e;
      }
    }

    if (serverError == null) {
      // NXCM-4165: all configured servers are reachable, hard evidence the user does not exist.
      throw new NoSuchLdapUserException(userId);
    }
    else {
      // NXCM-4165: report 'upstream' when a server was down, we need to trigger UserNotFoundTransientException
      //            because we cannot be sure it did not have the user
      throw new LdapDAOException("Failed to find user: " + userId + ", we could not connect to all servers.",
          serverError);
    }
  }

  @Override
  public Set<String> getUserRoles(String userId)
      throws LdapDAOException, NoLdapUserRolesFoundException
  {
    try {
      LdapUser ldapUser = this.getUser(userId);
      return ldapUser.getMembership();
    }
    catch (LdapDAOException e) {
      this.log.debug("Failed to find user: " + userId, e);
    }
    catch (NoSuchLdapUserException e) {
      this.log.debug("Failed to find user: " + userId, e);
    }
    throw new NoLdapUserRolesFoundException(userId);
  }

  @Override
  public SortedSet<LdapUser> getUsers(int userCount)
      throws LdapDAOException
  {
    if (userCount < 0) {
      return this.getAllUsers();
    }

    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        if (userCount - users.size() <= 0) {
          // we have all the users we where looking for
          break;
        }

        users.addAll(connector.getUsers(userCount - users.size()));
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to get users from ldap server.", e);
      }
    }

    return users;
  }

  @Override
  public SortedSet<LdapUser> searchUsers(String username, Set<String> roleIds)
      throws LdapDAOException
  {
    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    for (LdapConnector connector : this.getLdapConnectors()) {
      try {
        users.addAll(connector.searchUsers(username, roleIds));
      }
      catch (LdapDAOException e) {
        this.log.debug("Failed to get users from ldap server.", e);
      }
    }

    return users;
  }

  // package protected, so we can, inject mock objects for testing
  @VisibleForTesting
  synchronized List<LdapConnector> getLdapConnectors()
      throws LdapDAOException
  {
    if (this.ldapConnectors.isEmpty()) {
      for (LdapConfiguration ldapServer : ldapConfigurationManager.listLdapServerConfigurations()) {
        // first get the connector for the server
        LdapConnector originalLdapConnector =
            new DefaultLdapConnector(ldapServer.getId(), ldapUserManager, ldapGroupManager,
                LdapConnectionUtils.getLdapContextFactory(ldapServer, trustStore),
                getLdapAuthConfiguration(ldapServer));

        ldapConnectors.add(new FailoverLdapConnector(
            originalLdapConnector,
            null,
            ldapServer.getConnection().getConnectionRetryDelay(),
            ldapServer.getConnection().getMaxIncidentsCount()));
      }
    }
    return this.ldapConnectors;
  }

  private LdapAuthConfiguration getLdapAuthConfiguration(LdapConfiguration ldapServer) {
    return LdapConnectionUtils.getLdapAuthConfiguration(ldapServer);
  }

  private void authenticateUser(LdapUser ldapUser, String password, LdapConnector ldapConnector,
                                LdapConfiguration ldapServer)
      throws LdapServerNotFoundException, AuthenticationException, LdapDAOException
  {
    if (Strings2.isEmpty(ldapServer.getMapping().getUserPasswordAttribute())) {
      // auth with bind
      log.debug("Checking auth with bind for ldap user: {}", ldapUser.getUsername());
      ldapAuthenticator.authenticateUserWithBind(ldapUser, password, ldapConnector.getLdapContextFactory(),
          ldapServer.getConnection().getAuthScheme());
    }
    else {
      // auth by checking password,
      log.debug("Checking auth with attribute for ldap user: {}", ldapUser.getUsername());
      ldapAuthenticator.authenticateUserWithPassword(ldapUser, password);
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onEvent(final LdapClearCacheEvent evt) {
    // clear the connectors
    ldapConnectors.clear();
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) {
    eventBus.unregister(this);
  }
}
