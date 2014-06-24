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
package com.sonatype.security.ldap.connector;

import java.util.Set;
import java.util.SortedSet;

import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.connector.LdapConnector;

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailoverLdapConnector
    implements LdapConnector
{

  private static Logger log = LoggerFactory.getLogger(FailoverLdapConnector.class);

  private LdapConnector originalLdapManagerConnector;

  private LdapConnector backupLdapManagerConnector;

  private int retryDelay = 0;

  private long connectionFailedTime = 0;

  private int incidents = 0;

  public FailoverLdapConnector(LdapConnector originalLdapManagerConnector,
                               LdapConnector backupLdapManagerConnector, int retryDelay)
  {
    this.originalLdapManagerConnector = originalLdapManagerConnector;
    this.backupLdapManagerConnector = backupLdapManagerConnector;
    this.retryDelay = retryDelay * 1000;
  }

  @VisibleForTesting
  void connectionFailed() {
    this.connectionFailedTime = System.currentTimeMillis();
    incidents += 1;
  }

  @VisibleForTesting
  void resetFailure() {
    this.connectionFailedTime = 0;
    incidents = 0;
  }

  @VisibleForTesting
  void setConnectionFailedTime(long connectionFailedTime) {
    this.connectionFailedTime = connectionFailedTime;
  }

  @VisibleForTesting
  boolean isOriginalConnectorValid() {
    // main connector is well
    if ((connectionFailedTime == 0)) {
      return true;
    }

    // connector (maybe) recovered, reset time + incidents
    if ((this.connectionFailedTime + this.retryDelay) < System.currentTimeMillis()) {
      resetFailure();
      return true;
    }
    else if (this.incidents < 3)
    // connector is still below the failure threshold, do not blacklist but try again
    {
      return true;
    }

    return false;
  }

  public SortedSet<String> getAllGroups()
      throws LdapDAOException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getAllGroups();
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getAllGroups();
      }
    }
    else {
      return this.getBackupConnector().getAllGroups();
    }
  }

  public SortedSet<LdapUser> getAllUsers()
      throws LdapDAOException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getAllUsers();
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getAllUsers();
      }
    }
    else {
      return this.getBackupConnector().getAllUsers();
    }
  }

  public String getGroupName(String groupId)
      throws LdapDAOException,
             NoSuchLdapGroupException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getGroupName(groupId);
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getGroupName(groupId);
      }
    }
    else {
      return this.getBackupConnector().getGroupName(groupId);
    }
  }

  public LdapUser getUser(String username)
      throws NoSuchLdapUserException,
             LdapDAOException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getUser(username);
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getUser(username);
      }
    }
    else {
      return this.getBackupConnector().getUser(username);
    }
  }

  public Set<String> getUserRoles(String userId)
      throws LdapDAOException,
             NoLdapUserRolesFoundException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getUserRoles(userId);
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getUserRoles(userId);
      }
    }
    else {
      return this.getBackupConnector().getUserRoles(userId);
    }
  }

  public SortedSet<LdapUser> getUsers(int userCount)
      throws LdapDAOException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.getUsers(userCount);
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).getUsers(userCount);
      }
    }
    else {
      return this.getBackupConnector().getUsers(userCount);
    }
  }

  public SortedSet<LdapUser> searchUsers(String username, Set<String> roleIds)
      throws LdapDAOException
  {
    if (isOriginalConnectorValid()) {
      try {
        return this.originalLdapManagerConnector.searchUsers(username, roleIds);
      }
      catch (LdapDAOException e) {
        return this.getBackupConnector(e).searchUsers(username, roleIds);
      }
    }
    else {
      return this.getBackupConnector().searchUsers(username, roleIds);
    }
  }

  public String getIdentifier() {
    return this.originalLdapManagerConnector.getIdentifier();
  }

  public LdapContextFactory getLdapContextFactory() throws LdapDAOException {
    if (isOriginalConnectorValid()) {
      return this.originalLdapManagerConnector.getLdapContextFactory();
    }
    else {
      return this.getBackupConnector().getLdapContextFactory();
    }
  }

  public int getRetryDelay() {
    return retryDelay;
  }

  /**
   * Convenience method so we don't have this code in every method. Some sort of dispatch pattern might work better in
   * the place of this.
   */
  private LdapConnector getBackupConnector(LdapDAOException exception) throws LdapDAOException {
    connectionFailed();

    if (this.backupLdapManagerConnector == null) {
      if (log.isDebugEnabled()) {
        log.warn("Problem connecting to LDAP server:", exception);
      }
      else {
        log.warn("Problem connecting to LDAP server: {}", exception.toString());
      }
      throw exception;
    }

    // log stacktrace only if debug is enabled
    if (log.isDebugEnabled()) {
      log.warn("Problem connecting to primary LDAP server, using backup connector", exception);
    }
    else {
      log.warn("Problem connecting to primary LDAP server, using backup connector ({})", exception.toString());
    }

    return this.backupLdapManagerConnector;
  }

  private LdapConnector getBackupConnector() throws LdapDAOException {
    if (this.backupLdapManagerConnector == null) {
      throw new LdapDAOException("Waiting for connection retry timeout.");
    }

    return this.backupLdapManagerConnector;
  }

}
