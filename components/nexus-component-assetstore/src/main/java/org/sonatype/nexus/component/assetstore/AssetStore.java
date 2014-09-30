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
package org.sonatype.nexus.component.assetstore;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;

/**
 * Provides read/write access to component "assets", which are the content streams that belong to components.
 *
 * @see <a href="https://docs.sonatype.com/display/Nexus/Nexus+3+Component+Manager+Architecture#Nexus3ComponentManagerArchitecture-ComponentStorage">Nexus 3 Component Storage</a>
 * @since 3.0
 */
public interface AssetStore
{

  /**
   * Stores a new asset for an existing component.
   *
   * @return the stored asset.
   * @throws IllegalStateException if the component doesn't exist or the asset already exists.
   */
  Asset create(ComponentId componentId, String path, InputStream stream, @Nullable String contentType);

  /**
   * Updates an existing asset for an existing component.
   *
   * @return the stored asset.
   * @throws IllegalStateException if the component or asset doesn't exist.
   */
  Asset update(ComponentId componentId, String path, InputStream stream, @Nullable String contentType);

  /**
   * Gets a stored asset, or {@code null} if the component or asset doesn't exist.
   */
  @Nullable
  Asset get(ComponentId componentId, String path);

  /**
   * Gets all stored assets that belong a component, keyed by path.
   *
   * @throws IllegalStateException if the component does not exist.
   */
  Map<String, Asset> getAll(ComponentId component);

  /**
   * Deletes a stored asset.
   *
   * @return {@code true} if the asset previously existed, {@code false} otherwise.
   */
  boolean delete(ComponentId componentId, String path);
}
