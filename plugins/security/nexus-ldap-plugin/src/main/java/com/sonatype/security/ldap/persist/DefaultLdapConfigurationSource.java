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
package com.sonatype.security.ldap.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import com.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;
import com.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Writer;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.ModelUtils.CorruptModelException;
import org.sonatype.nexus.configuration.ModelUtils.Versioned;
import org.sonatype.nexus.configuration.ModelloUtils;
import org.sonatype.nexus.configuration.ModelloUtils.ModelloModelReader;
import org.sonatype.nexus.configuration.ModelloUtils.ModelloModelUpgrader;
import org.sonatype.nexus.configuration.ModelloUtils.ModelloModelWriter;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class DefaultLdapConfigurationSource
    extends ComponentSupport
    implements LdapConfigurationSource
{
  private static final String OSS_VERSION_MARKER = "OSS-XML";

  /**
   * Model reader and versioned to detect XML model version, with special care about OSS and Pro models.
   */
  private static class LdapModelReader
      extends ModelloModelReader<CLdapConfiguration>
      implements Versioned
  {
    private final LdapConfigurationXpp3Reader modelloReader = new LdapConfigurationXpp3Reader();

    @Override
    public CLdapConfiguration doRead(final Reader reader) throws IOException, XmlPullParserException {
      return modelloReader.read(reader);
    }

    @Override
    public String readVersion(final InputStream input) throws IOException, CorruptModelException {
      // special handling for versions needed, as we might hit OSS or Pro ldap.xml
      // Legacy code relied on fact that OSS ldap.xml was NOT VERSIONED, which is not true anymore
      // so here, we basically inspect the structure of XML as they differ on 1st level sibling of root:
      // if <servers> found, we deal with Pro XML, otherwise it's OSS
      try (final Reader r = new InputStreamReader(input, charset)) {
        try {
          final Xpp3Dom dom = Xpp3DomBuilder.build(r);
          final Xpp3Dom versionNode = dom.getChild("version");
          if (versionNode != null && !Strings.isNullOrEmpty(versionNode.getValue()) && dom.getChildCount() < 2) {
            // edge case: EMPTY (only version present) XML is Pro, as only Pro was writing versions out before
            return versionNode.getValue();
          }
          final Xpp3Dom serversNode = dom.getChild("servers");
          if (serversNode != null) {
            // servers node exists, this is Pro, and is versioned, use standard ways to get it's version
            // and enforce version presence
            if (versionNode != null) {
              if (Strings.isNullOrEmpty(versionNode.getValue())) {
                throw new CorruptModelException("Nexus Pro LDAP XML model invalid: empty 'version' node");
              }
              return versionNode.getValue();
            }
            else {
              throw new CorruptModelException("Passed in LDAP model does not have 'version' node");
            }
          }
          else {
            // servers node not exists, this is OSS and only one model version exists of it
            return OSS_VERSION_MARKER;
          }
        }
        catch (XmlPullParserException e) {
          throw new CorruptModelException("Passed in XML model cannot be parsed", e);
        }
      }
    }
  }

  /**
   * Model writer.
   */
  private static class LdapModelWriter
      extends ModelloModelWriter<CLdapConfiguration>
  {
    private final LdapConfigurationXpp3Writer modelloWriter = new LdapConfigurationXpp3Writer();

    @Override
    public void write(final Writer writer, final CLdapConfiguration model) throws IOException {
      model.setVersion(CLdapConfiguration.MODEL_VERSION);
      modelloWriter.write(writer, model);
    }
  }

  /**
   * OSS Ldap configuration to Pro 2.8.0 upgrader.
   */
  private static class LdapOSSTo280Upgrader
      extends ModelloModelUpgrader
  {
    private final org.sonatype.security.ldap.realms.persist.PasswordHelper legacyPasswordHelper;

    private final PasswordHelper passwordHelper;

    protected LdapOSSTo280Upgrader(
        final org.sonatype.security.ldap.realms.persist.PasswordHelper legacyPasswordHelper,
        final PasswordHelper passwordHelper)
    {
      super(OSS_VERSION_MARKER, "2.8.0");
      this.legacyPasswordHelper = legacyPasswordHelper;
      this.passwordHelper = passwordHelper;
    }

    @Override
    public void doUpgrade(final Reader reader, final Writer writer) throws IOException, XmlPullParserException {
      final org.sonatype.security.ldap.realms.persist.model.Configuration ossConfiguration = new org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader()
          .read(reader);
      // decrypt legacy if needed
      if (ossConfiguration.getConnectionInfo() != null && !Strings.isNullOrEmpty(
          ossConfiguration.getConnectionInfo().getSystemPassword())) {
        try {
          ossConfiguration.getConnectionInfo()
              .setSystemPassword(
                  legacyPasswordHelper.decrypt(ossConfiguration.getConnectionInfo().getSystemPassword()));
        }
        catch (Exception e) {
          throw new CorruptModelException("Could not decrypt legacy encrypted LDAP password during upgrade", e);
        }
      }
      final CLdapServerConfiguration newServerConfig = new CLdapServerConfiguration();
      newServerConfig.setId("default");
      newServerConfig.setName(buildName(ossConfiguration.getConnectionInfo()));
      newServerConfig.setConnectionInfo(convertConnectionInfo(ossConfiguration.getConnectionInfo()));
      // encrypt system password
      if (newServerConfig.getConnectionInfo() != null
          && !Strings.isNullOrEmpty(newServerConfig.getConnectionInfo().getSystemPassword())) {
        try {
          newServerConfig.getConnectionInfo().setSystemPassword(
              passwordHelper.encrypt(newServerConfig.getConnectionInfo().getSystemPassword()));
        }
        catch (Exception e) {
          // this means JRE does not have needed JCE stuff? Die hard
          throw Throwables.propagate(e);
        }
      }
      newServerConfig.setUserAndGroupConfig(convertUserAndGroupInfo(ossConfiguration.getUserAndGroupConfig()));
      final CLdapConfiguration newConfiguration = new CLdapConfiguration();
      newConfiguration.setVersion(toVersion());
      newConfiguration.addServer(newServerConfig);
      new LdapConfigurationXpp3Writer().write(writer, newConfiguration);
    }

    private CConnectionInfo convertConnectionInfo(org.sonatype.security.ldap.realms.persist.model.CConnectionInfo old) {
      final CConnectionInfo newConnectionInfo = new CConnectionInfo();
      newConnectionInfo.setAuthScheme(old.getAuthScheme());
      newConnectionInfo.setCacheTimeout(CConnectionInfo.CACHE_TIMEOUT_DEFAULT);
      newConnectionInfo.setConnectionRetryDelay(CConnectionInfo.CONNECTION_RETRY_DELAY_DEFAULT);
      newConnectionInfo.setConnectionTimeout(CConnectionInfo.CONNECTION_TIMEOUT);
      newConnectionInfo.setHost(old.getHost());
      newConnectionInfo.setPort(old.getPort());
      newConnectionInfo.setProtocol(old.getProtocol());
      newConnectionInfo.setRealm(old.getRealm());
      newConnectionInfo.setSearchBase(old.getSearchBase());
      newConnectionInfo.setSystemPassword(old.getSystemPassword());
      newConnectionInfo.setSystemUsername(old.getSystemUsername());
      return newConnectionInfo;
    }

    private CUserAndGroupAuthConfiguration convertUserAndGroupInfo(
        org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration old)
    {
      final CUserAndGroupAuthConfiguration newUserAndGroup = new CUserAndGroupAuthConfiguration();
      newUserAndGroup.setUserMemberOfAttribute(old.getUserMemberOfAttribute());
      newUserAndGroup.setGroupBaseDn(old.getGroupBaseDn());
      newUserAndGroup.setGroupIdAttribute(old.getGroupIdAttribute());
      newUserAndGroup.setGroupMemberAttribute(old.getGroupMemberAttribute());
      newUserAndGroup.setGroupMemberFormat(old.getGroupMemberFormat());
      newUserAndGroup.setGroupObjectClass(old.getGroupObjectClass());
      newUserAndGroup.setLdapGroupsAsRoles(old.isLdapGroupsAsRoles());
      newUserAndGroup.setGroupSubtree(old.isGroupSubtree());

      newUserAndGroup.setEmailAddressAttribute(old.getEmailAddressAttribute());
      newUserAndGroup.setUserBaseDn(old.getUserBaseDn());
      newUserAndGroup.setUserIdAttribute(old.getUserIdAttribute());
      newUserAndGroup.setUserObjectClass(old.getUserObjectClass());
      newUserAndGroup.setUserPasswordAttribute(old.getUserPasswordAttribute());
      newUserAndGroup.setUserRealNameAttribute(old.getUserRealNameAttribute());
      newUserAndGroup.setUserSubtree(old.isUserSubtree());

      newUserAndGroup.setLdapFilter(old.getLdapFilter());
      newUserAndGroup.setUserMemberOfAttribute(old.getUserMemberOfAttribute());
      return newUserAndGroup;
    }

    private String buildName(org.sonatype.security.ldap.realms.persist.model.CConnectionInfo connectionInfo) {
      final StringBuilder buffer = new StringBuilder();
      buffer.append(connectionInfo.getProtocol()).append("://");
      buffer.append(connectionInfo.getHost()).append(":");
      buffer.append(connectionInfo.getPort());
      return buffer.toString();
    }
  }

  /**
   * Model version 2.0.1 was the only model used in Nexus 2.x line, so this upgrader just changes the model version
   * to latest, as the model itself did not change.
   */
  private static class Ldap201To280Upgrader
      extends ModelloModelUpgrader
  {
    protected Ldap201To280Upgrader() {
      super("2.0.1", "2.8.0");
    }

    @Override
    public void doUpgrade(final Reader reader, final Writer writer) throws IOException, XmlPullParserException {
      // no model structure change, merely the version
      final CLdapConfiguration configuration = new LdapConfigurationXpp3Reader().read(reader);
      configuration.setVersion(toVersion());
      new LdapConfigurationXpp3Writer().write(writer, configuration);
    }
  }

  private final PasswordHelper passwordHelper;

  private final File configurationFile;

  private final LdapModelReader ldapModelReader;

  private final LdapModelWriter ldapModelWriter;

  private final LdapOSSTo280Upgrader ldapOSSTo280Upgrader;

  private final Ldap201To280Upgrader ldap201To280Upgrader;

  @Inject
  public DefaultLdapConfigurationSource(final ApplicationConfiguration applicationConfiguration,
                                        final PasswordHelper passwordHelper,
                                        final org.sonatype.security.ldap.realms.persist.PasswordHelper legacyPasswordHelper)
  {
    checkNotNull(applicationConfiguration);
    checkNotNull(passwordHelper);
    checkNotNull(legacyPasswordHelper);
    this.passwordHelper = passwordHelper;
    this.configurationFile = new File(applicationConfiguration.getConfigurationDirectory(), "ldap.xml");
    this.ldapModelReader = new LdapModelReader();
    this.ldapModelWriter = new LdapModelWriter();
    this.ldapOSSTo280Upgrader = new LdapOSSTo280Upgrader(legacyPasswordHelper, passwordHelper);
    this.ldap201To280Upgrader = new Ldap201To280Upgrader();
  }

  @Override
  public CLdapConfiguration load() throws ConfigurationException, IOException {
    log.debug("Loading LDAP configuration: {}", configurationFile);
    try {
      final CLdapConfiguration configuration = ModelloUtils
          .load(CLdapConfiguration.MODEL_VERSION, configurationFile, ldapModelReader, ldapOSSTo280Upgrader,
              ldap201To280Upgrader);
      decryptPasswords(configuration);
      return configuration;
    }
    catch (CorruptModelException e) {
      throw new InvalidConfigurationException("LDAP configuration is corrupted", e);
    }
    catch (FileNotFoundException e) {
      // This is ok, may not exist first time around
      return defaultConfiguration();
    }
  }

  @Override
  public void save(final CLdapConfiguration configuration) throws IOException {
    log.debug("Saving LDAP configuration: {}", configurationFile);
    final CLdapConfiguration savedConfiguration = configuration.clone();
    encryptPasswords(savedConfiguration);
    ModelloUtils.save(savedConfiguration, configurationFile, ldapModelWriter);
  }

  private void encryptPasswords(final CLdapConfiguration ldapConfiguration) {
    for (CLdapServerConfiguration ldapServer : ldapConfiguration.getServers()) {
      if (ldapServer.getConnectionInfo() != null
          && !Strings.isNullOrEmpty(ldapServer.getConnectionInfo().getSystemPassword())) {
        try {
          ldapServer.getConnectionInfo().setSystemPassword(
              passwordHelper.encrypt(ldapServer.getConnectionInfo().getSystemPassword()));
        }
        catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    }
  }

  private void decryptPasswords(final CLdapConfiguration ldapConfiguration) {
    for (CLdapServerConfiguration ldapServer : ldapConfiguration.getServers()) {
      if (ldapServer.getConnectionInfo() != null
          && !Strings.isNullOrEmpty(ldapServer.getConnectionInfo().getSystemPassword())) {
        try {
          ldapServer.getConnectionInfo().setSystemPassword(
              passwordHelper.decrypt(ldapServer.getConnectionInfo().getSystemPassword()));
        }
        catch (Exception e) {
          log.warn("Failed to LDAP decrypt passwords, loading them as plain text.", e);
        }
      }
    }
  }

  private CLdapConfiguration defaultConfiguration() {
    CLdapConfiguration ldapConfiguration = new CLdapConfiguration();
    ldapConfiguration.setVersion(CLdapConfiguration.MODEL_VERSION);
    ldapConfiguration.setServers(new ArrayList<CLdapServerConfiguration>());
    return ldapConfiguration;
  }
}
