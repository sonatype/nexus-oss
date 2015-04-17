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
package org.sonatype.nexus.configuration.source;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.configuration.ApplicationDirectories;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;
import org.sonatype.nexus.validation.ValidationResponseException;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 *
 * @author cstamas
 */
@Singleton
@Named("file")
public class FileConfigurationSource
    extends AbstractApplicationConfigurationSource
{
  private final Provider<SystemStatus> systemStatusProvider;

  private final ApplicationConfigurationValidator configurationValidator;

  private final ApplicationConfigurationSource nexusDefaults;

  private final ConfigurationHelper configHelper;

  private final File configurationFile;

  @Inject
  public FileConfigurationSource(final ApplicationDirectories applicationDirectories,
                                 final Provider<SystemStatus> systemStatusProvider,
                                 final ApplicationConfigurationValidator configurationValidator,
                                 final @Named("static") ApplicationConfigurationSource nexusDefaults,
                                 final ConfigurationHelper configHelper)
  {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
    this.configurationValidator = checkNotNull(configurationValidator);
    this.nexusDefaults = checkNotNull(nexusDefaults);
    this.configHelper = checkNotNull(configHelper);

    configurationFile = new File(applicationDirectories.getWorkDirectory("etc"), "nexus.xml");
    log.debug("Configuration file: {}", configurationFile);
  }

  @Override
  public Configuration loadConfiguration() throws IOException {
    if (!configurationFile.exists()) {
      log.info("Installing default configuration");
      setConfiguration(nexusDefaults.loadConfiguration());
      saveConfiguration(configurationFile);
    }

    loadConfiguration(configurationFile.toURI().toURL());

    // seems a bit dirty, but the config might need to be upgraded.
    if (getConfiguration() != null) {
      // decrypt the passwords
      setConfiguration(configHelper.encryptDecryptPasswords(getConfiguration(), false));
    }

    upgradeNexusVersion();

    ValidationResponse vResponse = configurationValidator.validateModel(getConfiguration());
    dumpValidationErrors(vResponse);
    if (vResponse.isValid()) {
      if (vResponse.isModified()) {
        log.info("Validation has modified the configuration, storing the changes.");

        storeConfiguration();
      }

      return getConfiguration();
    }
    throw new ValidationResponseException(vResponse);
  }

  private void dumpValidationErrors(final ValidationResponse response) {
    if (response.getErrors().size() > 0 || response.getWarnings().size() > 0) {
      log.error("* * * * * * * * * * * * * * * * * * * * * * * * * *");
      log.error("Nexus configuration has validation errors/warnings");
      log.error("* * * * * * * * * * * * * * * * * * * * * * * * * *");

      if (response.getErrors().size() > 0) {
        log.error("The ERRORS:");
        for (ValidationMessage msg : response.getErrors()) {
          log.error(msg.toString());
        }
      }

      if (response.getWarnings().size() > 0) {
        log.error("The WARNINGS:");
        for (ValidationMessage msg : response.getWarnings()) {
          log.error(msg.toString());
        }
      }

      log.error("* * * * * * * * * * * * * * * * * * * * *");
    }
    else {
      log.info("Nexus configuration validated successfully.");
    }
  }

  private void upgradeNexusVersion() throws IOException {
    final String currentVersion = checkNotNull(systemStatusProvider.get().getVersion());
    final String previousVersion = getConfiguration().getNexusVersion();
    if (!currentVersion.equals(previousVersion)) {
      getConfiguration().setNexusVersion(currentVersion);
      storeConfiguration();
    }
  }

  @Override
  public void storeConfiguration() throws IOException {
    saveConfiguration(configurationFile);
  }

  private void saveConfiguration(final File file) throws IOException {
    // Create the dir if doesn't exist, throw runtime exception on failure
    // bad bad bad
    try {
      DirSupport.mkdir(file.getParentFile().toPath());
    }
    catch (IOException e) {
      String message =
          "\r\n******************************************************************************\r\n"
              + "* Could not create configuration file [ " + file + "]!!!! *\r\n"
              + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
              + "******************************************************************************";

      log.error(message, e);
      throw new IOException("Could not create configuration file " + file.getAbsolutePath(), e);
    }

    // Clone the conf so we can encrypt the passwords
    final Configuration configuration = configHelper.encryptDecryptPasswords(getConfiguration(), true);
    log.debug("Saving configuration: {}", file);
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
}
