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
import org.sonatype.nexus.common.hash.MultiHashingInputStream;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuard;
import org.sonatype.nexus.common.stateguard.StateGuardAware;
import org.sonatype.nexus.common.stateguard.Transitions;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_OWNS_ASSET;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_OWNS_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_PART_OF_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CHECKSUM;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CONTENT_TYPE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;
import static org.sonatype.nexus.repository.storage.StorageFacet.V_ASSET;
import static org.sonatype.nexus.repository.storage.StorageFacet.V_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageTxImpl.State.CLOSED;
import static org.sonatype.nexus.repository.storage.StorageTxImpl.State.OPEN;

/**
 * Default {@link StorageTx} implementation.
 *
 * @since 3.0
 */
public class StorageTxImpl
    extends ComponentSupport
    implements StorageTx, StateGuardAware
{
  private final BlobTx blobTx;

  private final GraphTx graphTx;

  private final Object bucketId;

  private final StateGuard stateGuard = new StateGuard.Builder().initial(CLOSED).create();

  public StorageTxImpl(final BlobTx blobTx,
                       final GraphTx graphTx,
                       final Object bucketId)
  {
    this.blobTx = checkNotNull(blobTx);
    this.graphTx = checkNotNull(graphTx);
    this.bucketId = checkNotNull(bucketId);
  }

  public static final class State
  {
    public static final String OPEN = "OPEN";

    public static final String CLOSED = "CLOSED";
  }

  @Override
  public StateGuard getStateGuard() {
    return stateGuard;
  }

  @Override
  @Guarded(by = OPEN)
  public GraphTx getGraphTx() {
    return graphTx;
  }

  @Override
  @Guarded(by = OPEN)
  public void commit() {
    graphTx.commit();
    blobTx.commit();
  }

  @Override
  @Guarded(by = OPEN)
  public void rollback() {
    graphTx.rollback();
    blobTx.rollback();
  }

  @Override
  @Transitions(from = OPEN, to = CLOSED)
  public void close() {
    graphTx.close(); // rolls back and releases underlying ODatabaseDocumentTx to pool
    blobTx.rollback(); // no-op if no changes have occurred since last commit
  }

  @Override
  @Guarded(by = OPEN)
  public OrientVertex getBucket() {
    return findVertex(bucketId, null);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<OrientVertex> browseAssets(final Vertex bucket) {
    checkNotNull(bucket);

    return orientVertices(bucket.getVertices(Direction.OUT, E_OWNS_ASSET));
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<OrientVertex> browseComponents(final Vertex bucket) {
    checkNotNull(bucket);

    return orientVertices(bucket.getVertices(Direction.OUT, E_OWNS_COMPONENT));
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<OrientVertex> browseVertices(@Nullable final String className) {
    if (className == null) {
      return orientVertices(graphTx.getVertices());
    }
    else {
      return orientVertices(graphTx.getVerticesOfClass(className));
    }
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findAsset(final Object vertexId, final Vertex bucket) {
    checkNotNull(vertexId);
    checkNotNull(bucket);

    OrientVertex vertex = findVertex(vertexId, V_ASSET);
    return bucketOwns(bucket, E_OWNS_ASSET, vertex) ? vertex : null;
  }

  private boolean bucketOwns(Vertex bucket, String edgeLabel, @Nullable Vertex item) {
    if (item == null) {
      return false;
    }
    Vertex first = Iterables.getFirst(item.getVertices(Direction.IN, edgeLabel), null);
    return bucket.equals(first);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findAssetWithProperty(final String propName, final Object propValue,
                                            final Vertex bucket)
  {
    return findWithPropertyOwnedBy(V_ASSET, propName, propValue, E_OWNS_ASSET, bucket);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrientVertex> findAssets(final Vertex component) {
    List vertices = Lists.newArrayList(checkNotNull(component).getVertices(Direction.IN, E_PART_OF_COMPONENT));
    return (List<OrientVertex>) vertices;
  }

  @SuppressWarnings("unchecked")
  private OrientVertex findWithPropertyOwnedBy(String className, String propName, Object propValue,
                                               String edgeLabel, Vertex bucket)
  {
    checkNotNull(propName);
    checkNotNull(propValue);
    checkNotNull(bucket);

    Map<String, Object> parameters = ImmutableMap.of("propValue", propValue, "bucket", bucket);
    String query = String.format("select from %s where %s = :propValue and in('%s') contains :bucket",
        className, propName, edgeLabel);
    Iterable<OrientVertex> vertices = graphTx.command(new OCommandSQL(query)).execute(parameters);
    return Iterables.getFirst(vertices, null);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<OrientVertex> findAssets(@Nullable String whereClause,
                                           @Nullable Map<String, Object> parameters,
                                           @Nullable Iterable<Repository> repositories,
                                           @Nullable String querySuffix)
  {
    return findVertices(V_ASSET, whereClause, parameters, E_OWNS_ASSET, repositories, querySuffix);
  }

  @Override
  @Guarded(by = OPEN)
  public long countAssets(@Nullable String whereClause,
                          @Nullable Map<String, Object> parameters,
                          @Nullable Iterable<Repository> repositories,
                          @Nullable String querySuffix)
  {
    return countVertices(V_ASSET, whereClause, parameters, E_OWNS_ASSET, repositories, querySuffix);
  }

  private Iterable<OrientVertex> findVertices(String className,
                                              @Nullable String whereClause,
                                              @Nullable Map<String, Object> parameters,
                                              @Nullable String edgeLabel,
                                              @Nullable Iterable<Repository> repositories,
                                              @Nullable String querySuffix)
  {
    String query = buildQuery(className, false, whereClause, edgeLabel, repositories, querySuffix);
    log.debug("Finding vertices with query: {}, parameters: {}", query, parameters);
    return graphTx.command(new OCommandSQL(query)).execute(parameters);
  }

  private long countVertices(String className,
                             @Nullable String whereClause,
                             @Nullable Map<String, Object> parameters,
                             @Nullable String edgeLabel,
                             @Nullable Iterable<Repository> repositories,
                             @Nullable String querySuffix)
  {
    String query = buildQuery(className, true, whereClause, edgeLabel, repositories, querySuffix);
    log.debug("Counting vertices with query: {}, parameters: {}", query, parameters);
    List<ODocument> results = graphTx.getRawGraph().command(new OCommandSQL(query)).execute(parameters);
    return results.get(0).field("count");
  }

  private String buildQuery(String className,
                            boolean isCount,
                            @Nullable String whereClause,
                            @Nullable String edgeLabel,
                            @Nullable Iterable<Repository> repositories,
                            @Nullable String querySuffix)
  {
    StringBuilder query = new StringBuilder();
    query.append("select");
    if (isCount) {
      query.append(" count(*)");
    }
    query.append(" from ").append(className);
    if (whereClause != null) {
      query.append(" where ").append(whereClause);
    }

    if (repositories != null) {
      List<String> bucketConstraints = Lists.newArrayList(
          Iterables.transform(repositories, new Function<Repository, String>()
          {
            @Override
            public String apply(final Repository repository) {
              return String.format("%s = '%s'", P_REPOSITORY_NAME, repository.getName());
            }
          }).iterator());
      if (bucketConstraints.size() > 0) {
        checkArgument(edgeLabel != null);
        if (whereClause == null) {
          query.append(" where");
        }
        else {
          query.append(" and");
        }
        query.append(" in('").append(edgeLabel).append("') contains (");
        query.append(Joiner.on(" or ").join(bucketConstraints));
        query.append(")");
      }
    }

    if (querySuffix != null) {
      query.append(" ").append(querySuffix);
    }

    return query.toString();
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findComponent(final Object vertexId, final Vertex bucket) {
    checkNotNull(vertexId);
    checkNotNull(bucket);

    OrientVertex vertex = findVertex(vertexId, V_COMPONENT);
    return bucketOwns(bucket, E_OWNS_COMPONENT, vertex) ? vertex : null;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findComponentWithProperty(final String propName, final Object propValue, final Vertex bucket) {
    return findWithPropertyOwnedBy(V_COMPONENT, propName, propValue, E_OWNS_COMPONENT, bucket);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<OrientVertex> findComponents(@Nullable String whereClause,
                                               @Nullable Map<String, Object> parameters,
                                               @Nullable Iterable<Repository> repositories,
                                               @Nullable String querySuffix)
  {
    return findVertices(V_COMPONENT, whereClause, parameters, E_OWNS_COMPONENT, repositories, querySuffix);
  }

  @Override
  @Guarded(by = OPEN)
  public long countComponents(@Nullable String whereClause,
                              @Nullable Map<String, Object> parameters,
                              @Nullable Iterable<Repository> repositories,
                              @Nullable String querySuffix)
  {
    return countVertices(V_COMPONENT, whereClause, parameters, E_OWNS_COMPONENT, repositories, querySuffix);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findVertex(final Object vertexId, @Nullable final String className) {
    checkNotNull(vertexId);

    OrientVertex vertex = graphTx.getVertex(vertexId);
    if (vertex != null && className != null && !vertex.getProperty("@class").equals(className)) {
      return null;
    }
    return vertex;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public OrientVertex findVertexWithProperty(final String propName, final Object propValue,
                                             @Nullable final String className)
  {
    checkNotNull(propName);
    checkNotNull(propValue);

    Iterable<OrientVertex> vertices = orientVertices(graphTx.getVertices(propName, propValue));
    OrientVertex vertex = Iterables.getFirst(vertices, null);
    if (vertex != null && className != null && !vertex.getProperty("@class").equals(className)) {
      return null;
    }
    return vertex;
  }

  @Override
  @Guarded(by = OPEN)
  public OrientVertex createAsset(final Vertex bucket) {
    checkNotNull(bucket);

    OrientVertex asset = createVertex(V_ASSET);
    asset.setProperty(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    graphTx.addEdge(null, bucket, asset, E_OWNS_ASSET);
    return asset;
  }

  @Override
  @Guarded(by = OPEN)
  public OrientVertex createComponent(final Vertex bucket) {
    checkNotNull(bucket);

    OrientVertex component = createVertex(V_COMPONENT);
    component.setProperty(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    graphTx.addEdge(null, bucket, component, E_OWNS_COMPONENT);
    return component;
  }

  @Override
  @Guarded(by = OPEN)
  public OrientVertex createVertex(final String className) {
    checkNotNull(className);

    return graphTx.addVertex(className, (String) null);
  }

  @Override
  @Guarded(by = OPEN)
  public NestedAttributesMap getAttributes(final OrientVertex vertex) {
    checkNotNull(vertex);

    Map<String, Object> backing = vertex.getProperty(P_ATTRIBUTES);
    return new NestedAttributesMap("attributes", backing);
  }

  @Override
  @Guarded(by = OPEN)
  public void deleteVertex(final Vertex vertex) {
    checkNotNull(vertex);

    graphTx.removeVertex(vertex);
  }

  @Override
  @Guarded(by = OPEN)
  public BlobRef createBlob(final InputStream inputStream, Map<String, String> headers) {
    checkNotNull(inputStream);
    checkNotNull(headers);

    return blobTx.create(inputStream, headers);
  }

  @Override
  public BlobRef setBlob(final InputStream inputStream, final Map<String, String> headers, final OrientVertex asset,
                            final Iterable<HashAlgorithm> hashAlgorithms, final String contentType)
  {
    checkNotNull(inputStream);
    checkNotNull(headers);
    checkNotNull(asset);
    checkNotNull(hashAlgorithms);
    checkNotNull(contentType);

    // Delete old blob if necessary
    String oldBlobRefString = asset.getProperty(P_BLOB_REF);
    if (oldBlobRefString != null) {
      deleteBlob(BlobRef.parse(oldBlobRefString));
    }

    // Store new blob while calculating hashes in one pass
    final MultiHashingInputStream hashingStream = new MultiHashingInputStream(hashAlgorithms, inputStream);
    final BlobRef newBlobRef = createBlob(hashingStream, headers);

    asset.setProperty(P_BLOB_REF, newBlobRef.toString());
    asset.setProperty(P_SIZE, hashingStream.count());
    asset.setProperty(P_CONTENT_TYPE, contentType);

    // Set attributes map to contain computed checksum metadata
    Map<HashAlgorithm, HashCode> hashes = hashingStream.hashes();
    NestedAttributesMap checksums = getAttributes(asset).child(P_CHECKSUM);
    for (HashAlgorithm algorithm : hashAlgorithms) {
      checksums.set(algorithm.name(), hashes.get(algorithm).toString());
    }

    return newBlobRef;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Blob getBlob(final BlobRef blobRef) {
    checkNotNull(blobRef);

    return blobTx.get(blobRef);
  }

  @Override
  @Guarded(by = OPEN)
  public void deleteBlob(final BlobRef blobRef) {
    checkNotNull(blobRef);

    blobTx.delete(blobRef);
  }

  private static Iterable<OrientVertex> orientVertices(Iterable<Vertex> plainVertices) {
    return (Iterable<OrientVertex>) (Iterable) plainVertices;
  }
}
