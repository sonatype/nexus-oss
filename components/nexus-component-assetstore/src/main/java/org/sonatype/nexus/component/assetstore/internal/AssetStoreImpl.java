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
package org.sonatype.nexus.component.assetstore.internal;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.recordstore.FieldDefinition;
import org.sonatype.nexus.component.recordstore.Record;
import org.sonatype.nexus.component.recordstore.RecordId;
import org.sonatype.nexus.component.recordstore.RecordQuery;
import org.sonatype.nexus.component.recordstore.RecordStore;
import org.sonatype.nexus.component.recordstore.RecordStoreSchema;
import org.sonatype.nexus.component.recordstore.RecordStoreSession;
import org.sonatype.nexus.component.recordstore.RecordType;
import org.sonatype.nexus.component.assetstore.AssetStore;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link AssetStore} implementation.
 */
public class AssetStoreImpl
    implements AssetStore
{
  protected static final String ASSET_FIELD = "asset";
  protected static final String BLOB_ID_FIELD = "blobId";
  protected static final String COMPONENT_FIELD = "component";
  protected static final String CONTENT_TYPE_FIELD = "contentType";
  protected static final String FIRST_CREATED_FIELD = "firstCreated";
  protected static final String ID_FIELD = "id";
  protected static final String NODE_ID_FIELD = "nodeId";
  protected static final String PATH_FIELD = "path";

  /**
   * A BlobRef record, which describes an asset's content as stored on a single node.
   */
  protected static final RecordType BLOB_REF_RECORD = new RecordType("BlobRef")
      .withField(new FieldDefinition(ASSET_FIELD, RecordId.class).withIndexed(true).withNotNull(true))
      .withField(new FieldDefinition(NODE_ID_FIELD, String.class).withNotNull(true))
      .withField(new FieldDefinition(BLOB_ID_FIELD, String.class).withNotNull(true));

  /**
   * A Component record, which has a canonical id.
   *
   * NOTE: This will move to component-services when ready, and this impl will
   * use that to look up component Records given ComponentIds.
   */
  protected static final RecordType COMPONENT_RECORD = new RecordType("Component")
      .withField(new FieldDefinition(ID_FIELD, String.class).withIndexed(true).withUnique(true).withNotNull(true));

  /**
   * An Asset record, which describes a single file (with a path) associated with a component.
   */
  protected static final RecordType ASSET_RECORD = new RecordType("Asset")
      .withField(new FieldDefinition(ID_FIELD, String.class).withIndexed(true).withUnique(true).withNotNull(true))
      .withField(new FieldDefinition(COMPONENT_FIELD, RecordId.class).withIndexed(true).withNotNull(true))
      .withField(new FieldDefinition(PATH_FIELD, String.class).withNotNull(true))
      .withField(new FieldDefinition(FIRST_CREATED_FIELD, Date.class).withNotNull(true))
      .withField(new FieldDefinition(CONTENT_TYPE_FIELD, String.class));

  private final RecordStore recordStore;

  private final BlobStore blobStore;

  @Inject
  public AssetStoreImpl(RecordStore recordStore, BlobStore blobStore) {
    this.recordStore = checkNotNull(recordStore);
    this.blobStore = checkNotNull(blobStore);

    // create classes in orient if needed
    try (RecordStoreSession session = recordStore.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      schema.addType(COMPONENT_RECORD); // TODO: Also move this to component-services when ready
      schema.addType(ASSET_RECORD);
      schema.addType(BLOB_REF_RECORD);
    }
  }

  @Override
  public Asset create(final ComponentId componentId, final String path, final InputStream stream, @Nullable final String contentType) {
    checkNotNull(componentId);
    checkNotNull(path);
    checkNotNull(stream);

    // store stream as blob on this node
    Blob blob = blobStore.create(stream, ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, path,
        BlobStore.CREATED_BY_HEADER, "TODO:Determine how to get this and how it's useful for disaster recovery"));

    // persist the Asset record and an associated BlobRef record
    try (RecordStoreSession session = recordStore.openSession()) {
      Record componentRecord = getExistingComponentRecord(session, componentId);

      // create new asset record
      Record assetRecord = session.create(ASSET_RECORD);
      assetRecord.set(ID_FIELD, assetId(componentId, path));
      assetRecord.set(PATH_FIELD, path);
      assetRecord.set(COMPONENT_FIELD, componentRecord.getId());
      assetRecord.set(FIRST_CREATED_FIELD, blob.getMetrics().getCreationTime().toDate());
      if (contentType != null) {
        assetRecord.set(CONTENT_TYPE_FIELD, contentType);
      }
      try {
        assetRecord.save();
      }
      catch (ORecordDuplicatedException e) {
        throw new IllegalStateException(String.format(
            "An asset already exists with path: %s for component: %s", path, componentId.asUniqueString()));
      }

      // create new BlobRef record describing the copy at this node
      try {
        createAndSaveLocalBlobRefRecord(session, assetRecord, blob);
      }
      catch (Throwable t) {
        // back out the asset record before bubbling up
        try {
          session.delete(assetRecord);
        }
        catch (Throwable t2) {
          // TODO: log a warning that the asset record could not be cleaned up
        }
      }
    }
    catch (Throwable t) {
      // TODO: handle unique constraint violation exception from orient, and throw as ISE..still need to cleanup
      // clean up stored blob if record storage fails for any reason
      blobStore.delete(blob.getId());
      Throwables.propagate(t);
    }

    // TODO: publish a message that an asset has just been added

    return new BlobAsset(componentId, blob, path, contentType, blob.getMetrics().getCreationTime());
  }

  private Record createAndSaveLocalBlobRefRecord(RecordStoreSession session, Record assetRecord, Blob blob) {
    Record localBlobRefRecord = session.create(BLOB_REF_RECORD);
    localBlobRefRecord.set(ASSET_FIELD, assetRecord.getId());
    localBlobRefRecord.set(NODE_ID_FIELD, localNodeId());
    localBlobRefRecord.set(BLOB_ID_FIELD, blob.getId().asUniqueString());
    localBlobRefRecord.save();
    return localBlobRefRecord;
  }

  @Override
  public Asset update(final ComponentId componentId, final String path, final InputStream stream,
                      @Nullable final String contentType) {
    checkNotNull(componentId);
    checkNotNull(path);
    checkNotNull(stream);

    // store stream as blob on this node
    Blob blob = blobStore.create(stream, ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, assetId(componentId, path),
        BlobStore.CREATED_BY_HEADER, "TODO:Determine how to get this and how it's useful for disaster recovery"));

    Map<String, BlobId> oldBlobIds = Maps.newHashMap();

    // STAGE 1: Update records in a transaction
    try (RecordStoreSession session = recordStore.openSession()) {
      // TODO: begin transaction

      // retrieve update the existing asset record
      Record assetRecord = getExistingAssetRecord(session, componentId, path);
      if (contentType == null) {
        if (assetRecord.has(CONTENT_TYPE_FIELD)) {
          assetRecord.remove(CONTENT_TYPE_FIELD);
        }
      }
      else {
        assetRecord.set(CONTENT_TYPE_FIELD, contentType);
      }
      assetRecord.save();

      // delete all existing associated blob records, holding on to their blob ids for later deletion
      deleteBlobRefRecords(session, assetRecord, oldBlobIds);

      // create new blob record describing the copy at this node
      createAndSaveLocalBlobRefRecord(session, assetRecord, blob);

      // TODO: commit transaction
    }

    // STAGE 2: Delete old blobs
    deleteOldBlobs(oldBlobIds);

    // TODO: publish a message that an asset has just been updated

    return new BlobAsset(componentId, blob, path, contentType, blob.getMetrics().getCreationTime());
  }

  @Nullable
  @Override
  public Asset get(final ComponentId componentId, final String path) {
    checkNotNull(componentId);
    checkNotNull(path);

    try (RecordStoreSession session = recordStore.openSession()) {
      Record assetRecord = getRecordWithId(session, ASSET_RECORD, assetId(componentId, path));
      if (assetRecord == null) {
        return null;
      }
      return getAssetFromRecord(session, assetRecord, componentId);
    }
  }

  @Override
  public Map<String, Asset> getAll(final ComponentId componentId) {
    checkNotNull(componentId);

    try (RecordStoreSession session = recordStore.openSession()) {
      Record componentRecord = getExistingComponentRecord(session, componentId);

      RecordQuery query = new RecordQuery(ASSET_RECORD).withEqual(COMPONENT_FIELD, componentRecord.getId());
      List<Record> result = session.find(query);

      Map<String, Asset> assets = Maps.newHashMap();
      for (Record assetRecord : result) {
        assets.put((String) assetRecord.get(PATH_FIELD), getAssetFromRecord(session, assetRecord, componentId));
      }

      return assets;
    }
  }

  @Override
  public boolean delete(final ComponentId componentId, final String path) {
    checkNotNull(componentId);
    checkNotNull(path);

    Map<String, BlobId> oldBlobIds = Maps.newHashMap();

    // STAGE 1: Delete records in a transaction
    try (RecordStoreSession session = recordStore.openSession()) {
      // TODO: begin transaction

      Record assetRecord = getRecordWithId(session, ASSET_RECORD, assetId(componentId, path));
      if (assetRecord == null) {
        return false;
      }

      // delete all existing associated blob records, holding on to their blob ids for later deletion
      deleteBlobRefRecords(session, assetRecord, oldBlobIds);

      // delete the asset record
      session.delete(assetRecord);

      // TODO: commit transaction
    }

    // STAGE 2: Delete old blobs
    return deleteOldBlobs(oldBlobIds);

    // TODO: publish a message that an asset has just been deleted
  }

  private boolean deleteOldBlobs(Map<String, BlobId> oldBlobIds) {
    boolean deletedLocalBlob = false;
    for (String nodeId : oldBlobIds.keySet()) {
      BlobId blobId = oldBlobIds.get(nodeId);
      try {
        if (nodeId.equals(localNodeId())) {
          deletedLocalBlob = blobStore.delete(blobId);
        }
        else {
          deleteRemoteBlob(nodeId, blobId);
        }
      }
      catch (Throwable t) {
        // TODO: log a warning that this blob couldn't be deleted, but we're gonna keep on truckin'
      }
    }
    return deletedLocalBlob;
  }

  private void deleteRemoteBlob(String nodeId, BlobId blobId) {
    // TODO: Signal to remote node that the blob with the given id should be deleted
  }

  private void deleteBlobRefRecords(RecordStoreSession session, Record assetRecord, Map<String, BlobId> oldBlobIds) {
    RecordQuery query = new RecordQuery(BLOB_REF_RECORD).withEqual(ASSET_FIELD, assetRecord.getId());
    for (Record blobRefRecord : session.find(query)) {
      String nodeId = blobRefRecord.get(NODE_ID_FIELD);
      BlobId blobId = new BlobId((String) blobRefRecord.get(BLOB_ID_FIELD));
      session.delete(blobRefRecord);
      oldBlobIds.put(nodeId, blobId);
    }
  }

  private Asset getAssetFromRecord(RecordStoreSession session, Record assetRecord, ComponentId componentId) {
    // get the blob
    BlobId blobId = new BlobId((String) getExistingLocalBlobRefRecord(session, assetRecord).get(BLOB_ID_FIELD));
    Blob blob = blobStore.get(blobId);
    if (blob == null) {
      throw new IllegalStateException(String.format("Blob record exists, but not found in blob store: %s", blobId));
    }

    // return a blob-based Asset instance
    String path = assetRecord.get(PATH_FIELD);
    String contentType = assetRecord.get(CONTENT_TYPE_FIELD);
    Date firstCreated = assetRecord.get(FIRST_CREATED_FIELD);
    return new BlobAsset(componentId, blob, path, contentType, new DateTime(firstCreated));
  }

  private Record getExistingLocalBlobRefRecord(RecordStoreSession session, Record assetRecord) {
    RecordQuery query = new RecordQuery(BLOB_REF_RECORD)
        .withEqual(ASSET_FIELD, assetRecord.getId()).withEqual(NODE_ID_FIELD, localNodeId());
    List<Record> result = session.find(query);
    checkState(!result.isEmpty(), "Local blobRef does not exist yet for asset: %s", assetRecord.get(ID_FIELD));
    return result.get(0);
  }

  private String localNodeId() {
    // TODO: Find some way to uniquely and persistently identify nodes in a cluster..
    return "local";
  }

  protected static Record getExistingAssetRecord(RecordStoreSession session, ComponentId componentId, String path) {
    Record record = getRecordWithId(session, ASSET_RECORD, assetId(componentId, path));
    checkState(record != null, "Asset does not exist in storage yet: %s", assetId(componentId, path));
    return record;
  }

  protected static Record getExistingComponentRecord(RecordStoreSession session, ComponentId componentId) {
    Record record = getRecordWithId(session, COMPONENT_RECORD, componentId.asUniqueString());
    checkState(record != null, "Component does not exist in storage yet: %s", componentId.asUniqueString());
    return record;
  }

  protected static Record getRecordWithId(RecordStoreSession session, RecordType recordType, String id) {
    RecordQuery query = new RecordQuery(recordType).withEqual(ID_FIELD, id);
    List<Record> result = session.find(query);
    if (result.isEmpty()) {
      return null;
    }
    else {
      return result.get(0);
    }
  }

  private static String assetId(ComponentId componentId, String path) {
    return String.format("%s:%s", componentId.asUniqueString(), path);
  }
}
