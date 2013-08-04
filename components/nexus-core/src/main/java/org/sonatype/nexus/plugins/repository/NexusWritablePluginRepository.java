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

import java.io.IOException;
import java.net.URL;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * Writable {@link NexusPluginRepository} that supports installation and deletion of Nexus plugins.
 */
public interface NexusWritablePluginRepository
    extends NexusPluginRepository
{
  /**
   * Downloads and installs the given Nexus plugin into the writable repository.
   *
   * @param bundle The plugin resource bundle
   * @return {@code true} if the plugin installed successfully; otherwise {@code false}
   */
  boolean installPluginBundle(URL bundle)
      throws IOException;

  /**
   * Deletes the given Nexus plugin from the writable repository.
   *
   * @param gav The plugin coordinates
   * @return {@code true} if the plugin was successfully deleted; otherwise {@code false}
   */
  boolean deletePluginBundle(GAVCoordinate gav)
      throws IOException;
}
