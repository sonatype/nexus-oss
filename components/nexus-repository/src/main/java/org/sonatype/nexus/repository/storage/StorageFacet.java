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
package org.sonatype.nexus.repository.storage;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Storage {@link Facet}.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface StorageFacet
  extends Facet
{
  /**
   * Gets a transaction for working with the graph.
   */
  Graph getGraphTx();

  // Browse Assets and Components

  /**
   * Gets all vertices representing assets in the graph.
   */
  Iterable<Vertex> browseAssets(Graph graph);

  /**
   * Gets all vertices representing components in the graph.
   */
  Iterable<Vertex> browseComponents(Graph graph);

  // Read Assets and Components

  /**
   * Gets a vertex representing an asset with a given id, or {@code null}.
   *
   * The id may be a {@code String}, {@code ODocument}, {@code OIdentifiable}, or {@code EntityId}.
   */
  @Nullable
  Vertex findAssetWithId(Graph graph, Object id);

  /**
   * Gets a vertex representing an asset with a given path, or {@code null}.
   */
  @Nullable
  Vertex findAssetWithPath(Graph graph, String path);

  /**
   * Gets a vertex representing an asset with a given id, or {@code null}.
   *
   * The id may be a {@code String}, {@code ODocument}, {@code OIdentifiable}, or {@code EntityId}.
   */
  @Nullable
  Vertex findComponentWithId(Graph graph, Object id);

  // Edit Assets and Components (edits can be done directly on Vertex objects, followed by calling graph.commit())

  // Add Assets and Components

  /**
   * Creates a new vertex representing an asset.
   */
  Vertex createAsset(Graph graph);

  /**
   * Creates a new vertex representing a component.
   */
  Vertex createComponent(Graph graph);

  // Delete Assets and Components

  /**
   * Removes a vertex from the graph.
   */
  void delete(Vertex vertex);

  // TODO: Blob CR_D
}
