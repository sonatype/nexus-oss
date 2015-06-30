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
 * Actual persistence mechanism of {@link LdapConfiguration}.
 */
public interface LdapConfigurationSource
{
  /**
   * Loads all the configuration entries with undefined order.
   */
  List<LdapConfiguration> loadAll();

  /**
   * Creates a new entry, with newly assigned ID that is returned.
   */
  String create(LdapConfiguration ldapConfiguration);

  /**
   * Updates existing entry. Returns {@code true} if entry found and updated.
   */
  boolean update(LdapConfiguration ldapConfiguration);

  /**
   * Deletes existing entry. Returns {@code true} if entry found and deleted.
   */
  boolean delete(String id);
}
