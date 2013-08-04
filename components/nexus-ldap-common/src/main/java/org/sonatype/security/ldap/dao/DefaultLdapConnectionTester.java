/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.security.ldap.dao;

import java.util.SortedSet;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = LdapConnectionTester.class)
public class DefaultLdapConnectionTester
    implements LdapConnectionTester
{

  @Requirement
  private LdapUserDAO ldapUserDao;

  @Requirement
  private LdapGroupDAO ldapGroupDAO;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void testConnection(LdapContextFactory ldapContextFactory)
      throws NamingException
  {
    // get the connection and close it, if this throws an exception, then the config is wrong.
    LdapContext ctx = null;
    try {
      ctx = ldapContextFactory.getSystemLdapContext();
      ctx.getAttributes("");
    }
    finally {
      if (ctx != null) {
        try {
          ctx.close();
        }
        catch (NamingException e) {
          // ignore, it might not even be open
        }
      }
    }

  }

  public SortedSet<LdapUser> testUserAndGroupMapping(LdapContextFactory ldapContextFactory,
                                                     LdapAuthConfiguration ldapAuthConfiguration, int numberOfResults)
      throws LdapDAOException,
             NamingException
  {
    LdapContext ctx = ldapContextFactory.getSystemLdapContext();
    try {
      SortedSet<LdapUser> users = this.ldapUserDao.getUsers(
          ctx,
          ldapAuthConfiguration,
          numberOfResults);

      if (ldapAuthConfiguration.isLdapGroupsAsRoles()
          && StringUtils.isEmpty(ldapAuthConfiguration.getUserMemberOfAttribute())) {
        for (LdapUser ldapUser : users) {
          try {
            ldapUser.setMembership(
                this.ldapGroupDAO.getGroupMembership(ldapUser.getUsername(), ctx, ldapAuthConfiguration));
          }
          catch (NoLdapUserRolesFoundException e) {
            // this is ok, the users has no roles, not a problem
            if (logger.isDebugEnabled()) {
              this.logger.debug("While testing for user mapping user: " + ldapUser.getUsername()
                  + " had no roles.");
            }
          }
        }
      }
      return users;
    }
    finally {
      try {
        ctx.close();
      }
      catch (NamingException e) {
        // ignore, it might not even be open
      }
    }
  }

}
