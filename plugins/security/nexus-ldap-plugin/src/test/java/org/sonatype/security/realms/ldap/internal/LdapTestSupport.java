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
package org.sonatype.security.realms.ldap.internal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.ldaptestsuite.LdapServerConfiguration;
import org.sonatype.ldaptestsuite.Partition;
import org.sonatype.nexus.proxy.maven.routing.Config;
import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;
import org.sonatype.plexus.rest.DefaultReferenceFactory;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.security.realms.ldap.internal.persist.LdapConfigurationSource;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Support for LDAP UTs with in-memory configuration source.
 */
public abstract class LdapTestSupport
    extends SecurityTestSupport
{
  protected LinkedHashMap<String, LdapServerConfiguration> ldapServerConfigurations = Maps.newLinkedHashMap();

  protected LinkedHashMap<String, LdapServer> ldapServers = Maps.newLinkedHashMap();

  protected LinkedHashMap<String, LdapConfiguration> ldapClientConfigurations = Maps.newLinkedHashMap();

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
        final SecuritySystem securitySystem = getBoundSecuritySystem();
        if (securitySystem != null) {
          binder.bind(SecuritySystem.class).toInstance(securitySystem);
        }
      }
    });
  }

  /**
   * SecuritySystem to bind in this test. Usually, you want a mock, but there are FEW ITs/UTs that might want
   * the "real thing", and override this method to return {@code null} or return any other prepped instance.
   */
  protected SecuritySystem getBoundSecuritySystem() {
    return mock(SecuritySystem.class);
  }

  @Before
  public void startLdap()
      throws Exception
  {
    ldapServerConfigurations = createLdapServerConfigurations();
    for (Map.Entry<String, LdapServerConfiguration> configurationEntry : ldapServerConfigurations.entrySet()) {
      final LdapServer ldapServer = new LdapServer(configurationEntry.getValue());
      ldapServers.put(configurationEntry.getKey(), ldapServer);
    }
    startLdapServers();
    ldapClientConfigurations = createLdapClientConfigurations();
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
    checkArgument(ldapServers.containsKey(name), "Unknown LDAP Server %s", name);
    final LdapServer ldapServer = ldapServers.get(name).start();
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
      ldapServer.stop();
    }
  }

  protected LinkedHashMap<String, LdapConfiguration> createLdapClientConfigurations() {
    final LinkedHashMap<String, LdapConfiguration> result = Maps.newLinkedHashMap();
    // default implementation creates entries for all defined LDAP servers
    int order = 1;
    for (Entry<String, LdapServer> entry : ldapServers.entrySet()) {
      order++;
      final LdapConfiguration ldapConfiguration = createLdapClientConfigurationForServer(
          entry.getKey(), order, entry.getValue());
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

  protected LinkedHashMap<String, LdapServerConfiguration> createLdapServerConfigurations() {
    final LinkedHashMap<String, LdapServerConfiguration> result = Maps.newLinkedHashMap();
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
    return resolveDefaultLdifFile();
  }

  protected File resolveDefaultLdifFile() {
    return util.resolveFile("src/test/resources/defaults/ut/default-ldap.ldif");
  }

  // ==

  protected Mapping buildUserAndGroupAuthConfiguration() {
    final Mapping userGroupConf = new Mapping();
    userGroupConf.setUserMemberOfAttribute("userMemberOfAttribute");
    userGroupConf.setGroupBaseDn("groupBaseDn");
    userGroupConf.setGroupIdAttribute("groupIdAttribute");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("groupMemberFormat");
    userGroupConf.setGroupObjectClass("groupObjectClass");
    userGroupConf.setLdapGroupsAsRoles(true);
    userGroupConf.setEmailAddressAttribute("emailAddressAttribute");
    userGroupConf.setUserBaseDn("userBaseDn");
    userGroupConf.setUserIdAttribute("userIdAttribute");
    userGroupConf.setUserObjectClass("userObjectClass");
    userGroupConf.setUserPasswordAttribute("userPasswordAttribute");
    userGroupConf.setUserRealNameAttribute("userRealNameAttribute");
    userGroupConf.setUserSubtree(true);
    return userGroupConf;
  }

  protected Connection buildConnectionInfo() throws UnsupportedEncodingException {
    Connection connInfo = new Connection();
    connInfo.setAuthScheme("ldap");
    connInfo.setBackupHost(new Host(Protocol.ldap, "backupHost", 11111));
    connInfo.setMaxIncidentsCount(3);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost(new Host(Protocol.ldap, "localhost", 386));
    connInfo.setSaslRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));
    return connInfo;
  }

  protected void compareConfiguration(LdapConfiguration expected, LdapConfiguration actual)
      throws Exception
  {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String expectedString = objectMapper.writeValueAsString(expected);
    final String actualString = objectMapper.writeValueAsString(actual);
    assertThat(expected, equalTo(actual));
  }
}
