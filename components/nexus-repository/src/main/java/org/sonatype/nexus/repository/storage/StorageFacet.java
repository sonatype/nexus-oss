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
 * Storage {@link Facet}.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface StorageFacet
  extends Facet
{
  static String E_CONTAINS_COMPONENTS_WITH_LABEL = "contains_components_with_label";

  static String E_HAS_LABEL = "has_label";

  static String E_OWNS_ASSET = "owns_asset";

  static String E_OWNS_COMPONENT = "owns_component";

  static String E_PART_OF_COMPONENT = "part_of_component";

  static String P_BLOB_REF = "blob_ref";

  static String P_CONTENT_TYPE = "content_type";

  static String P_PATH = "path";

  static String V_ASSET = "asset";

  static String V_COMPONENT = "component";

  static String V_LABEL = "label";

  static String V_BUCKET = "bucket";

  /**
   * Gets a transaction for working with the graph.
   */
  OrientGraph getGraphTx();

  // Browse Vertices

  /**
   * Gets all asset vertices.
   */
  Iterable<Vertex> browseAssets(OrientGraph graph);

  /**
   * Gets all bucket vertices.
   */
  Iterable<Vertex> browseBuckets(OrientGraph graph);

  /**
   * Gets all component vertices.
   */
  Iterable<Vertex> browseComponents(OrientGraph graph);

  /**
   * Gets all label vertices.
   */
  Iterable<Vertex> browseLabels(OrientGraph graph);

  // Read Vertices

  /**
   * Gets a vertex with a given id, or {@code null}.
   *
   * The id may be a {@code String}, {@code ODocument}, {@code OIdentifiable}, or {@code EntityId}.
   */
  @Nullable
  Vertex findById(OrientGraph graph, Object id);

  /**
   * Gets a vertex representing an asset with a given path, or {@code null}.
   */
  @Nullable
  Vertex findByPath(OrientGraph graph, String path);

  // Edit Vertices (edits can be done directly on Vertex objects, followed by calling graph.commit())

  // Add Vertices

  /**
   * Creates a new asset vertex.
   */
  Vertex createAsset(OrientGraph graph);

  /**
   * Creates a new bucket vertex.
   */
  Vertex createBucket(OrientGraph graph);

  /**
   * Creates a new component vertex.
   */
  Vertex createComponent(OrientGraph graph);

  /**
   * Creates a new label vertex.
   */
  Vertex createLabel(OrientGraph graph);

  // Delete Assets and Components

  /**
   * Removes a vertex from the graph.
   */
  void delete(OrientGraph graph, Vertex vertex);

  // Create, Read, and Update Blobs

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
