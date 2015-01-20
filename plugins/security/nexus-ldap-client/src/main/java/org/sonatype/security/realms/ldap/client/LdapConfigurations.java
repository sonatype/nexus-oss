/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.ldap.client;

import java.util.List;

import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;

/**
 * A collection of LDAP server configurations. One can have multiple LDAP servers configured and ordered. This will
 * mean that NX will reach to LDAP servers to perform queries in specific order. First LDAP entity being added causes
 * that LDAP realm gets activated. Similarly, last LDAP entity being deleted deactivates the LDAP realm too.
 *
 * @since 3.0
 */
public interface LdapConfigurations
{
  /**
   * Returns all LDAP existing configuration entities, or empty list. Never {@code null}.
   */
  List<Configuration> get();

  /**
   * Returns LDAP configuration entity by it's ID or throws (for example {@link NexusClientNotFoundException} for
   * nonexistent ID).
   */
  Configuration get(String id);

  /**
   * Creates an LDAP configuration entity. After setting it one must save it. First LDAP entity being saved causes that
   * LDAP realm gets activated. Similarly, last entity being deleted deactivates the LDAP realm too.
   */
  Configuration create();

  /**
   * Sets the entity order based on passed entity ID list.
   */
  void order(List<String> ids);

  /**
   * Clears LDAP caches.
   */
  void clearCaches();
}
