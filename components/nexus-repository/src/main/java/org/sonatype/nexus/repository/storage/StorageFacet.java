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
package org.sonatype.nexus.repository.storage;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * Storage {@link Facet}, providing component and asset storage for a repository.
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
  OrientGraph getGraphTx();

  /**
   * Gets all assets owned by the repository.
   */
  Iterable<Vertex> browseAssets(OrientGraph graph);

  /**
   * Gets all components owned by the repository.
   */
  Iterable<Vertex> browseComponents(OrientGraph graph);

  /**
   * Gets an asset by id, or {@code null} if not found.
   */
  @Nullable
  Vertex findAsset(OrientGraph graph, Object vertexId);

  /**
   * Gets an asset by some other identifying property, or {@code null} if not found.
   */
  @Nullable
  Vertex findAssetWithProperty(OrientGraph graph, String propName, Object propValue);

  /**
   * Gets a component by id, or {@code null} if not found.
   */
  @Nullable
  Vertex findComponent(OrientGraph graph, Object vertexId);

  /**
   * Gets a component by some other identifying property, or {@code null} if not found.
   */
  @Nullable
  Vertex findComponentWithProperty(OrientGraph graph, String propName, Object propValue);

  /**
   * Creates a new asset.
   */
  Vertex createAsset(OrientGraph graph);

  /**
   * Creates a new component.
   */
  Vertex createComponent(OrientGraph graph);

  /**
   * Deletes an existing vertex.
   */
  void deleteVertex(OrientGraph graph, Vertex vertex);

  /**
   * Creates a new Blob.
   */
  BlobRef createBlob(InputStream inputStream, Map<String, String> headers);

  /**
   * Gets a Blob.
   */
  @Nullable
  org.sonatype.nexus.blobstore.api.Blob getBlob(BlobRef blobRef);

  /**
   * Deletes a Blob.
   */
  boolean deleteBlob(BlobRef blobRef);
}
