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

package org.sonatype.nexus.configuration.source;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.upgrade.ApplicationConfigurationUpgrader;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.security.events.SecurityConfigurationChanged;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 *
 * @author cstamas
 */
@Component(role = ApplicationConfigurationSource.class, hint = "file")
public class FileConfigurationSource
    extends AbstractApplicationConfigurationSource
{

  /**
   * The configuration file.
   */
  @org.codehaus.plexus.component.annotations.Configuration(value = "${nexus-work}/conf/nexus.xml")
  private File configurationFile;

  /**
   * The configuration validator.
   */
  @Requirement
  private ApplicationConfigurationValidator configurationValidator;

  /**
   * The configuration upgrader.
   */
  @Requirement
  private ApplicationConfigurationUpgrader configurationUpgrader;

  /**
   * The nexus defaults configuration source.
   */
  @Requirement(hint = "static")
  private ApplicationConfigurationSource nexusDefaults;

  @Requirement
  private EventBus eventBus;

  @Requirement
  private ConfigurationHelper configHelper;

  /**
   * Flag to mark defaulted config
   */
  private boolean configurationDefaulted;

  @Requirement
  private ApplicationStatusSource applicationStatusSource;

  /**
   * Gets the configuration validator.
   *
   * @return the configuration validator
   */
  public ConfigurationValidator getConfigurationValidator() {
    return configurationValidator;
  }

  /**
   * Sets the configuration validator.
   *
   * @param configurationValidator the new configuration validator
   */
  public void setConfigurationValidator(ConfigurationValidator configurationValidator) {
    if (!ApplicationConfigurationValidator.class.isAssignableFrom(configurationValidator.getClass())) {
      throw new IllegalArgumentException("ConfigurationValidator is invalid type "
          + configurationValidator.getClass().getName());
    }

    this.configurationValidator = (ApplicationConfigurationValidator) configurationValidator;
  }

  /**
   * Gets the configuration file.
   *
   * @return the configuration file
   */
  public File getConfigurationFile() {
    return configurationFile;
  }

  /**
   * Sets the configuration file.
   *
   * @param configurationFile the new configuration file
   */
  public void setConfigurationFile(File configurationFile) {
    this.configurationFile = configurationFile;
  }

  public Configuration loadConfiguration()
      throws ConfigurationException, IOException
  {
    // propagate call and fill in defaults too
    nexusDefaults.loadConfiguration();

    if (getConfigurationFile() == null || getConfigurationFile().getAbsolutePath().contains("${")) {
      throw new ConfigurationException("The configuration file is not set or resolved properly: "
          + getConfigurationFile().getAbsolutePath());
    }

    if (!getConfigurationFile().exists()) {
      getLogger().warn("No configuration file in place, copying the default one and continuing with it.");

      // get the defaults and stick it to place
      setConfiguration(nexusDefaults.getConfiguration());

      saveConfiguration(getConfigurationFile());

      configurationDefaulted = true;
    }
    else {
      configurationDefaulted = false;
    }

    try {
      loadConfiguration(getConfigurationFile());

      // was able to load configuration w/o upgrading it
      setConfigurationUpgraded(false);
    }
    catch (ConfigurationException e) {
      getLogger().info("Configuration file is outdated, begin upgrade");

      upgradeConfiguration(getConfigurationFile());

      // had to upgrade configuration before I was able to load it
      setConfigurationUpgraded(true);

      loadConfiguration(getConfigurationFile());

      // if the configuration is upgraded we need to reload the security.
      // it would be great if this was put somewhere else, but I am out of ideas.
      // the problem is the default security was already loaded with the security-system component was loaded
      // so it has the defaults, the upgrade from 1.0.8 -> 1.4 moves security out of the nexus.xml
      // and we cannot use the 'correct' way of updating the info, because that would cause an infinit loop
      // loading the nexus.xml
      this.eventBus.post(new SecurityConfigurationChanged());
    }

    upgradeNexusVersion();

    ValidationResponse vResponse =
        getConfigurationValidator().validateModel(new ValidationRequest(getConfiguration()));

    dumpValidationErrors(vResponse);

    setValidationResponse(vResponse);

    if (vResponse.isValid()) {
      if (vResponse.isModified()) {
        getLogger().info("Validation has modified the configuration, storing the changes.");

        storeConfiguration();
      }

      return getConfiguration();
    }
    else {
      throw new InvalidConfigurationException(vResponse);
    }
  }

  protected void dumpValidationErrors(final ValidationResponse response) {
    // summary
    if (response.getValidationErrors().size() > 0 || response.getValidationWarnings().size() > 0) {
      getLogger().error("* * * * * * * * * * * * * * * * * * * * * * * * * *");

      getLogger().error("Nexus configuration has validation errors/warnings");

      getLogger().error("* * * * * * * * * * * * * * * * * * * * * * * * * *");

      if (response.getValidationErrors().size() > 0) {
        getLogger().error("The ERRORS:");

        for (ValidationMessage msg : response.getValidationErrors()) {
          getLogger().error(msg.toString());
        }
      }

      if (response.getValidationWarnings().size() > 0) {
        getLogger().error("The WARNINGS:");

        for (ValidationMessage msg : response.getValidationWarnings()) {
          getLogger().error(msg.toString());
        }
      }

      getLogger().error("* * * * * * * * * * * * * * * * * * * * *");
    }
    else {
      getLogger().info("Nexus configuration validated successfully.");
    }
  }

  protected void upgradeNexusVersion()
      throws IOException
  {
    final String currentVersion = checkNotNull(applicationStatusSource.getSystemStatus().getVersion());
    final String previousVersion = getConfiguration().getNexusVersion();
    if (currentVersion.equals(previousVersion)) {
      setInstanceUpgraded(false);
    }
    else {
      setInstanceUpgraded(true);
      getConfiguration().setNexusVersion(currentVersion);
      storeConfiguration();
    }

  }

  public void storeConfiguration()
      throws IOException
  {
    saveConfiguration(getConfigurationFile());
  }

  public InputStream getConfigurationAsStream()
      throws IOException
  {
    return new FileInputStream(getConfigurationFile());
  }

  @Override
  public ApplicationConfigurationSource getDefaultsSource() {
    return nexusDefaults;
  }

  protected void upgradeConfiguration(File file)
      throws IOException, ConfigurationException
  {
    getLogger().info("Trying to upgrade the configuration file " + file.getAbsolutePath());

    setConfiguration(configurationUpgrader.loadOldConfiguration(file));

    // after all we should have a configuration
    if (getConfiguration() == null) {
      throw new ConfigurationException("Could not upgrade Nexus configuration! Please replace the "
          + file.getAbsolutePath() + " file with a valid Nexus configuration file.");
    }

    getLogger().info("Creating backup from the old file and saving the upgraded configuration.");

    backupConfiguration();

    saveConfiguration(file);
  }

  /**
   * Load configuration.
   *
   * @param file the file
   * @return the configuration
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void loadConfiguration(File file)
      throws IOException, ConfigurationException
  {
    getLogger().debug("Loading Nexus configuration from " + file.getAbsolutePath());

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);

      loadConfiguration(fis);

      // seems a bit dirty, but the config might need to be upgraded.
      if (this.getConfiguration() != null) {
        // decrypt the passwords
        setConfiguration(configHelper.encryptDecryptPasswords(getConfiguration(), false));
      }
    }
    finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  /**
   * Save configuration.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void saveConfiguration(final File file)
      throws IOException
  {
    // Create the dir if doesn't exist, throw runtime exception on failure
    // bad bad bad
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      String message =
          "\r\n******************************************************************************\r\n"
              + "* Could not create configuration file [ " + file.toString() + "]!!!! *\r\n"
              + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
              + "******************************************************************************";

      getLogger().error(message);
      throw new IOException("Could not create configuration file " + file.getAbsolutePath());
    }

    // Clone the conf so we can encrypt the passwords
    final Configuration configuration = configHelper.encryptDecryptPasswords(getConfiguration(), true);
    getLogger().debug("Saving configuration: {}", file);
    final FileReplacer fileReplacer = new FileReplacer(file);
    // we save this file many times, don't litter backups
    fileReplacer.setDeleteBackupFile(true);
    fileReplacer.replace(new ContentWriter()
    {
      @Override
      public void write(final BufferedOutputStream output)
          throws IOException
      {
        new NexusConfigurationXpp3Writer().write(output, configuration);
      }
    });
  }

  /**
   * Was the active configuration fetched from config file or from default source? True if it from default source.
   */
  public boolean isConfigurationDefaulted() {
    return configurationDefaulted;
  }

  public void backupConfiguration()
      throws IOException
  {
    File file = getConfigurationFile();

    // backup the file
    File backup = new File(file.getParentFile(), file.getName() + ".bak");
    FileUtils.copyFile(file, backup);
  }
}