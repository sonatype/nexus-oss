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

package org.sonatype.nexus.configuration;

import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A component responsible for "apply" (config -> repo) and "prepare" (repo -> config) steps for all those config
 * elements that does not map directly to a model and some extra processing is needed.
 *
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface Configurator
{
  /**
   * Will apply the configuration parameters from coreConfiguratuin to the target.
   */
  void applyConfiguration(Object target, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration)
      throws ConfigurationException;

  /**
   * Will prepare model for save, by syncing it with target state (if needed).
   */
  void prepareForSave(Object target, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration);
}
