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
package org.sonatype.nexus.componentviews.config;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * Persistant storage for view configurations. These will be modified by administrators. When Nexus starts, any
 * stored configurations will be instantiated as views.
 *
 * @since 3.0
 */
public interface ViewConfigStore
    extends Lifecycle
{
  /**
   * Adds a config
   *
   * @param item to be added
   * @throws IOException IOException If any problem encountered while read/store of config storage
   */
  ViewConfigId add(ViewConfig item) throws IOException;

  /**
   * Updates stored config if exists.
   *
   * @param item to be updated
   * @throws IOException If any problem encountered while read/store of config storage
   */
  void update(ViewConfigId id, ViewConfig item) throws IOException;

  /**
   * Deletes stored config if exists.
   *
   * @param id of config to be deleted
   * @return false if config to be deleted does not exist in storage, true otherwise
   * @throws IOException If any problem encountered while read/store of config storage
   */
  void remove(ViewConfigId id) throws IOException;

  /**
   * Retrieves stored configs.
   *
   * @return configs (never null)
   * @throws IOException If any problem encountered while read/store of config storage
   */
  Map<ViewConfigId, ViewConfig> getAll() throws IOException;


  /**
   * Retrieves a config based on its view name.
   *
   * @return null if there's no view by that name.
   */
  @Nullable
  ViewConfig get(String viewName) throws IOException;
}
