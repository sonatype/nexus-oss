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

package org.sonatype.nexus.plugins.repository;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * {@link File} backed {@link NexusPluginRepository} that supplies system plugins.
 */
@Named(SystemNexusPluginRepository.ID)
@Singleton
final class SystemNexusPluginRepository
    extends AbstractFileNexusPluginRepository
{
  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  static final String ID = "system";

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  @Inject
  @Named("${nexus-app}/plugin-repository")
  private File systemPluginsFolder;

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public String getId() {
    return ID;
  }

  public int getPriority() {
    return 50;
  }

  // ----------------------------------------------------------------------
  // Customized methods
  // ----------------------------------------------------------------------

  @Override
  protected File getNexusPluginsDirectory() {
    if (!systemPluginsFolder.exists()) {
      systemPluginsFolder.mkdirs();
    }
    return systemPluginsFolder;
  }
}
