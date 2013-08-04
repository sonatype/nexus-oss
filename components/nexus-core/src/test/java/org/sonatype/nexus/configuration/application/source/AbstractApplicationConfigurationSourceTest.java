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

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.test.NexusTestSupport;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;

public abstract class AbstractApplicationConfigurationSourceTest
    extends NexusTestSupport
{
  protected ApplicationConfigurationSource configurationSource;

  protected abstract ApplicationConfigurationSource getConfigurationSource()
      throws Exception;

  protected abstract InputStream getOriginatingConfigurationInputStream()
      throws IOException;

  @Test
  public void testConfigStream()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    // not using load here since File config would load it and store it
    // thus changing it (but no content change!)
    copyDefaultConfigToPlace();

    InputStream configStream = null;

    InputStream originalStream = null;

    try {
      configStream = configurationSource.getConfigurationAsStream();

      originalStream = getOriginatingConfigurationInputStream();

      assertTrue(IOUtil.contentEquals(configStream, originalStream));
    }
    finally {
      if (configStream != null) {
        configStream.close();
      }

      if (originalStream != null) {
        originalStream.close();
      }
    }
  }

  @Test
  public void testGetConfiguration()
      throws Exception
  {
    configurationSource = getConfigurationSource();

    assertTrue(configurationSource.getConfiguration() == null);

    configurationSource.loadConfiguration();

    assertFalse(configurationSource.getConfiguration() == null);
  }

}
