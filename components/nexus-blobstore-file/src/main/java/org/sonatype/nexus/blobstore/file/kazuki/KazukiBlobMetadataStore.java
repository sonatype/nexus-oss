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
package org.sonatype.nexus.blobstore.file.kazuki;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobStoreException;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.nexus.blobstore.file.MetadataMetrics;
import org.sonatype.nexus.blobstore.file.guice.FileBlobStoreModule;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.Key;
import io.kazuki.v0.store.guice.KazukiModule;
import io.kazuki.v0.store.index.SecondaryIndexStore;
import io.kazuki.v0.store.index.query.QueryBuilder;
import io.kazuki.v0.store.index.query.QueryOperator;
import io.kazuki.v0.store.index.query.QueryTerm;
import io.kazuki.v0.store.index.query.ValueType;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValueIterator;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.keyvalue.KeyValueStoreIteration.SortDirection;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.schema.TypeValidation;
import io.kazuki.v0.store.schema.model.Attribute.Type;
import io.kazuki.v0.store.schema.model.AttributeTransform;
import io.kazuki.v0.store.schema.model.IndexAttribute;
import io.kazuki.v0.store.schema.model.Schema;
import io.kazuki.v0.store.sequence.KeyImpl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A Kazuki-backed implementation of the {@link BlobMetadataStore}.
 *
 * @since 3.0
 */
public class KazukiBlobMetadataStore
    extends LifecycleSupport
    implements BlobMetadataStore
{
  /**
   * A secondary index to facilitate searching by blob state.
   */
  private static final String STATE_INDEX = "stateIndex";

  public static final String METADATA_TYPE = "blobdata";


  public static final String DYNAMIC_KZ_NAME = "kazuki-dynamic-name";

  private final Lifecycle lifecycle;

  private final KeyValueStore kvStore;

  private final SchemaStore schemaStore;

  private final SecondaryIndexStore secondaryIndexStore;

  /**
   * Kazuki's {@link KazukiModule} creates this class's dependencies under a specific, dynamic name. The
   * {@code @Named(DYNAMIC_KZ_NAME)} annotations on the constructor allow the {@link FileBlobStoreModule} to
   * associate this private {@link KazukiBlobMetadataStore} instance with the correct set of kazuki components.
   */
  @Inject
  public KazukiBlobMetadataStore(@Named(DYNAMIC_KZ_NAME) Lifecycle lifecycle,
                                 @Named(DYNAMIC_KZ_NAME) final KeyValueStore kvStore,
                                 @Named(DYNAMIC_KZ_NAME) final SchemaStore schemaStore,
                                 @Named(DYNAMIC_KZ_NAME) final SecondaryIndexStore secondaryIndexStore)
  {
    this.lifecycle = checkNotNull(lifecycle, "lifecycle");
    this.kvStore = checkNotNull(kvStore, "key value store");
    this.schemaStore = checkNotNull(schemaStore, "schema store");
    this.secondaryIndexStore = checkNotNull(secondaryIndexStore, "secondary index store");
  }

  @Override
  protected void doStart() throws Exception {
    lifecycle.init();
    lifecycle.start();

    if (schemaStore.retrieveSchema(METADATA_TYPE) == null) {
      Schema schema = new Schema.Builder()
          .addAttribute("blobState", Type.ENUM,
              Arrays.asList((Object) BlobState.CREATING, BlobState.ALIVE, BlobState.MARKED_FOR_DELETION), false)

          .addAttribute("headers", Type.MAP, false)
          .addAttribute("creationTime", Type.UTC_DATE_SECS, true)
          .addAttribute("sha1Hash", Type.UTF8_SMALLSTRING, true)
          .addAttribute("contentSize", Type.I64, false)

          .addIndex(STATE_INDEX,
              asList(new IndexAttribute("blobState", SortDirection.ASCENDING, AttributeTransform.NONE)), false)
          .build();

      log.info("Creating schema for file blob metadata");

      schemaStore.createSchema(METADATA_TYPE, schema);
    }
  }

  @Override
  protected void doStop() throws Exception {
    lifecycle.stop();
    lifecycle.shutdown();
  }

  // TODO: All these methods are synchronized due to https://github.com/kazukidb/kazuki/issues/17
  @Override
  public synchronized BlobId add(final BlobMetadata metadata) {
    final FlatBlobMetadata flat = flatten(metadata);
    Key key = null;
    try {
      key = kvStore.create(METADATA_TYPE, FlatBlobMetadata.class, flat, TypeValidation.STRICT).getKey();
      log.debug("Adding metadata for blob {}", asBlobId(key));
      return asBlobId(key);
    }
    catch (KazukiException e) {
      throw new BlobStoreException(e, "unknown", asBlobId(key));
    }
  }

  @Nullable
  @Override
  public synchronized BlobMetadata get(final BlobId blobId) {
    try {
      final Key key = asKey(checkNotNull(blobId));
      final FlatBlobMetadata metadata = findMetadata(key);
      if (metadata == null) {
        return null;
      }
      return expand(metadata);
    }
    catch (KazukiException e) {
      throw new BlobStoreException(e, "unknown", blobId);
    }
  }

  @Override
  public synchronized void update(final BlobId blobId, final BlobMetadata metadata) {
    Key key = asKey(checkNotNull(blobId));
    try {
      final FlatBlobMetadata flat = flatten(metadata);
      kvStore.update(key, FlatBlobMetadata.class, flat);
    }
    catch (KazukiException e) {
      throw new BlobStoreException(e, "unknown", blobId);
    }
  }

  @Override
  public synchronized void delete(final BlobId blobId) {
    checkNotNull(blobId);
    try {
      kvStore.delete(asKey(blobId));
    }
    catch (KazukiException e) {
      throw new BlobStoreException(e, "unknown", blobId);
    }
  }

  @Override
  public synchronized Iterator<BlobId> findWithState(BlobState blobState) {
    final List<QueryTerm> queryTerms = new QueryBuilder()
        .andMatchesSingle("blobState", QueryOperator.EQ, ValueType.STRING, blobState.toString())
        .build();
    final KeyValueIterable<Key> keys = secondaryIndexStore
        .queryWithoutPagination(METADATA_TYPE, FlatBlobMetadata.class, STATE_INDEX, queryTerms, SortDirection.ASCENDING,
            null, null);

    final KeyValueIterator<Key> keyIterator = keys.iterator();

    return new Iterator<BlobId>()
    {
      @Override
      public boolean hasNext() {
        return keyIterator.hasNext();
      }

      @Override
      public BlobId next() {
        final Key key = keyIterator.next();
        if (key == null) {
          return null;
        }
        return asBlobId(key);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Provides metrics about the blobs in the blob store. The {@link MetadataMetrics#getBlobCount() blob count} only
   * considers blobs that are currently in {@link BlobState#ALIVE}, but {@link MetadataMetrics#getTotalSize() total
   * size}
   * includes blobs that are marked for deletion.
   */
  @Override
  public synchronized MetadataMetrics getMetadataMetrics() {

    // TODO: Replace this brute force approach with a counter
    // TODO: Replace the statistics object with kazuki's eventual support for counters

    final KeyValueIterable<FlatBlobMetadata> values = kvStore.iterators()
        .values(METADATA_TYPE, FlatBlobMetadata.class, SortDirection.ASCENDING);
    long totalSize = 0;
    long blobCount = 0;
    for (FlatBlobMetadata metadata : values) {
      if (metadata == null) {
        // Concurrent modification can cause objects in an iterator to return null.
        continue;
      }

      if (BlobState.ALIVE.equals(metadata.getBlobState())) {
        blobCount++;
      }

      totalSize += metadata.getContentSize();
    }

    return new MetadataMetrics(blobCount, totalSize);
  }

  private FlatBlobMetadata findMetadata(Key key) throws KazukiException {
    checkNotNull(key);
    return kvStore.retrieve(key, FlatBlobMetadata.class);
  }

  private FlatBlobMetadata flatten(final BlobMetadata metadata) {
    final FlatBlobMetadata flat = new FlatBlobMetadata();

    flat.setBlobState(metadata.getBlobState());
    final BlobMetrics metrics = metadata.getMetrics();
    if (metrics != null) {
      flat.setSha1Hash(metrics.getSHA1Hash());
      flat.setContentSize(metrics.getContentSize());
      flat.setCreationTime(metrics.getCreationTime());
    }
    flat.setHeaders(metadata.getHeaders());
    return flat;
  }

  private BlobMetadata expand(final FlatBlobMetadata flat) {
    final BlobMetadata metadata = new BlobMetadata(flat.getBlobState(), flat.getHeaders());
    metadata.setMetrics(new BlobMetrics(flat.getCreationTime(), flat.getSha1Hash(), flat.getContentSize()));
    return metadata;
  }

  private BlobId asBlobId(Key key) {
    return new BlobId(key.toString());
  }

  private Key asKey(BlobId blobId) {
    // TODO: This isn't strictly legal; I need a way to synthesize kz keys from strings
    // See https://github.com/kazukidb/kazuki/issues/14
    return KeyImpl.valueOf(blobId.getId());
  }
}
