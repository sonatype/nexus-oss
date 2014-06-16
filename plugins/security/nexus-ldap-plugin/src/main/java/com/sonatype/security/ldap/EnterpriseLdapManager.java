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
package com.sonatype.security.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ldap.internal.ssl.SSLLdapContextFactory;
import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.connector.FailoverLdapConnector;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.LdapAuthenticator;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.connector.DefaultLdapConnector;
import org.sonatype.security.ldap.realms.connector.LdapConnector;
import org.sonatype.security.ldap.realms.persist.LdapClearCacheEvent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ldap.model.LdapTrustStoreKey.ldapTrustStoreKey;

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

  public LdapUser authenticateUserTest(String userId, String password, CLdapServerConfiguration ldapServer)
      throws AuthenticationException, InvalidConfigurationException, LdapServerNotFoundException,
             NoSuchLdapUserException
  {
    try {
      // first build the connector
      LdapConnector testLdapConnector =
          new DefaultLdapConnector(ldapServer.getId(), this.ldapUserManager, this.ldapGroupManager,
              this.getLdapContextFactory(ldapServer, false),
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

  public LdapUser authenticateUser(String userId, String password)
      throws AuthenticationException
  {
    try {
      for (LdapConnector connector : this.getLdapConnectors()) {
        try {
          LdapUser ldapUser = connector.getUser(userId);

          // do the authentication
          this.authenticateUser(
              ldapUser,
              password,
              connector,
              this.ldapConfigurationManager.getLdapServerConfiguration(connector.getIdentifier()));

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
  synchronized List<LdapConnector> getLdapConnectors()
      throws LdapDAOException
  {
    if (this.ldapConnectors.isEmpty()) {

      for (CLdapServerConfiguration ldapServer : this.ldapConfigurationManager.listLdapServerConfigurations()) {
        // first get the connector for the server
        LdapConnector originalLdapConnector =
            new DefaultLdapConnector(ldapServer.getId(), this.ldapUserManager, this.ldapGroupManager,
                this.getLdapContextFactory(ldapServer, false),
                this.getLdapAuthConfiguration(ldapServer));

        LdapConnector backupLdapConnector = null;

        // if we have a backup mirror defined we need to include that
        if (StringUtils.isNotEmpty(ldapServer.getConnectionInfo().getBackupMirrorHost())
            && ldapServer.getConnectionInfo().getBackupMirrorPort() > 0
            && StringUtils.isNotEmpty(ldapServer.getConnectionInfo().getBackupMirrorProtocol())) {

          backupLdapConnector =
              new DefaultLdapConnector(ldapServer.getId(), this.ldapUserManager, this.ldapGroupManager,
                  this.getLdapContextFactory(ldapServer, true),
                  this.getLdapAuthConfiguration(ldapServer));
        }

        this.ldapConnectors.add(new FailoverLdapConnector(
            originalLdapConnector,
            backupLdapConnector,
            ldapServer.getConnectionInfo().getConnectionRetryDelay()));

      }
    }
    return this.ldapConnectors;
  }

  private LdapContextFactory getLdapContextFactory(CLdapServerConfiguration ldapServer, boolean useBackupUrl)
      throws LdapDAOException
  {
    final DefaultLdapContextFactory ldapContextFactory = LdapConnectionUtils.getLdapContextFactory(
        ldapServer, useBackupUrl
    );
    final TrustStoreKey key = ldapTrustStoreKey(ldapServer.getId() == null ? "<unknown>" : ldapServer.getId());
    if ("ldaps".equals(ldapServer.getConnectionInfo().getProtocol())) {
      final SSLContext sslContext = trustStore.getSSLContextFor(key);
      if (sslContext != null) {
        log.debug(
            "{} is using a Nexus SSL Trust Store for accessing {}",
            key, ldapServer.getConnectionInfo().getHost()
        );
        return new SSLLdapContextFactory(sslContext, ldapContextFactory);
      }
    }
    log.debug(
        "{} is using a JVM Trust Store for accessing {}",
        key, ldapServer.getConnectionInfo().getHost()
    );
    return ldapContextFactory;
  }

  private LdapAuthConfiguration getLdapAuthConfiguration(CLdapServerConfiguration ldapServer) {
    return LdapConnectionUtils.getLdapAuthConfiguration(ldapServer);
  }

  private void authenticateUser(LdapUser ldapUser, String password, LdapConnector ldapConnector,
                                CLdapServerConfiguration ldapServer)
      throws InvalidConfigurationException, LdapServerNotFoundException, AuthenticationException, LdapDAOException
  {
    if (StringUtils.isEmpty(ldapServer.getUserAndGroupConfig().getUserPasswordAttribute())) {
      // auth with bind
      if (this.log.isDebugEnabled()) {
        this.log.debug("Checking auth with bind for ldap user: " + ldapUser.getUsername());
      }
      this.ldapAuthenticator.authenticateUserWithBind(ldapUser, password, ldapConnector.getLdapContextFactory(),
          ldapServer.getConnectionInfo().getAuthScheme());
    }
    else {
      // auth by checking password,
      if (this.log.isDebugEnabled()) {
        this.log.debug("Checking auth with attribute for ldap user: " + ldapUser.getUsername());
      }
      this.ldapAuthenticator.authenticateUserWithPassword(ldapUser, password);
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onEvent(final LdapClearCacheEvent evt) {
    // clear the connectors
    this.ldapConnectors.clear();
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) {
    this.eventBus.unregister(this);
  }
}
