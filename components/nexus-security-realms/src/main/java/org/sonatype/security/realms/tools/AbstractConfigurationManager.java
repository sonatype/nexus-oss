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

package org.sonatype.security.realms.tools;

import org.sonatype.security.model.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigurationManager
    implements ConfigurationManager
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected Logger getLogger() {
    return logger;
  }

  //

  private volatile EnhancedConfiguration configurationCache = null;

  public void clearCache() {
    configurationCache = null;
  }

  protected EnhancedConfiguration getConfiguration() {
    // Assign configuration to local variable first, as calls to clearCache can null it out at any time
    EnhancedConfiguration configuration = this.configurationCache;
    if (configuration == null || shouldRebuildConifuguration()) {
      synchronized (this) {
        // double-checked locking of volatile is apparently OK with java5+
        // http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
        configuration = this.configurationCache;
        if (configuration == null || shouldRebuildConifuguration()) {
          configuration = new EnhancedConfiguration(doGetConfiguration());
          this.configurationCache = configuration;
        }
      }
    }
    return configuration;
  }

  /**
   * Returns <code>true</code> if configuration needs to be rebuilt (by calling {@link #doGetConfiguration()}).
   */
  protected boolean shouldRebuildConifuguration() {
    return false;
  }

  /**
   * Builds and returns fresh new Configuration instance. Implementation is expected to reset
   * {@link #shouldRebuildConifuguration()} flag back to <code>false</code>.
   */
  protected abstract Configuration doGetConfiguration();
}
