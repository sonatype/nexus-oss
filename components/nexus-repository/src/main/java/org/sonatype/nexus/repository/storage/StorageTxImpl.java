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

import javax.annotation.Nonnull;
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
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BUCKET;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CHECKSUM;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.V_ASSET;
import static org.sonatype.nexus.repository.storage.StorageFacet.V_BUCKET;
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
  private static final long DELETE_BATCH_SIZE = 100L;

  private final BlobTx blobTx;

  private final ODatabaseDocumentTx db;

  private final ORID bucketId;

  private final WritePolicy writePolicy;

  private final StateGuard stateGuard = new StateGuard.Builder().initial(CLOSED).create();

  public StorageTxImpl(final BlobTx blobTx,
                       final ODatabaseDocumentTx db,
                       final ORID bucketId,
                       final WritePolicy writePolicy)
  {
    this.blobTx = checkNotNull(blobTx);
    this.db = checkNotNull(db);
    this.bucketId = checkNotNull(bucketId);
    this.writePolicy = writePolicy;
  }

  public static final class State
  {
    public static final String OPEN = "OPEN";

    public static final String CLOSED = "CLOSED";
  }

  @Override
  @Nonnull
  public StateGuard getStateGuard() {
    return stateGuard;
  }

  @Override
  @Guarded(by = OPEN)
  public ODatabaseDocumentTx getDb() {
    return db;
  }

  @Override
  @Guarded(by = OPEN)
  public void commit() {
    db.commit();
    blobTx.commit();
  }

  @Override
  @Guarded(by = OPEN)
  public void rollback() {
    db.rollback();
    blobTx.rollback();
  }

  @Override
  @Transitions(from = OPEN, to = CLOSED)
  public void close() {
    db.close(); // rolls back and releases ODatabaseDocumentTx to pool
    blobTx.rollback(); // no-op if no changes have occurred since last commit
  }

  @Override
  @Guarded(by = OPEN)
  public Bucket getBucket() {
    return new BucketImpl(findDocument(bucketId, null));
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Bucket> browseBuckets() {
    return Iterables.transform(
        db.browseClass(V_BUCKET),
        new Function<ODocument, Bucket>()
        {
          @Override
          public Bucket apply(final ODocument doc) {
            return new BucketImpl(doc);
          }
        });
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> browseAssets(final Bucket bucket) {
    checkNotNull(bucket);

    return Iterables.transform(
        browseDocuments(V_ASSET, bucket),
        new Function<ODocument, Asset>()
        {
          @Override
          public Asset apply(final ODocument doc) {
            return new AssetImpl(doc);
          }
        });
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> browseComponents(final Bucket bucket) {
    checkNotNull(bucket);

    return Iterables.transform(
        browseDocuments(V_COMPONENT, bucket),
        new Function<ODocument, Component>()
        {
          @Override
          public Component apply(final ODocument doc) {
            return new ComponentImpl(doc);
          }
        });
  }

  private Iterable<ODocument> browseDocuments(final String className, final Bucket bucket) {
    checkNotNull(bucket);

    Map<String, Object> parameters = ImmutableMap.<String, Object>of("bucket", bucket.document());
    String query = String.format("select from %s where bucket = :bucket", className);
    return db.command(new OCommandSQL(query)).execute(parameters);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAsset(final ORID id, final Bucket bucket) {
    checkNotNull(id);
    checkNotNull(bucket);
    ODocument doc = findDocument(id, V_ASSET);
    return bucketOwns(bucket, doc) ? new AssetImpl(doc) : null;
  }

  private boolean bucketOwns(Bucket bucket, @Nullable ODocument item) {
    return item != null && bucket.document().equals(item.field(P_BUCKET, ODocument.class));
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAssetWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    ODocument doc = findWithPropertyOwnedBy(V_ASSET, propName, propValue, bucket);
    if (doc == null) {
      return null;
    }
    return new AssetImpl(doc);
  }

  @SuppressWarnings("unchecked")
  private ODocument findWithPropertyOwnedBy(String className, String propName, Object propValue,
                                            Bucket bucket)
  {
    checkNotNull(propName);
    checkNotNull(propValue);
    checkNotNull(bucket);

    Map<String, Object> parameters = ImmutableMap.of("propValue", propValue, "bucket", bucket.document());
    String query = String.format("select from %s where %s = :propValue and bucket = :bucket", className, propName);
    Iterable<ODocument> docs = db.command(new OCommandSQL(query)).execute(parameters);
    return Iterables.getFirst(docs, null);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> findAssets(@Nullable String whereClause,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Iterable<Repository> repositories,
                                    @Nullable String querySuffix)
  {
    return Iterables.transform(
        findDocuments(V_ASSET, whereClause, parameters, repositories, querySuffix),
        new Function<ODocument, Asset>()
        {
          @Override
          public Asset apply(final ODocument doc) {
            return new AssetImpl(doc);
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
    return countDocuments(V_ASSET, whereClause, parameters, repositories, querySuffix);
  }

  private Iterable<ODocument> findDocuments(String className,
                                            @Nullable String whereClause,
                                            @Nullable Map<String, Object> parameters,
                                            @Nullable Iterable<Repository> repositories,
                                            @Nullable String querySuffix)
  {
    String query = buildQuery(className, false, whereClause, repositories, querySuffix);
    log.debug("Finding documents with query: {}, parameters: {}", query, parameters);
    return db.command(new OCommandSQL(query)).execute(parameters);
  }

  private long countDocuments(String className,
                              @Nullable String whereClause,
                              @Nullable Map<String, Object> parameters,
                              @Nullable Iterable<Repository> repositories,
                              @Nullable String querySuffix)
  {
    String query = buildQuery(className, true, whereClause, repositories, querySuffix);
    log.debug("Counting documents with query: {}, parameters: {}", query, parameters);
    List<ODocument> results = db.command(new OCommandSQL(query)).execute(parameters);
    return results.get(0).field("count");
  }

  private String buildQuery(String className,
                            boolean isCount,
                            @Nullable String whereClause,
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
              return String.format("%s.%s = '%s'", P_BUCKET, P_REPOSITORY_NAME, repository.getName());
            }
          }).iterator());
      if (bucketConstraints.size() > 0) {
        if (whereClause == null) {
          query.append(" where");
        }
        else {
          query.append(" and");
        }
        query.append(" (");
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
    ODocument doc = findDocument(id, V_COMPONENT);
    return bucketOwns(bucket, doc) ? new ComponentImpl(doc) : null;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Component findComponentWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    ODocument doc = findWithPropertyOwnedBy(V_COMPONENT, propName, propValue, bucket);
    if (doc == null) {
      return null;
    }
    return new ComponentImpl(doc);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> findComponents(@Nullable String whereClause,
                                            @Nullable Map<String, Object> parameters,
                                            @Nullable Iterable<Repository> repositories,
                                            @Nullable String querySuffix)
  {
    return Iterables.transform(
        findDocuments(V_COMPONENT, whereClause, parameters, repositories, querySuffix),
        new Function<ODocument, Component>()
        {
          @Override
          public Component apply(final ODocument doc) {
            return new ComponentImpl(doc);
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
    return countDocuments(V_COMPONENT, whereClause, parameters, repositories, querySuffix);
  }

  @Nullable
  private ODocument findDocument(final ORID id, @Nullable final String className) {
    checkNotNull(id);

    ODocument doc = db.getRecord(id);
    if (doc != null && className != null && !doc.field("@class").equals(className)) {
      return null;
    }
    return doc;
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Format format) {
    checkNotNull(bucket);
    checkNotNull(format);

    return createAsset(bucket, format.getValue());
  }

  private Asset createAsset(final Bucket bucket, final String format) {
    ODocument assetDoc = db.newInstance(V_ASSET);
    assetDoc.field(P_BUCKET, bucket.document());
    assetDoc.field(P_FORMAT, format);
    assetDoc.field(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    return new AssetImpl(assetDoc);
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Component component) {
    Asset asset = createAsset(bucket, component.format());
    asset.document().field(P_COMPONENT, component.document());
    return asset;
  }

  @Override
  @Guarded(by = OPEN)
  public Component createComponent(final Bucket bucket, final Format format) {
    checkNotNull(bucket);
    checkNotNull(format);

    ODocument doc = db.newInstance(V_COMPONENT);
    doc.field(P_BUCKET, bucket.document());
    doc.field(P_FORMAT, format.getValue());
    doc.field(P_ATTRIBUTES, ImmutableMap.of(), OType.EMBEDDEDMAP);
    return new ComponentImpl(doc);
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
    db.delete(component.document());
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
    db.delete(asset.document());
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

    // finally, delete the bucket document
    db.delete(bucket.document());
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

    asset.save();

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
