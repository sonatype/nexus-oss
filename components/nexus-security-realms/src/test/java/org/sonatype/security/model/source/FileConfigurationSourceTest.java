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

package org.sonatype.security.model.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileConfigurationSourceTest
    extends AbstractSecurityConfigurationSourceTest

{
  protected SecurityModelConfigurationSource getConfigurationSource()
      throws Exception
  {
    FileModelConfigurationSource source =
        (FileModelConfigurationSource) lookup(SecurityModelConfigurationSource.class, "file");

    source.setConfigurationFile(new File(getSecurityConfiguration()));

    return source;
  }

  protected InputStream getOriginatingConfigurationInputStream()
      throws IOException
  {
    return getClass().getResourceAsStream("/META-INF/security/security.xml");
  }

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

  public void testIsConfigurationUpgraded()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(false, configurationSource.isConfigurationUpgraded());
  }

  public void testIsConfigurationDefaulted()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(true, configurationSource.isConfigurationDefaulted());
  }

  public void testIsConfigurationDefaultedShouldNot()
      throws Exception
  {
    copyDefaultSecurityConfigToPlace();

    configurationSource = getConfigurationSource();

    configurationSource.loadConfiguration();

    assertEquals(false, configurationSource.isConfigurationDefaulted());
  }

  // NOT EXPOSED
  // public void testGetDefaultsSource()
  // throws Exception
  // {
  // configurationSource = getConfigurationSource();
  //
  // assertFalse( configurationSource.getDefaultsSource() == null );
  // }
}
