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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.entity.EntityId;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CHECKSUM;
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

  private final Bucket bucket;

  private final WritePolicy writePolicy;

  private final StateGuard stateGuard = new StateGuard.Builder().initial(CLOSED).create();

  private final BucketEntityAdapter bucketEntityAdapter;

  private final ComponentEntityAdapter componentEntityAdapter;

  private final AssetEntityAdapter assetEntityAdapter;

  public StorageTxImpl(final BlobTx blobTx,
                       final ODatabaseDocumentTx db,
                       final Bucket bucket,
                       final WritePolicy writePolicy,
                       final BucketEntityAdapter bucketEntityAdapter,
                       final ComponentEntityAdapter componentEntityAdapter,
                       final AssetEntityAdapter assetEntityAdapter)
  {
    this.blobTx = checkNotNull(blobTx);
    this.db = checkNotNull(db);
    this.bucket = checkNotNull(bucket);
    this.writePolicy = writePolicy;
    this.bucketEntityAdapter = checkNotNull(bucketEntityAdapter);
    this.componentEntityAdapter = checkNotNull(componentEntityAdapter);
    this.assetEntityAdapter = checkNotNull(assetEntityAdapter);
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
    return bucket;
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Bucket> browseBuckets() {
    return bucketEntityAdapter.browse(db);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> browseAssets(final Bucket bucket) {
    return assetEntityAdapter.browseByBucket(db, bucket);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> browseAssets(final Component component) {
    return assetEntityAdapter.browseByComponent(db, component);
  }

  @Override
  public Asset firstAsset(final Component component) {
    return Iterables.getFirst(browseAssets(component), null);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> browseComponents(final Bucket bucket) {
    return componentEntityAdapter.browseByBucket(db, bucket);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAsset(final EntityId id, final Bucket bucket) {
    checkNotNull(id);
    checkNotNull(bucket);
    Asset asset = assetEntityAdapter.get(db, id);
    return bucketOwns(bucket, asset) ? asset : null;
  }

  private boolean bucketOwns(final Bucket bucket, final @Nullable MetadataNode item) {
    return item != null && Objects.equals(bucket.getEntityMetadata().getId(), item.bucketId());
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Asset findAssetWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    return assetEntityAdapter.findByProperty(db, propName, propValue, bucket);
  }


  @Override
  @Guarded(by = OPEN)
  public Iterable<Asset> findAssets(@Nullable String whereClause,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Iterable<Repository> repositories,
                                    @Nullable String querySuffix)
  {
    return assetEntityAdapter.browseByQuery(db, whereClause, parameters, repositories, querySuffix);
  }

  @Override
  @Guarded(by = OPEN)
  public long countAssets(@Nullable String whereClause,
                          @Nullable Map<String, Object> parameters,
                          @Nullable Iterable<Repository> repositories,
                          @Nullable String querySuffix)
  {
    return assetEntityAdapter.countByQuery(db, whereClause, parameters, repositories, querySuffix);
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Component findComponent(final EntityId id, final Bucket bucket) {
    checkNotNull(id);
    checkNotNull(bucket);
    Component component = componentEntityAdapter.get(db, id);
    return bucketOwns(bucket, component) ? component : null;
  }

  @Nullable
  @Override
  @Guarded(by = OPEN)
  public Component findComponentWithProperty(final String propName, final Object propValue, final Bucket bucket) {
    return componentEntityAdapter.findByProperty(db, propName, propValue, bucket);
  }

  @Override
  @Guarded(by = OPEN)
  public Iterable<Component> findComponents(@Nullable String whereClause,
                                            @Nullable Map<String, Object> parameters,
                                            @Nullable Iterable<Repository> repositories,
                                            @Nullable String querySuffix)
  {
    return componentEntityAdapter.browseByQuery(db, whereClause, parameters, repositories, querySuffix);
  }

  @Override
  @Guarded(by = OPEN)
  public long countComponents(@Nullable String whereClause,
                              @Nullable Map<String, Object> parameters,
                              @Nullable Iterable<Repository> repositories,
                              @Nullable String querySuffix)
  {
    return componentEntityAdapter.countByQuery(db, whereClause, parameters, repositories, querySuffix);
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Format format) {
    checkNotNull(format);
    return createAsset(bucket, format.toString());
  }

  private Asset createAsset(final Bucket bucket, final String format) {
    checkNotNull(bucket);
    Asset asset = new Asset();
    asset.bucketId(bucket.getEntityMetadata().getId());
    asset.format(format);
    asset.attributes(new NestedAttributesMap(P_ATTRIBUTES, new HashMap<String, Object>()));
    return asset;
  }

  @Override
  @Guarded(by = OPEN)
  public Asset createAsset(final Bucket bucket, final Component component) {
    checkNotNull(component);
    Asset asset = createAsset(bucket, component.format());
    asset.componentId(component.getEntityMetadata().getId());
    return asset;
  }

  @Override
  @Guarded(by = OPEN)
  public Component createComponent(final Bucket bucket, final Format format) {
    checkNotNull(bucket);
    checkNotNull(format);

    Component component = new Component();
    component.bucketId(bucket.getEntityMetadata().getId());
    component.format(format.toString());
    component.attributes(new NestedAttributesMap(P_ATTRIBUTES, new HashMap<String, Object>()));
    return component;
  }

  @Override
  public void saveComponent(final Component component) {
    if (component.isNew()) {
      componentEntityAdapter.add(db, component);
    }
    else {
      componentEntityAdapter.edit(db, component);
    }
  }

  @Override
  public void saveAsset(final Asset asset) {
    if (asset.isNew()) {
      assetEntityAdapter.add(db, asset);
    }
    else {
      assetEntityAdapter.edit(db, asset);
    }
  }

  @Override
  @Guarded(by = OPEN)
  public void deleteComponent(Component component) {
    deleteComponent(component, true);
  }

  public void deleteComponent(final Component component, final boolean checkWritePolicy) {
    checkNotNull(component);

    for (Asset asset : browseAssets(component)) {
      deleteAsset(asset, checkWritePolicy);
    }
    componentEntityAdapter.delete(db, component);
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
    assetEntityAdapter.delete(db, asset);
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
    bucketEntityAdapter.delete(db, bucket);
    commit();
  }

  @Override
  @Guarded(by = OPEN)
  public BlobRef createBlob(final InputStream inputStream, Map<String, String> headers) {
    checkNotNull(inputStream);
    checkNotNull(headers);

    ImmutableMap.Builder<String, String> storageHeaders = ImmutableMap.builder();
    storageHeaders.put(Bucket.REPO_NAME_HEADER, bucket.repositoryName());
    storageHeaders.putAll(headers);

    return blobTx.create(inputStream, storageHeaders.build());
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
