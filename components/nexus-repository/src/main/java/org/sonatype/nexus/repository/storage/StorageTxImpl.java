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
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.hash.MultiHashingInputStream;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuard;
import org.sonatype.nexus.common.stateguard.StateGuardAware;
import org.sonatype.nexus.common.stateguard.Transitions;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.IllegalOperationException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.*;
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
  private static final long DELETE_BATCH_SIZE = 100L;

  private final BlobTx blobTx;

  private final GraphTx graphTx;

  private final ORID bucketId;

  private final WritePolicy writePolicy;

  private final StateGuard stateGuard = new StateGuard.Builder().initial(CLOSED).create();

  public StorageTxImpl(final BlobTx blobTx,
                       final GraphTx graphTx,
                       final ORID bucketId,
                       final WritePolicy writePolicy)
  {
    this.blobTx = checkNotNull(blobTx);
    this.graphTx = checkNotNull(graphTx);
    this.bucketId = checkNotNull(bucketId);
    this.writePolicy = writePolicy;
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
  public Bucket getBucket() {
    return new BucketImpl(findVertex(bucketId, null));
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Bucket> browseBuckets() {
    return Iterables.transform(
        graphTx.getVerticesOfClass(V_BUCKET),
        new Function<Vertex, Bucket>()
        {
          @Override
          public Bucket apply(final Vertex vertex) {
            return new BucketImpl((OrientVertex) vertex);
          }
        });
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> browseAssets(final Bucket bucket) {
    checkNotNull(bucket);

    return Iterables.transform(
        bucket.vertex().getVertices(Direction.OUT, E_OWNS_ASSET),
        new Function<Vertex, Asset>()
        {
          @Override
          public Asset apply(final Vertex vertex) {
            return new AssetImpl((OrientVertex) vertex);
          }
        });
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> browseComponents(final Bucket bucket) {
    checkNotNull(bucket);

    return Iterables.transform(
        bucket.vertex().getVertices(Direction.OUT, E_OWNS_COMPONENT),
        new Function<Vertex, Component>()
        {
          @Override
          public Component apply(final Vertex vertex) {
            return new ComponentImpl((OrientVertex) vertex);
          }
        });
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAsset(final ORID id, final Bucket bucket) {
    checkNotNull(id);
    checkNotNull(bucket);

    OrientVertex vertex = findVertex(id, V_ASSET);
    return bucketOwns(bucket, E_OWNS_ASSET, vertex) ? new AssetImpl(vertex) : null;
  }

  private boolean bucketOwns(Bucket bucket, String edgeLabel, @Nullable Vertex item) {
    if (item == null) {
      return false;
    }
    Vertex first = Iterables.getFirst(item.getVertices(Direction.IN, edgeLabel), null);
    return bucket.vertex().equals(first);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAssetWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    OrientVertex vertex = findWithPropertyOwnedBy(V_ASSET, propName, propValue, E_OWNS_ASSET, bucket);
    if (vertex == null) {
      return null;
    }
    return new AssetImpl(vertex);
  }

  @SuppressWarnings("unchecked")
  private OrientVertex findWithPropertyOwnedBy(String className, String propName, Object propValue,
                                               String edgeLabel, Bucket bucket)
  {
    checkNotNull(propName);
    checkNotNull(propValue);
    checkNotNull(bucket);

    Map<String, Object> parameters = ImmutableMap.of("propValue", propValue, "bucket", bucket.vertex());
    String query = String.format("select from %s where %s = :propValue and in('%s') contains :bucket",
        className, propName, edgeLabel);
    Iterable<OrientVertex> vertices = graphTx.command(new OCommandSQL(query)).execute(parameters);
    return Iterables.getFirst(vertices, null);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> findAssets(@Nullable String whereClause,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Iterable<Repository> repositories,
                                    @Nullable String querySuffix)
  {
    return Iterables.transform(
        findVertices(V_ASSET, whereClause, parameters, E_OWNS_ASSET, repositories, querySuffix),
        new Function<OrientVertex, Asset>()
        {
          @Override
          public Asset apply(final OrientVertex vertex) {
            return new AssetImpl(vertex);
          }
        });
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
  public Component findComponent(final ORID id, final Bucket bucket) {
    checkNotNull(id);
    checkNotNull(bucket);

    OrientVertex vertex = findVertex(id, V_COMPONENT);
    return bucketOwns(bucket, E_OWNS_COMPONENT, vertex) ? new ComponentImpl(vertex) : null;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Component findComponentWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    OrientVertex vertex = findWithPropertyOwnedBy(V_COMPONENT, propName, propValue, E_OWNS_COMPONENT, bucket);
    if (vertex == null) {
      return null;
    }
    return new ComponentImpl(vertex);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> findComponents(@Nullable String whereClause,
                                            @Nullable Map<String, Object> parameters,
                                            @Nullable Iterable<Repository> repositories,
                                            @Nullable String querySuffix)
  {
    return Iterables.transform(
        findVertices(V_COMPONENT, whereClause, parameters, E_OWNS_COMPONENT, repositories, querySuffix),
        new Function<OrientVertex, Component>()
        {
          @Override
          public Component apply(final OrientVertex vertex) {
            return new ComponentImpl(vertex);
          }
        });
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
  private OrientVertex findVertex(final ORID id, @Nullable final String className) {
    checkNotNull(id);

    OrientVertex vertex = graphTx.getVertex(id);
    if (vertex != null && className != null && !vertex.getProperty("@class").equals(className)) {
      return null;
    }
    return vertex;
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Format format) {
    checkNotNull(bucket);
    checkNotNull(format);

    return createAsset(bucket, format.getValue());
  }

  private Asset createAsset(final Bucket bucket, final String format) {
    OrientVertex vertex = createVertex(V_ASSET);
    vertex.setProperty(P_FORMAT, format);
    vertex.setProperty(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    graphTx.addEdge(null, bucket.vertex(), vertex, E_OWNS_ASSET);
    return new AssetImpl(vertex);
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Component component) {
    Asset asset = createAsset(bucket, component.format());
    graphTx.addEdge(null, asset.vertex(), component.vertex(), E_PART_OF_COMPONENT);
    return asset;
  }

  @Override
  @Guarded(by = OPEN)
  public Component createComponent(final Bucket bucket, final Format format) {
    checkNotNull(bucket);
    checkNotNull(format);

    OrientVertex vertex = createVertex(V_COMPONENT);
    vertex.setProperty(P_FORMAT, format.getValue());
    vertex.setProperty(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    graphTx.addEdge(null, bucket.vertex(), vertex, E_OWNS_COMPONENT);
    return new ComponentImpl(vertex);
  }

  private OrientVertex createVertex(final String className) {
    return graphTx.addVertex(className, (String) null);
  }

  private void deleteVertex(final Vertex vertex) {
    graphTx.removeVertex(vertex);
  }

  @Override
  @Guarded(by = OPEN)
  public void deleteComponent(Component component) {
    deleteComponent(component, true);
  }

  public void deleteComponent(final Component component, final boolean checkWritePolicy) {
    checkNotNull(component);

    for (Asset asset : component.assets()) {
      deleteAsset(asset, checkWritePolicy);
    }
    deleteVertex(component.vertex());
  }

  @Override
  @Guarded(by = OPEN)
  public void deleteAsset(Asset asset) {
    deleteAsset(asset, true);
  }

  private void deleteAsset(final Asset asset, final boolean checkWritePolicy) {
    checkNotNull(asset);

    BlobRef blobRef = asset.blobRef();
    if (blobRef != null) {
      deleteBlob(blobRef, checkWritePolicy);
    }
    deleteVertex(asset.vertex());
  }

  @Override
  public void deleteBucket(Bucket bucket) {
    checkNotNull(bucket);

    long count = 0;

    // first delete all components and constituent assets
    for (Component component : browseComponents(bucket)) {
      deleteComponent(component, false);
      count++;
      if (count == DELETE_BATCH_SIZE) {
        commit();
        count = 0;
      }
    }
    commit();

    // then delete all standalone assets
    for (Asset asset : browseAssets(bucket)) {
      deleteAsset(asset, false);
      count++;
      if (count == DELETE_BATCH_SIZE) {
        commit();
        count = 0;
      }
    }
    commit();

    // finally, delete the bucket vertex
    deleteVertex(bucket.vertex());
    commit();
  }

  @Override
  @Guarded(by = OPEN)
  public BlobRef createBlob(final InputStream inputStream, Map<String, String> headers) {
    checkNotNull(inputStream);
    checkNotNull(headers);

    return blobTx.create(inputStream, headers);
  }

  @Override
  public BlobRef setBlob(final InputStream inputStream, final Map<String, String> headers, final Asset asset,
                         final Iterable<HashAlgorithm> hashAlgorithms, final String contentType)
  {
    checkNotNull(inputStream);
    checkNotNull(headers);
    checkNotNull(asset);
    checkNotNull(hashAlgorithms);
    checkNotNull(contentType);

    if (writePolicy == WritePolicy.DENY) {
      throw new IllegalOperationException("Repository is read only.");
    }

    // Delete old blob if necessary
    BlobRef oldBlobRef = asset.blobRef();
    if (oldBlobRef != null) {
      if (writePolicy == WritePolicy.ALLOW_ONCE) {
        throw new IllegalOperationException("Repository does not allow updating assets.");
      }
      deleteBlob(oldBlobRef, true);
    }

    // Store new blob while calculating hashes in one pass
    final MultiHashingInputStream hashingStream = new MultiHashingInputStream(hashAlgorithms, inputStream);
    final BlobRef newBlobRef = createBlob(hashingStream, headers);

    asset.blobRef(newBlobRef);
    asset.size(hashingStream.count());
    asset.contentType(contentType);

    // Set attributes map to contain computed checksum metadata
    Map<HashAlgorithm, HashCode> hashes = hashingStream.hashes();
    NestedAttributesMap checksums = asset.attributes().child(P_CHECKSUM);
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
  public Blob requireBlob(final BlobRef blobRef) {
    Blob blob = getBlob(blobRef);
    checkState(blob != null, "Blob not found: %s", blobRef);
    return blob;
  }

  private void deleteBlob(final BlobRef blobRef, boolean checkWritePolicy) {
    checkNotNull(blobRef);
    if (checkWritePolicy && writePolicy == WritePolicy.DENY) {
      throw new IllegalOperationException("Repository is read only.");
    }
    blobTx.delete(blobRef);
  }
}
