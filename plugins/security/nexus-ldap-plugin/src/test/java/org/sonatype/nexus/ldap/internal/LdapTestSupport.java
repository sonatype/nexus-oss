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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.sonatype.nexus.ldap.internal.persist.LdapConfigurationSource;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.WebSecurityModule;
import org.sonatype.nexus.security.config.MemorySecurityConfiguration;
import org.sonatype.nexus.security.config.PreconfiguredSecurityConfigurationSource;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.sisu.litmus.testsupport.ldap.LdapServer;
import org.sonatype.sisu.litmus.testsupport.port.PortRegistry;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import net.sf.ehcache.CacheManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.Mockito.mock;

/**
 * Support for LDAP UTs with in-memory configuration source.
 */
public abstract class LdapTestSupport
    extends NexusTestSupport
{
  private final PortRegistry portRegistry = new PortRegistry();

  protected LinkedHashMap<String, LdapServer> ldapServers = Maps.newLinkedHashMap();

  protected LinkedHashMap<String, LdapConfiguration> ldapClientConfigurations = Maps.newLinkedHashMap();

  protected LdapConfigurationSource ldapConfigurationSource = new MockLdapConfigurationSource();

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);

    modules.add(new WebSecurityModule(mock(ServletContext.class)));

    final MemorySecurityConfiguration securityModelConfig = getSecurityModelConfig();
    if (securityModelConfig != null) {
      modules.add(new AbstractModule()
      {
        @Override
        protected void configure() {
          bind(SecurityConfigurationSource.class)
              .annotatedWith(Names.named("default"))
              .toInstance(new PreconfiguredSecurityConfigurationSource(securityModelConfig));
        }
      });
    }

    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(LdapConfigurationSource.class).toInstance(ldapConfigurationSource);
        final SecuritySystem securitySystem = getBoundSecuritySystem();
        if (securitySystem != null) {
          binder.bind(SecuritySystem.class).toInstance(securitySystem);
        }
      }
    });
  }

  protected MemorySecurityConfiguration getSecurityModelConfig() {
    return null;
  }

  /**
   * SecuritySystem to bind in this test. Usually, you want a mock, but there are FEW ITs/UTs that might want
   * the "real thing", and override this method to return {@code null} or return any other prepped instance.
   */
  protected SecuritySystem getBoundSecuritySystem() {
    return mock(SecuritySystem.class);
  }

  @Before
  public void startLdap() throws Exception {
    createLdapServers();
    startLdapServers();
    ldapClientConfigurations = createLdapClientConfigurations();
    for (LdapConfiguration configuration : ldapClientConfigurations.values()) {
      ldapConfigurationSource.create(configuration);
    }
  }

  protected Collection<String> getLdapServerNames() {
    return Collections.singleton("default");
  }

  protected void createLdapServers() {
    for (String name : getLdapServerNames()) {
      ldapServers.put(name, createLdapServer(name));
    }
  }

  protected LdapServer createLdapServer(final String name) {
    return new LdapServer(util.createTempDir(), portRegistry);
  }

  protected synchronized void startLdapServers() throws Exception {
    for (String name : getLdapServerNames()) {
      final LdapServer ldapServer = ldapServers.get(name);
      if (ldapServer != null) {
        ldapServer.start();
        ldapServer.loadData(resolveLdifFile(name));
      }
    }
  }

  protected synchronized void suspendLdapServers() throws Exception {
    for (String name : getLdapServerNames()) {
      suspendLdapServer(name);
    }
  }

  protected synchronized void suspendLdapServer(final String name) throws Exception {
    checkArgument(ldapServers.containsKey(name), "Unknown LDAP Server %s", name);
    final LdapServer ldapServer = ldapServers.get(name);
    if (ldapServer != null) {
      ldapServer.suspend();
    }
  }

  protected synchronized void resumeLdapServers() throws Exception {
    for (String name : getLdapServerNames()) {
      resumeLdapServer(name);
    }
  }

  protected synchronized void resumeLdapServer(final String name) throws Exception {
    checkArgument(ldapServers.containsKey(name), "Unknown LDAP Server %s", name);
    final LdapServer ldapServer = ldapServers.get(name);
    if (ldapServer != null) {
      ldapServer.resume();
    }
  }

  @After
  public void stopLdap() throws Exception {
    lookup(CacheManager.class).shutdown();
    stopLdapServers();
  }

  protected synchronized void stopLdapServers() throws Exception {
    for (String name : getLdapServerNames()) {
      final LdapServer ldapServer = ldapServers.get(name);
      if (ldapServer != null) {
        ldapServer.stop();
      }
    }
  }

  protected LinkedHashMap<String, LdapConfiguration> createLdapClientConfigurations() {
    final LinkedHashMap<String, LdapConfiguration> result = Maps.newLinkedHashMap();
    // default implementation creates entries for all defined LDAP servers
    int order = 1;
    for (Entry<String, LdapServer> entry : ldapServers.entrySet()) {
      order++;
      final LdapConfiguration ldapConfiguration = createLdapClientConfigurationForServer(entry.getKey(), order,
          entry.getValue());
      if (ldapConfiguration != null) {
        result.put(entry.getKey(), ldapConfiguration);
      }
    }
    return result;
  }

  protected LdapConfiguration createLdapClientConfigurationForServer(final String name, final int order,
      final LdapServer ldapServer)
  {
    final LdapConfiguration ldapConfiguration = new LdapConfiguration();
    ldapConfiguration.setId("unused"); // create will override it anyway
    ldapConfiguration.setName(name);
    ldapConfiguration.setOrder(order);
    Connection connection = new Connection();
    connection.setSearchBase("o=sonatype");
    connection.setSystemUsername("uid=admin,ou=system");
    connection.setSystemPassword("secret");
    connection.setAuthScheme("simple");
    connection.setHost(new Host(Protocol.ldap, "localhost", ldapServer.getPort()));
    ldapConfiguration.setConnection(connection);

    final Mapping mapping = new Mapping();
    mapping.setGroupBaseDn("ou=groups");
    mapping.setGroupIdAttribute("cn");
    mapping.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    mapping.setGroupObjectClass("organizationalRole");
    mapping.setLdapGroupsAsRoles(true);
    mapping.setEmailAddressAttribute("mail");
    mapping.setUserMemberOfAttribute("businesscategory");
    mapping.setUserBaseDn("ou=people");
    mapping.setUserIdAttribute("uid");
    mapping.setUserObjectClass("inetOrgPerson");
    mapping.setUserPasswordAttribute("userPassword");
    mapping.setUserRealNameAttribute("sn");
    mapping.setUserSubtree(true);
    ldapConfiguration.setMapping(mapping);
    return ldapConfiguration;
  }

  protected File resolveLdifFile(final String name) {
    checkNotNull(name);
    final File classSpecificLdif = util.resolveFile("src/test/resources/" + getClass().getName().replace('.', '/')
        + "-" + name + "-ldap.ldif");
    if (classSpecificLdif.isFile()) {
      return classSpecificLdif;
    }
    return resolveDefaultLdifFile();
  }

  protected File resolveDefaultLdifFile() {
    return util.resolveFile("src/test/resources/defaults/ut/default-ldap.ldif");
  }

  /**
   * @deprecated Use {@link org.hamcrest.MatcherAssert} directly instead.
   */
  @Deprecated
  protected void assertEquals(String message, Object expected, Object actual) {
    // don't use junit framework Assert due to autoboxing bug
    MatcherAssert.assertThat(message, actual, Matchers.equalTo(expected));
  }

  /**
   * @deprecated Use {@link org.hamcrest.MatcherAssert} directly instead.
   */
  @Deprecated
  protected void assertEquals(Object expected, Object actual) {
    // don't use junit framework Assert
    MatcherAssert.assertThat(actual, Matchers.equalTo(expected));
  }
}
