/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.source.config;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.component.source.ComponentSourceRegistry;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * A persistent store of {@link ComponentSourceConfig} objects.
 *
 * @since 3.0
 */
public interface ComponentSourceConfigStore
    extends Lifecycle
{
  /**
   * Creates a new {@link ComponentSourceId} based on a user-provided name, which should be invoked to provide new
   * sources an {@link ComponentSourceConfig#getSourceId() id}.
   *
   * To find the id of a source currently in use, use {@link ComponentSourceRegistry#getSource(String)}.
   */
  public ComponentSourceId createId(String name);

  /**
   * Adds a config
   *
   * @param config to be added
   * @throws IOException IOException If any problem encountered while read/store of config storage
   */
  ComponentSourceConfigId add(ComponentSourceConfig config) throws IOException;

  /**
   * Updates stored config if exists.
   *
   * @param config to be updated
   * @throws IOException If any problem encountered while read/store of config storage
   */
  void update(ComponentSourceConfigId id, ComponentSourceConfig config) throws IOException;

  /**
   * Deletes stored config if exists.
   *
   * @param id of config to be deleted
   * @return false if config to be deleted does not exist in storage, true otherwise
   * @throws IOException If any problem encountered while read/store of config storage
   */
  void remove(ComponentSourceConfigId id) throws IOException;

  /**
   * Retrieves stored configs.
   *
   * @return configs (never null)
   * @throws IOException If any problem encountered while read/store of config storage
   */
  Map<ComponentSourceConfigId, ComponentSourceConfig> getAll() throws IOException;


  /**
   * Retrieves a config based on its name.
   *
   * @return null if there's no config by that name.
   */
  @Nullable
  ComponentSourceConfig get(String sourceName) throws IOException;
}
