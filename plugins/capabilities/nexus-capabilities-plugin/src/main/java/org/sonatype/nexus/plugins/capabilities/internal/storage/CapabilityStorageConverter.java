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
package org.sonatype.nexus.plugins.capabilities.internal.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Reader;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Upgrades/Converts configuration between Modello and Kazuki.
 *
 * @since 2.8
 */
@Singleton
@Named
public class CapabilityStorageConverter
    extends ComponentSupport
{

  private final File configDir;

  private CapabilityStorage capabilityStorage;

  @Inject
  public CapabilityStorageConverter(final ApplicationConfiguration applicationConfiguration,
                                    final CapabilityStorage capabilityStorage)
  {
    this.configDir = checkNotNull(applicationConfiguration).getConfigurationDirectory();
    this.capabilityStorage = checkNotNull(capabilityStorage);
  }

  public void convertToKazukiIfNecessary() throws Exception {
    File configFile = new File(configDir, "capabilities.xml");
    if (configFile.exists()) {
      log.info("Converting capabilities from capabilities.xml to Kazuki...");

      // ensure that there are no capabilities in Kazuki
      checkState(
          capabilityStorage.getAll().size() == 0,
          "Could not upgrade capabilities.xml to Kazuki as Kazuki already contains capabilities"
      );

      convertToKazuki(renameCapabilitiesXml(configFile));
    }
  }

  public Configuration convertFromKazuki() throws IOException {
    Map<CapabilityIdentity, CapabilityStorageItem> capabilities = capabilityStorage.getAll();
    Configuration configuration = new Configuration();
    for (Entry<CapabilityIdentity, CapabilityStorageItem> entry : capabilities.entrySet()) {
      configuration.addCapability(asCCapability(entry.getKey().toString(), entry.getValue()));
    }
    return configuration;
  }

  private void convertToKazuki(final File configFile) throws Exception {
    try (Reader r = new FileReader(configFile);
         FileInputStream is = new FileInputStream(configFile);
         Reader fr = new InputStreamReader(is)) {

      Xpp3DomBuilder.build(r);
      Configuration configuration = new NexusCapabilitiesConfigurationXpp3Reader().read(fr);
      List<CCapability> capabilities = configuration.getCapabilities();

      if (capabilities != null) {
        for (final CCapability c : capabilities) {

          Map<String, String> properties = Maps.newHashMap();
          if (c.getProperties() != null) {
            for (final CCapabilityProperty property : c.getProperties()) {
              properties.put(property.getKey(), property.getValue());
            }
          }

          capabilityStorage.add(new CapabilityStorageItem(
              c.getVersion(), c.getTypeId(), c.isEnabled(), c.getNotes(), properties
          ));
        }
        log.info("Converted {} capabilities from capabilities.xml to Kazuki", capabilities.size());
      }
    }
  }

  private File renameCapabilitiesXml(final File configFile) throws Exception {
    File backupFile = new File(configFile.getParentFile(), configFile.getName() + ".old");
    Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    Files.delete(configFile.toPath());
    return backupFile;
  }

  private CCapability asCCapability(final String id, final CapabilityStorageItem item) {
    final CCapability capability = new CCapability();
    capability.setId(id);
    capability.setVersion(item.getVersion());
    capability.setTypeId(item.getType());
    capability.setEnabled(item.isEnabled());
    capability.setNotes(item.getNotes());
    if (item.getProperties() != null) {
      for (Map.Entry<String, String> entry : item.getProperties().entrySet()) {
        final CCapabilityProperty property = new CCapabilityProperty();
        property.setKey(entry.getKey());
        property.setValue(entry.getValue());
        capability.addProperty(property);
      }
    }
    return capability;
  }

}
