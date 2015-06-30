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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.sonatype.nexus.ldap.internal.persist.LdapConfigurationSource;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A simple in-memory LDAP configuration source.
 */
public class MockLdapConfigurationSource
    implements LdapConfigurationSource
{
  private final LinkedHashMap<String, LdapConfiguration> configuration;

  public MockLdapConfigurationSource()
  {
    this.configuration = Maps.newLinkedHashMap();
  }

  @Override
  public List<LdapConfiguration> loadAll() {
    return Lists.newArrayList(configuration.values());
  }

  @Override
  public String create(final LdapConfiguration ldapConfiguration) {
    ldapConfiguration.setId(UUID.randomUUID().toString());
    configuration.put(ldapConfiguration.getId(), ldapConfiguration);
    // keep ordering
    int order = 1;
    for (LdapConfiguration c : configuration.values()) {
      c.setOrder(order++);
    }
    return ldapConfiguration.getId();
  }

  @Override
  public boolean update(final LdapConfiguration ldapConfiguration) {
    return configuration.put(ldapConfiguration.getId(), ldapConfiguration) != null;
  }

  @Override
  public boolean delete(final String id) {
    return configuration.remove(id) != null;
  }

  public void purge() {
    configuration.clear();
  }
}
