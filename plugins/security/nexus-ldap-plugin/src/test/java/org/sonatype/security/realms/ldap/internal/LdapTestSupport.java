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

package org.sonatype.security.realms.ldap.internal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.ldaptestsuite.LdapServerConfiguration;
import org.sonatype.ldaptestsuite.Partition;
import org.sonatype.nexus.proxy.maven.routing.Config;
import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;
import org.sonatype.plexus.rest.DefaultReferenceFactory;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.security.realms.ldap.internal.persist.LdapConfigurationSource;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Module;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.codec.Base64;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.junit.After;
import org.junit.Before;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;

/**
 * Support for LDAP UTs with in-memory configuration source.
 */
public abstract class LdapTestSupport
    extends SecurityTestSupport
{
  protected Map<String, LdapServerConfiguration> ldapServerConfigurations = Maps.newHashMap();

  protected Map<String, LdapServer> ldapServers = Maps.newHashMap();

  protected LdapConfigurationSource ldapConfigurationSource = new MockLdapConfigurationSource();

  public static String encodeBase64(final String value) {
    try {
      return Base64.encodeToString(value.getBytes(PREFERRED_ENCODING));
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected void customizeContainerConfiguration(final ContainerConfiguration configuration) {
    super.customizeContainerConfiguration(configuration);
    configuration.setAutoWiring(true);
    configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
  }

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new SecurityModule());
    // test specific bindings
    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(Config.class).toInstance(new ConfigImpl(false));
        binder.bind(ReferenceFactory.class).toInstance(new DefaultReferenceFactory());
        binder.bind(LdapConfigurationSource.class).toInstance(ldapConfigurationSource);
      }
    });
  }

  @Before
  public void startLdap()
      throws Exception
  {
    ldapServerConfigurations = createLdapServerConfigurations();
    startLdapServers();
    final Map<String, LdapConfiguration> ldapClientConfigurations = createLdapClientConfigurations();
    for (LdapConfiguration configuration : ldapClientConfigurations.values()) {
      ldapConfigurationSource.create(configuration);
    }
  }

  protected synchronized void startLdapServers() {
    for (String name : ldapServerConfigurations.keySet()) {
      startLdapServer(name);
    }
  }

  protected synchronized void startLdapServer(final String name) {
    checkArgument(ldapServerConfigurations.containsKey(name), "Unknown LDAP Server %s", name);
    checkArgument(!ldapServers.containsKey(name), "LDAP Server %s already started", name);
    final LdapServer ldapServer = new LdapServer(ldapServerConfigurations.get(name));
    ldapServers.put(name, ldapServer);
  }

  @After
  public void stopLdap()
      throws Exception
  {
    lookup(CacheManager.class).shutdown();
    stopLdapServers();
  }

  protected synchronized void stopLdapServers() {
    for (String name : ldapServerConfigurations.keySet()) {
      stopLdapServer(name);
    }
  }

  protected synchronized void stopLdapServer(final String name) {
    checkArgument(ldapServerConfigurations.containsKey(name), "Unknown LDAP Server %s", name);
    final LdapServer ldapServer = ldapServers.get(name);
    if (ldapServer != null) {
      ldapServer.close();
      ldapServers.remove(name);
    }
  }

  protected Map<String, LdapConfiguration> createLdapClientConfigurations() {
    final Map<String, LdapConfiguration> result = Maps.newHashMap();
    // default implementation creates entries for all defined LDAP servers
    for (String name : ldapServers.keySet()) {
      final LdapServer ldapServer = ldapServers.get(name);

      final LdapConfiguration ldapConfiguration = new LdapConfiguration();
      ldapConfiguration.setId("unused"); // create will override it anyway
      ldapConfiguration.setName(name);
      ldapConfiguration.setOrder(1);
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
      // mapping.setGroupMemberAttribute("groupMemberAttribute");
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
      result.put(name, ldapConfiguration);
    }
    return result;
  }

  protected Map<String, LdapServerConfiguration> createLdapServerConfigurations() {
    final Map<String, LdapServerConfiguration> result = Maps.newHashMap();
    result.put("default", createServerConfiguration("default"));
    return result;
  }

  protected LdapServerConfiguration createServerConfiguration(final String name) {
    return LdapServerConfiguration.builder()
        .withWorkingDirectory(util.createTempDir())
        .withPartitions(createPartition(name))
        .build();
  }

  protected Partition createPartition(final String name) {
    return Partition.builder()
        .withNameAndSuffix(name, "o=sonatype")
        .withIndexedAttributes("objectClass", "o")
        .withRootEntryClasses("top", "organization")
        .withLdifFile(resolveLdifFile(name)).build();
  }

  protected File resolveLdifFile(final String name) {
    checkNotNull(name);
    final File classSpecificLdif = util
        .resolveFile("src/test/resources/" + getClass().getName().replace('.', '/') + "-" + name + "-ldap.ldif");
    if (classSpecificLdif.isFile()) {
      return classSpecificLdif;
    }
    return util.resolveFile("src/test/resources/defaults/default-ldap.ldif");
  }
}
