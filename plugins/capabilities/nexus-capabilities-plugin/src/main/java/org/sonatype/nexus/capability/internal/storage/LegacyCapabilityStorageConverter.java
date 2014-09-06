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
package org.sonatype.nexus.capability.internal.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Reader;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Converts legacy XML {@code etc/capabilities.xml} configuration to current storage implementation.
 *
 * @since 2.8
 */
@Named
@Singleton
public class LegacyCapabilityStorageConverter
    extends ComponentSupport
{
  private final ApplicationDirectories applicationDirectories;

  private final CapabilityStorage capabilityStorage;

  @Inject
  public LegacyCapabilityStorageConverter(final ApplicationDirectories applicationDirectories,
                                          final CapabilityStorage capabilityStorage)
  {
    this.applicationDirectories = checkNotNull(applicationDirectories);
    this.capabilityStorage = checkNotNull(capabilityStorage);
  }

  public void maybeConvert() throws Exception {
    File dir = applicationDirectories.getWorkDirectory("etc");
    File file = new File(dir, "capabilities.xml");
    if (file.exists()) {
      log.info("Importing legacy capability entities from: {}", file);

      // ensure current store is empty
      checkState(capabilityStorage.getAll().isEmpty(), "Capability store already contains entities; aborting");

      // convert entities, move file out of the way so next run does not attempt this conversion
      convert(rename(file));
    }
  }

  private File rename(final File file) throws IOException {
    File backupFile = new File(file.getParentFile(), file.getName() + ".old");
    Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    Files.delete(file.toPath());
    return backupFile;
  }

  private void convert(final File file) throws Exception {
    try (Reader reader = new BufferedReader(new FileReader(file))) {
      Configuration configuration = new NexusCapabilitiesConfigurationXpp3Reader().read(reader);
      List<CCapability> capabilities = configuration.getCapabilities();

      if (capabilities == null) {
        log.info("No capabilities defined to convert; aborting");
        return;
      }

      for (final CCapability c : capabilities) {
        // HACK: do not convert outreach related capabilities and let outreach plugin recreate them (NEXUS-6349)
        if ("OutreachMarkerCapability".equals(c.getTypeId()) ||
            "OutreachManagementCapability".equals(c.getTypeId())) {
          continue;
        }

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

      log.info("Converted {} legacy capability entities", capabilities.size());
    }
  }
}
