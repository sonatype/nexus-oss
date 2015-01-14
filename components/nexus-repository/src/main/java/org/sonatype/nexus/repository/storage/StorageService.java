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

import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import com.tinkerpop.blueprints.Vertex;

/**
 * Storage service, providing component and asset storage for all repositories.
 *
 * @since 3.0
 */
public interface StorageService
  extends Lifecycle
{
  static String E_CONTAINS_COMPONENTS_WITH_LABEL = "contains_components_with_label";

  static String E_HAS_LABEL = "has_label";

  static String E_OWNS_ASSET = "owns_asset";

  static String E_OWNS_COMPONENT = "owns_component";

  static String E_PART_OF_COMPONENT = "part_of_component";

  static String P_BLOB_REF = "blob_ref";

  static String P_CONTENT_TYPE = "content_type";

  static String P_PATH = "path";

  static String P_REPOSITORY_NAME = "repository_name";

  static String V_ASSET = "asset";

  static String V_COMPONENT = "component";

  static String V_LABEL = "label";

  static String V_BUCKET = "bucket";

  GraphTx getGraphTx();

  Iterable<Vertex> browseAssetsOwnedBy(Vertex bucket);

  Iterable<Vertex> browseComponentsOwnedBy(Vertex bucket);

  Iterable<Vertex> browseVertices(GraphTx graph, @Nullable String className);

  @Nullable
  Vertex findAssetOwnedBy(GraphTx graph, Object vertexId, Vertex bucket);

  @Nullable
  Vertex findComponentOwnedBy(GraphTx graph, Object vertexId, Vertex bucket);

  @Nullable
  Vertex findVertex(GraphTx graph, Object vertexId, @Nullable String className);

  @Nullable
  Vertex findAssetWithPropertyOwnedBy(GraphTx graph, String propName, Object propValue, Vertex bucket);

  @Nullable
  Vertex findComponentWithPropertyOwnedBy(GraphTx graph, String propName, Object propValue, Vertex bucket);

  @Nullable
  Vertex findVertexWithProperty(GraphTx graph, String propName, Object propValue, @Nullable String className);

  Vertex createAssetOwnedBy(GraphTx graph, Vertex bucket);

  Vertex createComponentOwnedBy(GraphTx graph, Vertex bucket);

  Vertex createVertex(GraphTx graph, String className);

  void deleteVertex(GraphTx graph, Vertex vertex);

  BlobRef createBlob(InputStream inputStream, Map<String, String> headers);

  @Nullable
  org.sonatype.nexus.blobstore.api.Blob getBlob(BlobRef blobRef);

  boolean deleteBlob(BlobRef blobRef);
}
