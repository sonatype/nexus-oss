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

import java.io.IOException;
import java.net.URL;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.model.Configuration;

/**
 * A special "static" configuration source, that always return a factory provided defaults for Nexus configuration. It
 * is unmodifiable, since it actually reads the bundled config file from the module's JAR.
 *
 * @author cstamas
 */
@Singleton
@Named("static")
public class StaticConfigurationSource
    extends AbstractApplicationConfigurationSource
{
  @Override
  public Configuration loadConfiguration() throws IOException {
    URL url = getClass().getResource("/META-INF/nexus/nexus.xml");
    if (url == null) {
      log.info("No edition-specific configuration found, falling back to Core default configuration.");
      url = getClass().getResource("/META-INF/nexus/default-oss-nexus.xml");
    }
    loadConfiguration(url);

    return getConfiguration();
  }

  /**
   * The 'static' source is immutable.
   */
  @Override
  public void storeConfiguration() throws IOException {
    throw new UnsupportedOperationException();
  }
}
