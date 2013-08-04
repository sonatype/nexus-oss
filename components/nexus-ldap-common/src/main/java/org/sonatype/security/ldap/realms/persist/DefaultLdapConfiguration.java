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

package org.sonatype.security.ldap.realms.persist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import org.sonatype.security.ldap.realms.persist.model.Configuration;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Writer;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipherException;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role = LdapConfiguration.class, hint = "default", instantiationStrategy = "singleton")
public class DefaultLdapConfiguration
    extends AbstractLoggingComponent
    implements LdapConfiguration
{
  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  @Requirement(role = ConfigurationValidator.class)
  private ConfigurationValidator validator;

  @Requirement
  private PasswordHelper passwordHelper;

  @Requirement
  private PasswordEncoderManager passwordEncoderManager;

  @Requirement
  private EventBus eventBus;

  private Configuration configuration;

  private ReentrantLock lock = new ReentrantLock();

  protected File getConfigurationFile() {
    return new File(applicationConfiguration.getConfigurationDirectory(), "ldap.xml");
  }

  public CConnectionInfo readConnectionInfo() {
    CConnectionInfo connInfo = getConfiguration().getConnectionInfo();

    return connInfo;
  }

  public CUserAndGroupAuthConfiguration readUserAndGroupConfiguration() {
    return getConfiguration().getUserAndGroupConfig();
  }

  public void updateUserAndGroupConfiguration(CUserAndGroupAuthConfiguration userAndGroupConfig)
      throws InvalidConfigurationException
  {
    lock.lock();

    try {
      ValidationResponse vr = validator.validateUserAndGroupAuthConfiguration(null, userAndGroupConfig);

      if (vr.getValidationErrors().size() > 0) {
        throw new InvalidConfigurationException(vr);
      }

      getConfiguration().setUserAndGroupConfig(userAndGroupConfig);
    }
    finally {
      lock.unlock();
    }
  }

  public void updateConnectionInfo(CConnectionInfo connectionInfo)
      throws InvalidConfigurationException
  {
    lock.lock();

    try {
      ValidationResponse vr = validator.validateConnectionInfo(null, connectionInfo);

      if (vr.getValidationErrors().size() > 0) {
        throw new InvalidConfigurationException(vr);
      }

      getConfiguration().setConnectionInfo(connectionInfo);
    }
    finally {
      lock.unlock();
    }

  }

  public Configuration getConfiguration() {
    Reader fr = null;
    FileInputStream is = null;
    lock.lock();
    try {
      final File configurationFile = getConfigurationFile();

      if (configuration != null) {
        return configuration;
      }

      is = new FileInputStream(configurationFile);

      LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();

      fr = new InputStreamReader(is);

      configuration = reader.read(fr);

      ValidationResponse vr = validator.validateModel(new ValidationRequest(configuration));

      if (vr.getValidationErrors().size() > 0) {
        // TODO need to code the handling of invalid config
        configuration = new Configuration();
      }

      // decrypt the password, if it fails assume the password is clear text.
      // If the password is wrong the the LDAP Realm will not work, which is no different. If the user typed in
      // the
      // password wrong.
      if (configuration.getConnectionInfo() != null
          && StringUtils.isNotEmpty(configuration.getConnectionInfo().getSystemPassword())) {
        try {
          configuration.getConnectionInfo().setSystemPassword(
              passwordHelper.decrypt(configuration.getConnectionInfo().getSystemPassword()));
        }
        catch (PlexusCipherException e) {
          this.getLogger().error(
              "Failed to decrypt password, assuming the password in file: '"
                  + configurationFile.getAbsolutePath() + "' is clear text.", e);
        }
      }

    }
    catch (FileNotFoundException e) {
      // This is ok, may not exist first time around
      configuration = this.getDefaultConfiguration();
    }
    catch (IOException e) {
      getLogger().error("IOException while retrieving configuration file", e);
    }
    catch (XmlPullParserException e) {
      getLogger().error("Invalid XML Configuration", e);
    }
    finally {
      if (fr != null) {
        try {
          fr.close();
        }
        catch (IOException e) {
          // just closing if open
        }
      }

      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          // just closing if open
        }
      }

      lock.unlock();
    }

    return configuration;
  }

  public void save() {
    lock.lock();
    try {
      final File configurationFile = getConfigurationFile();
      if (!configurationFile.getParentFile().exists() && !configurationFile.getParentFile().mkdirs()) {
        String message =
            "\r\n******************************************************************************\r\n"
                + "* Could not create configuration file [ "
                + configurationFile.toString()
                + "]!!!! *\r\n"
                +
                "* Application cannot start properly until the process has read+write permissions to this folder *\r\n"
                + "******************************************************************************";
        getLogger().error(message);
        throw new IOException("Could not create configuration file " + configurationFile.getAbsolutePath());
      }

      final Configuration configuration = this.configuration.clone();
      // change the password to be encrypted
      if (configuration.getConnectionInfo() != null
          && StringUtils.isNotEmpty(configuration.getConnectionInfo().getSystemPassword())) {
        try {
          configuration.getConnectionInfo().setSystemPassword(
              passwordHelper.encrypt(configuration.getConnectionInfo().getSystemPassword()));
        }
        catch (PlexusCipherException e) {
          getLogger().error("Failed to encrypt password while storing configuration file", e);
        }
      }

      // perform the "safe save"
      getLogger().debug("Saving configuration: {}", configurationFile);
      final FileReplacer fileReplacer = new FileReplacer(configurationFile);
      fileReplacer.setDeleteBackupFile(true);

      fileReplacer.replace(new ContentWriter()
      {
        @Override
        public void write(final BufferedOutputStream output)
            throws IOException
        {
          new LdapConfigurationXpp3Writer().write(output, configuration);
        }
      });
    }
    catch (IOException e) {
      getLogger().error("IOException while storing configuration file", e);
    }
    finally {
      lock.unlock();
    }

    // fire clear cache event
    this.eventBus.post(new LdapClearCacheEvent(this));
  }

  public void clearCache() {
    configuration = null;

    // fire clear cache event
    this.eventBus.post(new LdapClearCacheEvent(null));
  }

  private Configuration getDefaultConfiguration() {

    Configuration defaultConfig = null;

    Reader fr = null;
    InputStream is = null;
    try {
      is = getClass().getResourceAsStream("/META-INF/realms/ldap.xml");
      LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();
      fr = new InputStreamReader(is);
      defaultConfig = reader.read(fr);
    }
    catch (IOException e) {
      this.getLogger().error(
          "Failed to read default LDAP Realm configuration.  This may be corrected while the application is running.",
          e);
      defaultConfig = new Configuration();
    }
    catch (XmlPullParserException e) {
      this.getLogger().error(
          "Failed to read default LDAP Realm configuration.  This may be corrected while the application is running.",
          e);
      defaultConfig = new Configuration();
    }
    finally {
      if (fr != null) {
        try {
          fr.close();
        }
        catch (IOException e) {
          // just closing if open
        }
      }

      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          // just closing if open
        }
      }
    }
    return defaultConfig;
  }

  public LdapAuthConfiguration getLdapAuthConfiguration() {
    CUserAndGroupAuthConfiguration userAndGroupsConf = readUserAndGroupConfiguration();
    LdapAuthConfiguration authConfig = new LdapAuthConfiguration();

    authConfig.setEmailAddressAttribute(userAndGroupsConf.getEmailAddressAttribute());
    // authConfig.setPasswordEncoding( userAndGroupsConf.getPreferredPasswordEncoding() );
    authConfig.setUserBaseDn(StringUtils.defaultString(userAndGroupsConf.getUserBaseDn(), ""));
    authConfig.setUserIdAttribute(userAndGroupsConf.getUserIdAttribute());
    authConfig.setUserObjectClass(userAndGroupsConf.getUserObjectClass());
    authConfig.setPasswordAttribute(userAndGroupsConf.getUserPasswordAttribute());
    authConfig.setUserRealNameAttribute(userAndGroupsConf.getUserRealNameAttribute());

    authConfig.setGroupBaseDn(StringUtils.defaultString(userAndGroupsConf.getGroupBaseDn(), ""));
    authConfig.setGroupIdAttribute(userAndGroupsConf.getGroupIdAttribute());
    // authConfig.setGroupMappings( groupMappings )
    authConfig.setGroupMemberAttribute(userAndGroupsConf.getGroupMemberAttribute());
    authConfig.setGroupMemberFormat(userAndGroupsConf.getGroupMemberFormat());
    authConfig.setGroupObjectClass(userAndGroupsConf.getGroupObjectClass());
    authConfig.setUserSubtree(userAndGroupsConf.isUserSubtree());
    authConfig.setGroupSubtree(userAndGroupsConf.isGroupSubtree());
    authConfig.setUserMemberOfAttribute(userAndGroupsConf.getUserMemberOfAttribute());
    authConfig.setLdapGroupsAsRoles(userAndGroupsConf.isLdapGroupsAsRoles());
    authConfig.setLdapFilter(userAndGroupsConf.getLdapFilter());
    return authConfig;
  }
}
