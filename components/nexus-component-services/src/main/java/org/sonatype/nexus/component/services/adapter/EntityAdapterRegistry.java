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
package org.sonatype.nexus.component.services.adapter;

import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Entity;

/**
 * A registry of {@link EntityAdapter}s, keyed by {@link Entity} domain class.
 *
 * @since 3.0
 */
public interface EntityAdapterRegistry
{
  /**
   * Registers an asset adapter and creates the OrientDB class in the database if it doesn't already exist.
   *
   * @throws IllegalStateException if an adapter already exists in the registry with the same entity class.
   */
  <T extends Asset> void registerAssetAdapter(AssetAdapter<T> adapter);

  /**
   * Registers a component adapter and creates the OrientDB class in the database if it doesn't already exist.
   *
   * @throws IllegalStateException if an adapter already exists in the registry with the same entity class.
   */
  <T extends Component> void registerComponentAdapter(ComponentAdapter<T> adapter);

  /**
   * Unregisters the adapter for the given asset class. If no such adapter is registered, this is a no-op.
   */
  <T extends Asset> void unregisterAssetAdapter(Class<T> entityClass);

  /**
   * Unregisters the adapter for the given component class. If no such adapter is registered, this is a no-op.
   */
  <T extends Component> void unregisterComponentAdapter(Class<T> entityClass);

  /**
   * Gets the registered adapter for the given asset class, or {@code null} if it doesn't exist.
   */
  @Nullable
  <T extends Asset> AssetAdapter<T> getAssetAdapter(Class<T> entityClass);

  /**
   * Gets the registered adapter for the given component class, or {@code null} if it doesn't exist.
   */
  @Nullable
  <T extends Component> ComponentAdapter<T> getComponentAdapter(Class<T> entityClass);

  /**
   * Gets all asset classes for which a registered adapter exists.
   */
  Set<Class<? extends Asset>> assetClasses();

  /**
   * Gets all component classes for which a registered adapter exists.
   */
  Set<Class<? extends Component>> componentClasses();
}
