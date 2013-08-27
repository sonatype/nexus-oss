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

package org.sonatype.nexus.configuration.application.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.source.FileConfigurationSource;
import org.sonatype.nexus.util.FileUtils;
import org.sonatype.security.guice.SecurityModule;

import com.google.common.collect.ObjectArrays;
import com.google.inject.Module;
import org.junit.Test;

public class FileConfigurationSourceTest
    extends AbstractApplicationConfigurationSourceTest

{
  /**
   * Note: adding the module here directly, as this UT cannot extend NexusAppTestSupport
   * as it's relies on some lazy operations done by configuration. If NexusAppTestSupport
   * extended, preparations done in these UTs would happen too late.
   */
  @Override
  protected Module[] getTestCustomModules() {
    Module[] modules = super.getTestCustomModules();
    if (modules == null) {
      modules = new Module[0];
    }
    modules = ObjectArrays.concat(modules, new SecurityModule());
    return modules;
  }

  @Override
  protected ApplicationConfigurationSource getConfigurationSource()
      throws Exception
  {
    return lookup(ApplicationConfigurationSource.class, "file");
  }

  @Override
  protected InputStream getOriginatingConfigurationInputStream()
      throws IOException
  {
    return getClass().getResourceAsStream("/META-INF/nexus/default-oss-nexus.xml");
  }

  @Test
  public void testStoreConfiguration()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    try {
      configurationSource.storeConfiguration();
    }
    catch (UnsupportedOperationException e) {
      fail();
    }
  }

  @Test
  public void testIsConfigurationUpgraded()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(false, configurationSource.isConfigurationUpgraded());
  }

  @Test
  public void testIsConfigurationDefaulted()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(true, configurationSource.isConfigurationDefaulted());
  }

  @Test
  public void testIsConfigurationDefaultedShouldNot()
      throws Exception
  {
    copyDefaultConfigToPlace();

    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(false, configurationSource.isConfigurationDefaulted());
  }

  @Test
  public void testGetDefaultsSource()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    assertFalse(configurationSource.getDefaultsSource() == null);
  }

  @Test
  public void testNEXUS2212LoadValidConfig()
      throws Exception
  {

    // copy the config into place
    File nexusConfigFile = FileUtils
        .getFileFromUrl(ClassLoader.getSystemClassLoader().getResource("nexus-NEXUS-2212.xml").toString());
    org.codehaus.plexus.util.FileUtils.copyFile(nexusConfigFile, new File(getWorkHomeDir(), "conf/nexus.xml"));

    configurationSource = (FileConfigurationSource) getConfigurationSource();
    configurationSource.loadConfiguration();
    assertTrue(configurationSource.getValidationResponse().isValid());

  }
}
