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
package org.sonatype.nexus.ldap.internal.persist;

import java.util.List;

import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;

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
   * Lists defined LDAP server configurations. If none defined, empty list returned.
   */
  List<LdapConfiguration> listLdapServerConfigurations();

  /**
   * Returns LDAP server configuration by it's ID, never {@code null}. If not found, exception will be thrown.
   *
   * @throws LdapServerNotFoundException if configuration with given ID does not exists.
   */
  LdapConfiguration getLdapServerConfiguration(String id) throws LdapServerNotFoundException;

  /**
   * Creates LDAP server configuration with new ID assigned that is returned. The realm might get activated,
   * if the added server is very first in the configuration.
   *
   * @throws IllegalArgumentException if configuration contains invalid settings.
   */
  String addLdapServerConfiguration(LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException;

  /**
   * Updates LDAP server configuration. If not found, or configuration is invalid, exception will be thrown.
   *
   * @throws LdapServerNotFoundException if configuration with given ID does not exists.
   * @throws IllegalArgumentException    if configuration contains invalid settings.
   */
  void updateLdapServerConfiguration(LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException, LdapServerNotFoundException;

  /**
   * Deletes the LDAP server configuration. If not found, exception will be thrown.
   *
   * @throws LdapServerNotFoundException if configuration with given ID does not exists.
   */
  void deleteLdapServerConfiguration(String id) throws LdapServerNotFoundException;

  /**
   * Sets LDAP server order. The parameter is ordered list of IDs of existing LDAP server configurations.
   *
   * @throws IllegalArgumentException if the passed order is wrong in any way that it contains non-existent IDs, or
   *                                  does not contains existent IDs.
   */
  void setServerOrder(List<String> orderdServerIds) throws IllegalArgumentException;
}
