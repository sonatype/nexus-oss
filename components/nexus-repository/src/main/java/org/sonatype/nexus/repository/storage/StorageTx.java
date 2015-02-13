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
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

/**
 * A storage transaction.
 *
 * @since 3.0
 */
public interface StorageTx
    extends AutoCloseable
{
  /**
   * Provides the underlying graph transaction.
   *
   * Note: The caller may use this to directly query or manipulate the OrientDB graph, if needed, but should not
   * directly commit, rollback, or close the underlying transaction.
   */
  OrientGraph getGraphTx();

  /**
   * Commits the transaction.
   */
  void commit();

  /**
   * Rolls back the transaction.
   */
  void rollback();

  /**
   * Closes the transaction. Uncommitted changes will be lost, and the object will be ineligible for further use.
   */
  void close();

  /**
   * Gets the bucket for the current repository.
   */
  OrientVertex getBucket();

  /**
   * Gets all assets owned by the specified bucket.
   */
  Iterable<OrientVertex> browseAssets(Vertex bucket);

  /**
   * Gets all components owned by the specified bucket.
   */
  Iterable<OrientVertex> browseComponents(Vertex bucket);

  /**
   * Gets all vertices, optionally limited to those in the specified class.
   */
  Iterable<OrientVertex> browseVertices(@Nullable String className);

  /**
   * Gets an asset by id, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findAsset(Object vertexId, Vertex bucket);

  /**
   * Gets an asset by some identifying property, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findAssetWithProperty(String propName, Object propValue, Vertex bucket);


  /**
   * Returns all the assets associated with a component.
   */
  List<OrientVertex> findAssets(Vertex component);


  /**
   * Gets all assets in the specified repositories that match the given where clause.
   *
   * @param whereClause an OrientDB select query, minus the "select from X where " prefix. Rather than passing values
   *                    in directly, they should be specified as :labeled portions of the where clause (e.g. a = :aVal).
   * @param parameters the name-value pairs specifying the values for any :labeled portions of the where clause.
   * @param repositories the repositories to limit the results to. If null or empty, results won't be limited
   *                     by repository.
   * @param querySuffix the part of the query after the where clause, which may by used for ordering and paging
   *                    as per the OrientDB select query syntax.
   * @see <a href="https://github.com/orientechnologies/orientdb/wiki/SQL-Query">OrientDB SELECT Query Documentation</a>
   */
  Iterable<OrientVertex> findAssets(@Nullable String whereClause,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Iterable<Repository> repositories,
                                    @Nullable String querySuffix);

  /**
   * Gets the number of assets matching the given where clause.
   */
  long countAssets(@Nullable String whereClause,
                   @Nullable Map<String, Object> parameters,
                   @Nullable Iterable<Repository> repositories,
                   @Nullable String querySuffix);

  /**
   * Gets a component by id, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findComponent(Object vertexId, Vertex bucket);

  /**
   * Gets a component by some identifying property, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findComponentWithProperty(String propName, Object propValue, Vertex bucket);

  /**
   * Gets all component in the specified repositories that match the given where clause.
   *
   * @param whereClause an OrientDB query, minus the "select from X where " prefix. Rather than passing values
   *                    in directly, they should be specified as :labeled portions of the where clause (e.g. a = :aVal).
   * @param parameters the name-value pairs specifying the values for any :labeled portions of the where clause.
   * @param repositories the repositories to limit the results to. If null or empty, results won't be limited
   *                     by repository.
   * @param querySuffix the part of the query after the where clause, which may by used for ordering and paging
   *                    as per the OrientDB select query syntax.
   * @see <a href="https://github.com/orientechnologies/orientdb/wiki/SQL-Query">OrientDB SELECT Query Documentation</a>
   */
  Iterable<OrientVertex> findComponents(@Nullable String whereClause,
                                        @Nullable Map<String, Object> parameters,
                                        @Nullable Iterable<Repository> repositories,
                                        @Nullable String querySuffix);

  /**
   * Gets the number of components matching the given where clause.
   */
  long countComponents(@Nullable String whereClause,
                       @Nullable Map<String, Object> parameters,
                       @Nullable Iterable<Repository> repositories,
                       @Nullable String querySuffix);

  /**
   * Gets a vertex by id, optionally limited by class, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findVertex(Object vertexId, @Nullable String className);

  /**
   * Gets a vertex by some identifying property, optionally limited by class, or {@code null} if not found.
   */
  @Nullable
  OrientVertex findVertexWithProperty(String propName, Object propValue, @Nullable String className);

  /**
   * Creates a new asset owned by the specified bucket.
   */
  OrientVertex createAsset(Vertex bucket);

  /**
   * Creates a new component owned by the specified bucket.
   */
  OrientVertex createComponent(Vertex bucket);

  /**
   * Creates a new vertex of the specified class.
   */
  OrientVertex createVertex(String className);

  /**
   * Gets the "attributes" map of an asset or component vertex.
   *
   * Changes to the map will be persisted when the transaction is committed.
   */
  NestedAttributesMap getAttributes(OrientVertex vertex);

  /**
   * Deletes an existing vertex.
   */
  void deleteVertex(Vertex vertex);

  /**
   * Creates a new Blob.
   */
  BlobRef createBlob(InputStream inputStream, Map<String, String> headers);

  /**
   * Creates a new Blob and updates the given asset with a reference to it, hash metadata, size, and content type.
   * The old blob, if any, will be deleted.
   */
  BlobRef setBlob(InputStream inputStream, Map<String, String> headers, OrientVertex asset,
                     Iterable<HashAlgorithm> hashAlgorithms, String contentType);

  /**
   * Gets a Blob, or {@code null if not found}.
   */
  @Nullable
  Blob getBlob(BlobRef blobRef);

  /**
   * Deletes a Blob.
   */
  void deleteBlob(BlobRef blobRef);
}
