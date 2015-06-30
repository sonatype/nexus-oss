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
package org.sonatype.nexus.ldap.internal;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sonatype.nexus.ldap.internal.connector.LdapConnector;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapUser;
import org.sonatype.nexus.ldap.internal.connector.dao.NoLdapUserRolesFoundException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapGroupException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapUserException;

import org.apache.shiro.realm.ldap.LdapContextFactory;

public class MockLdapConnector
    implements LdapConnector
{

  private SortedSet<String> groupIds = new TreeSet<String>();

  private SortedSet<LdapUser> users = new TreeSet<LdapUser>();

  private String identifier;

  private boolean disabled = false;

  public MockLdapConnector(String identifier, SortedSet<LdapUser> users, SortedSet<String> groupIds) {
    this.groupIds = groupIds;
    this.users = users;
    this.identifier = identifier;
  }

  public SortedSet<String> getAllGroups()
      throws LdapDAOException
  {
    this.checkStatus();

    return groupIds;
  }

  public SortedSet<LdapUser> getAllUsers()
      throws LdapDAOException
  {
    this.checkStatus();

    return users;
  }

  public String getGroupName(String groupId)
      throws LdapDAOException,
             NoSuchLdapGroupException
  {
    this.checkStatus();

    if (groupIds.contains(groupId)) {
      return groupId;
    }
    else {
      throw new NoSuchLdapGroupException(groupId, groupId);
    }
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public LdapContextFactory getLdapContextFactory() {
    // only used for binds, which we are not using for the mock
    return null;
  }

  public LdapUser getUser(String username)
      throws NoSuchLdapUserException,
             LdapDAOException
  {
    this.checkStatus();

    for (LdapUser ldapUser : this.users) {
      if (ldapUser.getUsername().equals(username)) {
        return ldapUser;
      }
    }
    throw new NoSuchLdapUserException(username);
  }

  public Set<String> getUserRoles(String username)
      throws LdapDAOException,
             NoLdapUserRolesFoundException
  {
    this.checkStatus();

    for (LdapUser ldapUser : this.users) {
      if (ldapUser.getUsername().equals(username)) {
        // if we do not have any roles, throw
        if (ldapUser.getMembership() == null || ldapUser.getMembership().isEmpty()) {
          throw new NoLdapUserRolesFoundException(username);
        }
        return ldapUser.getMembership();
      }
    }
    // no user, throw
    throw new NoLdapUserRolesFoundException(username);
  }

  public SortedSet<LdapUser> getUsers(int userCount)
      throws LdapDAOException
  {
    this.checkStatus();

    if (this.users.size() < userCount) {
      return this.users;
    }
    else {
      SortedSet<LdapUser> tmpUsers = new TreeSet<LdapUser>();
      int index = 0;
      for (LdapUser ldapUser : this.users) {
        if (index == userCount) {
          break;
        }

        index++; // counter
        tmpUsers.add(ldapUser);
      }
      return tmpUsers;
    }
  }

  public SortedSet<LdapUser> searchUsers(String username, Set<String> roleIds)
      throws LdapDAOException
  {
    this.checkStatus();

    SortedSet<LdapUser> tmpUsers = new TreeSet<LdapUser>();
    for (LdapUser ldapUser : this.users) {
      if (ldapUser.getUsername().startsWith(username)) {
        tmpUsers.add(ldapUser);
      }
    }
    return tmpUsers;
  }

  private void checkStatus()
      throws LdapDAOException
  {
    if (disabled) {
      throw new LdapDAOException("MockLdapConnector is disabled.");
    }
  }

  public void start() {
    this.disabled = false;
  }

  public void stop() {
    this.disabled = true;
  }

}
