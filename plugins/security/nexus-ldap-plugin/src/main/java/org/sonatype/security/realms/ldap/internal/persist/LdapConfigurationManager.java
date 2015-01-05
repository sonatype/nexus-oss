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
package org.sonatype.security.realms.ldap.internal.persist;

import java.util.List;

import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;

/**
 * Component responsible for handling caching, eventing and driving persistence using {@link LdapConfigurationSource}.
 */
public interface LdapConfigurationManager
{
  /**
   * Clears in-memory cache of LDAP server configuration.
   */
  void clearCache();

  /**
   * Lists defined LDAP server configurations.
   */
  List<LdapConfiguration> listLdapServerConfigurations();

  /**
   * Returns LDAP server configuration by it's ID, never {@code null}. If not found, exception will be thrown.
   */
  LdapConfiguration getLdapServerConfiguration(String id) throws LdapServerNotFoundException;

  /**
   * Creates LDAP server configuration with new ID that is returned. The realm might get activated,
   * if the added server is very first in the configuration.
   */
  void addLdapServerConfiguration(LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException;

  /**
   * Updates LDAP server configuration. If not found, exception will be thrown.
   */
  void updateLdapServerConfiguration(LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException, LdapServerNotFoundException;

  /**
   * Deletes the LDAP server configuration. If not found, exception will be thrown.
   */
  void deleteLdapServerConfiguration(String id) throws LdapServerNotFoundException;

  /**
   * Sets LDAP server order. The parameter is ordered list of IDs of existing LDAP server configurations.
   */
  void setServerOrder(List<String> orderdServerIds) throws IllegalArgumentException;
}
