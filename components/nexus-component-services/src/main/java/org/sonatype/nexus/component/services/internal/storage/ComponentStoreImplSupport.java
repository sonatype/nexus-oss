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
package org.sonatype.nexus.component.services.internal.storage;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.component.services.id.EntityIdFactory;
import org.sonatype.nexus.component.services.internal.query.OrientQueryBuilder;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_BLOB_REFS;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_COMPONENT;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_LENGTH;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_FIRST_CREATED;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_LAST_MODIFIED;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;

/**
 * Supporting base class for {@link ComponentStoreImpl}.
 *
 * @since 3.0
 */
abstract class ComponentStoreImplSupport
    extends EntityAdapterSupport
    implements EventSubscriber
{
  protected final EntityAdapterRegistry entityAdapterRegistry;

  // TODO: Determine how to get this when BlobStoreManager (or whatever we call it) is implemented
  protected static final String localBlobStoreId = "someBlobStoreId"; // hardcoded for now..

  protected final Provider<DatabaseInstance> databaseInstanceProvider;

  // TODO: Delegate to something else instead of using a BlobStore directly here...
  protected final BlobStore blobStore;

  protected final EntityIdFactory entityIdFactory;

  public ComponentStoreImplSupport(Provider<DatabaseInstance> databaseInstanceProvider,
                                   BlobStore blobStore,
                                   EntityAdapterRegistry entityAdapterRegistry,
                                   EntityIdFactory entityIdFactory) {
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.blobStore = checkNotNull(blobStore);
    this.entityAdapterRegistry = checkNotNull(entityAdapterRegistry);
    this.entityIdFactory = checkNotNull(entityIdFactory);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    blobStore.start();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    blobStore.stop();
  }

  /**
   * Executes the given function using a database connection.
   */
  protected <T> T execute(Function<ODatabaseDocumentTx, T> function) {
    return doExecute(function, false, null);
  }

  /**
   * Executes the given function using a database connection, with optional blob deletion on success and/or failure.
   */
  protected <T> T execute(Function<ODatabaseDocumentTx, T> function, BlobTx blobTx) {
    return doExecute(function, false, blobTx);
  }

  /**
   * Executes the given function using a database connection in a transaction, with optional blob deletion
   * on success and/or failure.
   */
  protected <T> T executeInTransaction(Function<ODatabaseDocumentTx, T> function, BlobTx blobTx) {
    return doExecute(function, true, blobTx);
  }

  /**
   * Gets a new {@link BlobTx} for tracking blob creation and deletion.
   */
  protected BlobTx beginBlobTx() {
    return new BlobTx(blobStore);
  }

  protected Envelope createComponentWithAssets(final ODatabaseDocumentTx db,
                                               final BlobTx blobTx,
                                               final Envelope sourceEnvelope) {
    Component sourceComponent = sourceEnvelope.getComponent();
    EntityAdapter componentAdapter = registeredComponentAdapter(sourceComponent.getClassName());

    EntityId componentId = entityIdFactory.newId();

    ODocument componentDocument = createComponentDocument(db, componentAdapter, sourceComponent, componentId);

    Set<Asset> assets = Sets.newHashSet();
    Set<EntityId> assetIds = Sets.newHashSet();
    Set<ORID> assetDocumentRids = Sets.newHashSet();
    for (Asset sourceAsset: sourceEnvelope.getAssets()) {
      EntityAdapter assetAdapter = registeredAssetAdapter(sourceAsset.getClassName());

      EntityId assetId = entityIdFactory.newId();

      Blob blob = createBlob(blobTx, sourceAsset, assetId);
      checkArgument(blob != null, "Cannot store new asset without a content stream");

      ODocument assetDocument = createAssetDocument(db, assetAdapter, sourceAsset, assetId, blob,
          componentDocument.getIdentity());

      assets.add(assetFrom(assetAdapter, assetDocument, componentId, blob));
      assetIds.add(assetId);
      assetDocumentRids.add(assetDocument.getIdentity());
    }

    if (assetDocumentRids.size() > 0) {
      componentDocument.field(P_ASSETS, assetDocumentRids);
      componentDocument.save();
    }

    return new Envelope(componentFrom(componentAdapter, componentDocument, assetIds), assets);
  }

  protected void deleteComponent(ODatabaseDocumentTx db, BlobTx blobTx, EntityAdapter componentAdapter,
                                 ODocument componentDocument) {
    Set<ODocument> assetDocuments = componentDocument.field(P_ASSETS);
    for (ODocument assetDocument: assetDocuments) {
      blobTx.delete(getLocalBlob(assetDocument));
      assetDocument.delete();
    }
    componentDocument.delete();
  }

  protected void deleteAsset(ODatabaseDocumentTx db, BlobTx blobTx, EntityAdapter assetAdapter,
                             ODocument assetDocument) {
    ODocument componentDocument = assetDocument.field(P_COMPONENT);
    Set<ORID> assetDocumentRids = componentDocument.field(P_ASSETS);
    assetDocumentRids.remove(assetDocument.getIdentity());
    componentDocument.field(P_ASSETS, assetDocumentRids);
    componentDocument.save();
    blobTx.delete(getLocalBlob(assetDocument));
    assetDocument.delete();
  }

  protected Component componentFrom(ODatabaseDocumentTx db, EntityAdapter componentAdapter, ODocument componentDocument) {
    Set<EntityId> assetIds = Sets.newHashSet();
    String query = String.format("SELECT FROM %s WHERE %s = ?", AssetAdapter.CLASS_NAME, P_COMPONENT);
    ORID componentRid = componentDocument.getIdentity();
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(componentRid);
    for (ODocument assetDocument: results) {
      assetIds.add(new EntityId((String) assetDocument.field(P_ID)));
    }

    return componentFrom(componentAdapter, componentDocument, assetIds);
  }

  protected Component componentFrom(EntityAdapter componentAdapter, ODocument componentDocument, Set<EntityId> assetIds) {
    Component component = new Component(componentAdapter.getClassName());
    populateEntity(component, componentDocument, ComponentAdapter.SYSTEM_PROPS);

    component.put(P_ID, entityId(componentDocument));
    component.put(P_ASSETS, assetIds);

    return component;
  }

  protected void populateEntity(Entity entity, ODocument document, Set<String> ignoreProps) {
    for (String name: document.fieldNames()) {
      if (!ignoreProps.contains(name)) {
        entity.put(name, toExternalType(document.field(name)));
      }
    }
  }

  protected Object toExternalType(Object o) {
    if (o instanceof Date) {
      return new DateTime((Date) o);
    }
    return o;
  }

  protected Object toStorageType(Object o) {
    if (o instanceof DateTime) {
      return ((DateTime) o).toDate();
    }
    else if (o instanceof EntityId) {
      return ((EntityId) o).asUniqueString();
    }
    return o;
  }

  protected Asset assetFrom(EntityAdapter assetAdapter, ODocument assetDocument) {
    ODocument componentDocument = assetDocument.field(P_COMPONENT);
    EntityId componentId = entityId(componentDocument);
    Blob blob = getLocalBlob(assetDocument);
    return assetFrom(assetAdapter, assetDocument, componentId, blob);
  }

  protected Asset assetFrom(final EntityAdapter assetAdapter,
                            final ODocument assetDocument,
                            final EntityId componentId,
                            final Blob blob) {
    Asset asset = new Asset(assetAdapter.getClassName());
    populateEntity(asset, assetDocument, AssetAdapter.SYSTEM_PROPS);

    asset.put(P_ID, entityId(assetDocument));
    asset.put(P_COMPONENT, componentId);
    asset.put(P_FIRST_CREATED, toExternalType(assetDocument.field(P_FIRST_CREATED)));
    asset.put(P_LAST_MODIFIED, blob.getMetrics().getCreationTime());
    asset.put(P_CONTENT_LENGTH, blob.getMetrics().getContentSize());
    asset.put(P_BLOB_REFS, toExternalType(assetDocument.field(P_BLOB_REFS)));
    asset.setStreamSupplier(new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        return blob.getInputStream();
      }
    });

    return asset;
  }

  protected ODocument createComponentDocument(ODatabaseDocumentTx db,
                                              EntityAdapter componentAdapter,
                                              Entity sourceComponent,
                                              EntityId componentId) {
    ODocument componentDocument = db.newInstance(componentAdapter.getClassName());
    populateDocument(sourceComponent, componentDocument, ComponentAdapter.SYSTEM_PROPS);

    componentDocument.field(P_ID, toStorageType(componentId));
    Set<ORID> assets = ImmutableSet.of();
    componentDocument.field(P_ASSETS, assets);

    return componentDocument.save();
  }

  protected ODocument createAssetDocument(ODatabaseDocumentTx db,
                                          EntityAdapter assetAdapter,
                                          Entity sourceAsset,
                                          EntityId assetId,
                                          Blob blob,
                                          ORID componentDocumentRid) {
    ODocument assetDocument = db.newInstance(assetAdapter.getClassName());
    populateDocument(sourceAsset, assetDocument, AssetAdapter.SYSTEM_PROPS);

    assetDocument.field(P_ID, toStorageType(assetId));
    assetDocument.field(P_FIRST_CREATED, toStorageType(blob.getMetrics().getCreationTime()));
    assetDocument.field(P_COMPONENT, componentDocumentRid);
    Map<String, String> blobRefs = ImmutableMap.of(localBlobStoreId, blob.getId().asUniqueString());
    assetDocument.field(P_BLOB_REFS, blobRefs);

    return assetDocument.save();
  }

  protected void populateDocument(Entity entity, ODocument document, Set<String> ignoreProps) {
    Map<String, Object> props = entity.toMap(false);
    for (String name: props.keySet()) {
      if (!ignoreProps.contains(name)) {
        document.field(name, toStorageType(props.get(name)));
      }
    }
  }

  protected EntityAdapter registeredAdapter(@Nullable String className) {
    if (className == null) {
      className = EntityAdapter.CLASS_NAME;
    }
    EntityAdapter adapter = entityAdapterRegistry.getAdapter(className);
    checkState(adapter != null, "No entity adapter registered for class %", className);
    return adapter;
  }

  protected EntityAdapter registeredComponentAdapter(@Nullable String className) {
    EntityAdapter adapter = registeredAdapter(className);
    checkState(adapter instanceof ComponentAdapter, "No component adapter registered for class %", className);
    return adapter;
  }

  protected EntityAdapter registeredAssetAdapter(@Nullable String className) {
    EntityAdapter adapter = registeredAdapter(className);
    checkState(adapter instanceof AssetAdapter, "No asset adapter registered for class %", className);
    return adapter;
  }

  protected List<ODocument> executeQuery(ODatabaseDocumentTx db, String query, Map<String, Object> parameters) {
    log.info("Executing query: {} with parameters: {}", query, parameters);
    return db.command(new OSQLSynchQuery<>(query)).execute(parameters);
  }

  protected OrientQueryBuilder queryBuilder(ODatabaseDocumentTx db,
                                          EntityAdapter adapter,
                                          @Nullable MetadataQuery query) {
    if (query == null) {
      return new OrientQueryBuilder(new MetadataQuery());
    }
    else if (query.skipEntityId() == null) {
      return new OrientQueryBuilder(query);
    }
    else {
      ODocument skipDocument = retrieveExistingDocument(db, adapter, query.skipEntityId());
      return new OrientQueryBuilder(query, skipDocument.getIdentity());
    }
  }

  protected MetadataQuery queryWith(@Nullable MetadataQueryRestriction restriction) {
    if (restriction == null) {
      return new MetadataQuery();
    }
    else {
      return new MetadataQuery().restriction(restriction);
    }
  }

  protected ODocument retrieveExistingDocument(ODatabaseDocumentTx db,
                                             EntityAdapter adapter,
                                             EntityId entityId) {
    ODocument document = retrieveDocument(db, adapter, entityId);
    checkState(document != null, "No such %s: %s", adapter.getClassName(), entityId.asUniqueString());
    return document;
  }

  @Nullable
  protected ODocument retrieveDocument(ODatabaseDocumentTx db, EntityAdapter adapter, EntityId entityId) {
    final String query = String.format("SELECT FROM %s WHERE %s = ?", adapter.getClassName(), EntityAdapter.P_ID);
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(entityId.asUniqueString());
    if (results.isEmpty()) {
      return null;
    }
    else {
      return results.get(0);
    }
  }

  @Nullable
  protected Blob createBlob(BlobTx blobTx, Asset asset, EntityId assetId) {
    checkNotNull(asset);
    InputStream sourceStream = asset.openStream();
    if (sourceStream == null) {
      return null;
    }
    return blobTx.create(sourceStream, ImmutableMap.of(
        BlobStore.BLOB_NAME_HEADER, assetId.asUniqueString(),
        BlobStore.CREATED_BY_HEADER, "")); // TODO: Determine where this comes from
  }

  protected Blob getLocalBlob(ODocument assetDocument) {
    Map<String, String> blobRefs = assetDocument.field(P_BLOB_REFS);
    checkState(blobRefs.containsKey(localBlobStoreId), "Local blobRef does not exist for that asset");
    BlobId blobId = new BlobId(blobRefs.get(localBlobStoreId));
    Blob blob = blobStore.get(new BlobId(blobRefs.get(localBlobStoreId)));
    checkState(blob != null, "Blob not found in local store: %", blobId.asUniqueString());
    return blob;
  }

  /**
   * Executes the given function using a database connection, optionally in a transaction, with optional
   * blob deletion on success and/or failure.
   *
   * @param useTransaction whether an explicit transaction should be used. If {@code true}, a transaction will be
   *        started on the {@code ODatabaseDocumentTx} before the function is executed. If a runtime exception occurs
   *        in the course of execution, this transaction will be automatically rolled back. Otherwise, the transaction
   *        will be automatically committed.
   * @param blobTx if specified, indicates which blobs to delete on sucess or failure.
   * @param function the function to execute.
   * @return the value returned by the given function.
   */
  private <T> T doExecute(Function<ODatabaseDocumentTx, T> function, boolean useTransaction, @Nullable BlobTx blobTx) {
    boolean successfullyFinished = false;
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      if (useTransaction) {
        db.begin();
      }
      try {
        T result = function.apply(db);
        if (useTransaction) {
          db.commit();
        }
        successfullyFinished = true;
        return result;
      }
      finally {
        if (successfullyFinished) {
          if (blobTx != null) {
            blobTx.commit();
          }
        }
        else {
          if (useTransaction && db.getTransaction().isActive()) {
            db.rollback();
          }
          if (blobTx != null) {
            blobTx.rollback();
          }
        }
      }
    }
  }
}
