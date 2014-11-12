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
package org.sonatype.nexus.component.services.internal.storage;

import java.io.IOException;
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
import org.sonatype.nexus.component.model.ComponentEnvelope;
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.id.EntityIdFactory;
import org.sonatype.nexus.component.services.internal.query.OrientQueryBuilder;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
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
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_FIRST_CREATED;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;

/**
 * Supporting base class for {@link ComponentStoreImpl}.
 *
 * @since 3.0
 */
abstract class ComponentStoreImplSupport
    extends LifecycleSupport
    implements EventSubscriber
{
  protected final EntityAdapterRegistry entityAdapterRegistry;

  // TODO: Determine how to get this when BlobStoreManager (or whatever we call it) is implemented
  private static final String localBlobStoreId = "someBlobStoreId"; // hardcoded for now..

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  // TODO: Delegate to something else instead of using a BlobStore directly here...
  private final BlobStore blobStore;

  private final EntityIdFactory entityIdFactory;

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
    start();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    stop();
  }

  @Override
  protected void doStart() throws Exception {
    blobStore.start();
  }

  @Override
  protected void doStop() throws Exception {
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

  /**
   * @see ComponentStore#countAssets(Class, MetadataQueryRestriction)
   * @see ComponentStore#countComponents(Class, MetadataQueryRestriction)
   */
  protected <T extends Entity> long count(final Class<T> entityClass,
                                          @Nullable final MetadataQueryRestriction restriction) {
    checkNotNull(entityClass);
    return execute(new Function<ODatabaseDocumentTx, Long>()
    {
      @Override
      public Long apply(final ODatabaseDocumentTx db) {
        OrientQueryBuilder queryBuilder = orientQueryBuilder(db, entityClass, queryWith(restriction));
        String countQuery = queryBuilder.buildQuery(entityClass, true);
        return executeCount(db, countQuery, queryBuilder.getParameters());
      }
    });
  }

  /**
   * @see ComponentStore#findAssets(Class, MetadataQuery)
   */
  protected <A extends Asset> List<A> findAssets(final ODatabaseDocumentTx db,
                                                 final Class<A> assetClass,
                                                 @Nullable final MetadataQuery metadataQuery) {
    checkAllNotNull(db, assetClass);
    OrientQueryBuilder queryBuilder = orientQueryBuilder(db, assetClass, metadataQuery);
    String query = queryBuilder.buildQuery(assetClass, false);
    return FluentIterable
        .from(executeQuery(db, query, queryBuilder.getParameters()))
        .transform(new Function<ODocument, A>()
        {
          @Override
          public A apply(final ODocument assetDocument) {
            return assetFrom(registeredAssetAdapter(assetClass), assetDocument);
          }
        })
        .toList();
  }

  /**
   * @see ComponentStore#findComponents(Class, MetadataQuery)
   */
  protected <C extends Component> List<C> findComponents(final ODatabaseDocumentTx db,
                                                         final Class<C> componentClass,
                                                         @Nullable final MetadataQuery metadataQuery) {
    checkAllNotNull(db, componentClass);
    OrientQueryBuilder queryBuilder = orientQueryBuilder(db, componentClass, metadataQuery);
    String query = queryBuilder.buildQuery(componentClass, false);
    return FluentIterable
        .from(executeQuery(db, query, queryBuilder.getParameters()))
        .transform(new Function<ODocument, C>()
        {
          @Override
          public C apply(final ODocument componentDocument) {
            return componentFrom(db, registeredComponentAdapter(componentClass), componentDocument);
          }
        })
        .toList();
  }

  /**
   * @see ComponentStore#createComponent(Component)
   */
  protected <C extends Component> C createComponent(ODatabaseDocumentTx db, C sourceComponent) {
    Iterable<Asset> sourceAssets = ImmutableSet.of();
    ComponentEnvelope<C, Asset> sourceEnvelope = new ComponentEnvelope<>(sourceComponent, sourceAssets);
    return createComponentWithAssets(db, sourceEnvelope, beginBlobTx());
  }

  /**
   * @see ComponentStore#createAsset(EntityId, Asset)
   */
  @SuppressWarnings("unchecked")
  protected <A extends Asset> A createAsset(ODatabaseDocumentTx db, EntityId componentId, A sourceAsset,
      BlobTx blobTx) {
    // check args, get adapter, and make sure the asset exists
    checkAllNotNull(db, componentId, sourceAsset, blobTx);
    Class<A> assetClass = (Class<A>) sourceAsset.getClass();
    AssetAdapter<A> assetAdapter = registeredAssetAdapter(assetClass);
    ODocument componentDocument = retrieveExistingDocument(db, Component.class, componentId);

    // generate asset id
    EntityId assetId = entityIdFactory.newId();

    // create blob from source
    Blob blob = createBlob(blobTx, sourceAsset, assetId);
    checkArgument(blob != null, "Cannot store new asset without a content stream");

    // create and save a new asset document using source and system-provided data
    ODocument assetDocument = createAssetDocument(db, assetClass, sourceAsset, assetAdapter, assetId,
        blob, componentDocument.getIdentity());

    // add asset document reference to component document and save it
    Set<ORID> assetDocumentRids = componentDocument.field(P_ASSETS);
    assetDocumentRids.add(assetDocument.getIdentity());
    componentDocument.field(P_ASSETS, assetDocumentRids);
    componentDocument.save();

    // return a new entity reflecting the stored state
    return assetFrom(assetAdapter, assetDocument, componentId, blob);
  }

  /**
   * @see ComponentStore#createComponentsWithAssets(Iterable, Predicate, int)
   */
  @SuppressWarnings("unchecked")
  protected <C extends Component, A extends Asset> C createComponentWithAssets(ODatabaseDocumentTx db,
      ComponentEnvelope<C, A> sourceEnvelope, BlobTx blobTx) {
    // check args and get adapters
    checkAllNotNull(db, sourceEnvelope, blobTx);
    C sourceComponent = sourceEnvelope.getComponent();
    Class<C> componentClass = (Class<C>) sourceComponent.getClass();
    ComponentAdapter<C> componentAdapter = registeredComponentAdapter(componentClass);

    // generate component id
    EntityId componentId = entityIdFactory.newId();

    // create component document using source data
    ODocument componentDocument = db.newInstance(new OClassNameBuilder().type(componentClass).build());
    componentAdapter.populateDocument(sourceComponent, componentDocument);

    // amend component document with system-managed data
    componentDocument.field(EntityAdapter.P_ID, componentId.asUniqueString());
    Set<ORID> assets = ImmutableSet.of();
    componentDocument.field(P_ASSETS, assets);

    // store component document
    componentDocument.save();

    // create all associated assets and hold on to them, their entity ids, and rids
    Set<EntityId> assetIds = Sets.newHashSet();
    Set<ORID> assetDocumentRids = Sets.newHashSet();
    for (A sourceAsset: sourceEnvelope.getAssets()) {
      Class<A> assetClass = (Class<A>) sourceAsset.getClass();
      AssetAdapter<A> assetAdapter = registeredAssetAdapter(assetClass);

      // generate asset id
      EntityId assetId = entityIdFactory.newId();

      // create blob from source
      Blob blob = createBlob(blobTx, sourceAsset, assetId);
      checkArgument(blob != null, "Cannot store new asset without a content stream");

      // create and save a new asset document using source and system-provided data
      ODocument assetDocument = createAssetDocument(db, assetClass, sourceAsset, assetAdapter, assetId,
          blob, componentDocument.getIdentity());

      // remember ids for later
      assetIds.add(assetId);
      assetDocumentRids.add(assetDocument.getIdentity());
    }

    // update and save the component document with asset rids
    if (assetDocumentRids.size() > 0) {
      componentDocument.field(P_ASSETS, assetDocumentRids);
      componentDocument.save();
    }

    // return a new entity reflecting the stored state
    return componentFrom(componentAdapter, componentDocument, assetIds);
  }

  /**
   * @see ComponentStore#readAsset(Class, EntityId)
   */
  protected <A extends Asset> A readAsset(ODatabaseDocumentTx db, Class<A> assetClass, EntityId assetId) {
    return assetFrom(
        registeredAssetAdapter(checkNotNull(assetClass)),
        retrieveExistingDocument(checkNotNull(db), assetClass, assetId));
  }

  /**
   * @see ComponentStore#readComponent(Class, EntityId)
   */
  protected <C extends Component> C readComponent(ODatabaseDocumentTx db, Class<C> componentClass, EntityId componentId) {
    return componentFrom(checkNotNull(db),
        registeredComponentAdapter(checkNotNull(componentClass)),
        retrieveExistingDocument(db, componentClass, componentId));
  }

  /**
   * @see ComponentStore#readComponentWithAssets(Class, Class, EntityId)
   */
  protected <C extends Component, A extends Asset> ComponentEnvelope<C, A> readComponentsWithAssets(
      ODatabaseDocumentTx db, Class<C> componentClass, Class<A> assetClass, EntityId componentId) {
    // check args, get adapters, and make sure the component exists
    checkAllNotNull(db, componentClass, assetClass, componentId);
    ComponentAdapter<C> componentAdapter = registeredComponentAdapter(componentClass);
    AssetAdapter<A> assetAdapter = registeredAssetAdapter(assetClass);
    ODocument componentDocument = retrieveExistingDocument(db, componentClass, componentId);

    // read assets
    Set<EntityId> assetIds = Sets.newHashSet();
    Set<A> assets = Sets.newHashSet();
    Set<ODocument> assetDocuments = componentDocument.field(P_ASSETS);
    for (ODocument assetDocument: assetDocuments) {
      A asset = assetFrom(assetAdapter, assetDocument);
      assets.add(asset);
      assetIds.add(asset.getId());
    }

    // return envelope with component and assets
    C component = componentFrom(componentAdapter, componentDocument, assetIds);
    return new ComponentEnvelope<>(component, assets);
  }

  /**
   * @see ComponentStore#updateComponent(EntityId, Component)
   */
  @SuppressWarnings("unchecked")
  protected <C extends Component> C updateComponent(ODatabaseDocumentTx db, EntityId componentId, C sourceComponent) {
    // check args, get adapter, and make sure the document exists
    checkAllNotNull(db, componentId, sourceComponent);
    Class<C> componentClass = (Class<C>) sourceComponent.getClass();
    ComponentAdapter<C> componentAdapter = registeredComponentAdapter(componentClass);
    ODocument componentDocument = retrieveExistingDocument(db, componentClass, componentId);

    // update and save component document using source data
    componentAdapter.populateDocument(sourceComponent, componentDocument);
    componentDocument.save();

    // return a new entity reflecting the stored state
    return componentFrom(db, componentAdapter, componentDocument);
  }

  /**
   * @see ComponentStore#updateAsset(EntityId, Asset)
   */
  @SuppressWarnings("unchecked")
  protected <A extends Asset> A updateAsset(ODatabaseDocumentTx db, EntityId assetId, A sourceAsset, BlobTx blobTx) {
    // check args, get adapter, make sure the asset and component exists, and grab the component id
    checkAllNotNull(db, assetId, sourceAsset, blobTx);
    Class<A> assetClass = (Class<A>) sourceAsset.getClass();
    AssetAdapter<A> assetAdapter = registeredAssetAdapter(assetClass);
    ODocument assetDocument = retrieveExistingDocument(db, assetClass, assetId);
    ODocument componentDocument = assetDocument.field(P_COMPONENT);
    EntityId componentId = new EntityId((String) componentDocument.field(EntityAdapter.P_ID));

    // update asset document using source data
    assetAdapter.populateDocument(sourceAsset, assetDocument);

    // grab the local blob
    Blob currentBlob = getLocalBlob(assetDocument);

    // create blob from source if it specifies a content change
    Blob newBlob = createBlob(blobTx, sourceAsset, assetId);
    if (newBlob != null) {
      blobTx.delete(currentBlob);
      currentBlob = newBlob;

      // update blob ref with new one
      Map<String, String> blobRefs = ImmutableMap.of(localBlobStoreId, currentBlob.getId().asUniqueString());
      assetDocument.field(P_BLOB_REFS, blobRefs);
    }

    // populate other base asset properties
    assetDocument.field(P_CONTENT_TYPE, sourceAsset.getContentType());
    assetDocument.field(P_PATH, sourceAsset.getPath());

    // save changes to asset document
    assetDocument.save();

    // return a new entity reflecting the stored state
    return assetFrom(assetAdapter, assetDocument, componentId, currentBlob);
  }

  /**
   * @see ComponentStore#deleteAsset(Class, EntityId)
   */
  protected <A extends Asset> boolean deleteAsset(ODatabaseDocumentTx db, Class<A> assetClass, EntityId assetId,
      BlobTx blobTx) {
    checkAllNotNull(db, assetClass, assetId, blobTx);
    ODocument assetDocument = retrieveDocument(db, assetClass, assetId);
    if (assetDocument == null) {
      return false;
    }

    ODocument componentDocument = assetDocument.field(P_COMPONENT);

    Set<ORID> assetDocumentRids = componentDocument.field(P_ASSETS);
    assetDocumentRids.remove(assetDocument.getIdentity());
    componentDocument.field(P_ASSETS, assetDocumentRids);
    componentDocument.save();

    blobTx.delete(getLocalBlob(assetDocument));
    assetDocument.delete();
    return true;
  }

  /**
   * @see ComponentStore#deleteComponent(Class, EntityId)
   */
  protected <C extends Component> boolean deleteComponent(ODatabaseDocumentTx db, Class<C> componentClass,
      EntityId componentId, BlobTx blobTx) {
    checkAllNotNull(db, componentClass, componentId, blobTx);
    ODocument componentDocument = retrieveDocument(db, componentClass, componentId);
    if (componentDocument == null) {
      return false;
    }

    Set<ODocument> assetDocuments = componentDocument.field(P_ASSETS);
    for (ODocument assetDocument: assetDocuments) {
      blobTx.delete(getLocalBlob(assetDocument));
      assetDocument.delete();
    }
    componentDocument.delete();
    return true;
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

  private List<ODocument> executeQuery(ODatabaseDocumentTx db, String query, Map<String, Object> parameters) {
    log.info("Executing query: {} with parameters: {}", query, parameters);
    return db.command(new OSQLSynchQuery<>(query)).execute(parameters);
  }

  /**
   * @see ComponentStore#countAssets(Class, MetadataQueryRestriction)
   * @see ComponentStore#countComponents(Class, MetadataQueryRestriction)
   */
  private long executeCount(ODatabaseDocumentTx db, String countQuery, Map<String, Object> parameters) {
    return executeQuery(db, countQuery, parameters).get(0).field("COUNT");
  }

  private <A extends Asset> AssetAdapter<A> registeredAssetAdapter(final Class<A> assetClass) {
    AssetAdapter<A> adapter = entityAdapterRegistry.getAssetAdapter(checkNotNull(assetClass));
    checkState(adapter != null, "No asset adapter registered for class %", assetClass);
    return adapter;
  }

  private <C extends Component> ComponentAdapter<C> registeredComponentAdapter(final Class<C> componentClass) {
    ComponentAdapter<C> adapter = entityAdapterRegistry.getComponentAdapter(componentClass);
    checkState(adapter != null, "No component adapter registered for class %", componentClass);
    return adapter;
  }

  private <A extends Asset> A assetFrom(AssetAdapter<A> assetAdapter,
      ODocument assetDocument) {
    ODocument componentDocument = assetDocument.field(P_COMPONENT);
    // TODO: For performance, consider storing componentId as a STRING directly in the asset document.
    EntityId componentId = new EntityId((String) componentDocument.field(EntityAdapter.P_ID));
    Blob blob = getLocalBlob(assetDocument);
    return assetFrom(assetAdapter, assetDocument, componentId, blob);
  }

  private <A extends Asset> A assetFrom(EntityAdapter<A> assetEntityAdapter, ODocument assetDocument,
      EntityId componentId, final Blob blob) {
    try {
      A asset = assetEntityAdapter.getEntityClass().newInstance();
      // set type-specific, then base/system-controlled properties
      assetEntityAdapter.populateEntity(assetDocument, asset);
      asset.setId(new EntityId((String) assetDocument.field(P_ID)));
      asset.setComponentId(componentId);
      asset.setContentType((String) assetDocument.field(P_CONTENT_TYPE));
      asset.setPath((String) assetDocument.field(P_PATH));
      asset.setFirstCreated(new DateTime((Date) assetDocument.field(P_FIRST_CREATED)));
      asset.setLastModified(blob.getMetrics().getCreationTime());
      asset.setContentLength(blob.getMetrics().getContentSize());
      asset.setStreamSupplier(new Supplier<InputStream>()
      {
        @Override
        public InputStream get() {
          return blob.getInputStream();
        }
      });
      return asset;
    }
    catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }

  private <C extends Component> C componentFrom(ODatabaseDocumentTx db, ComponentAdapter<C> componentAdapter,
      ODocument componentDocument) {
    // determine asset ids
    // TODO: For performance, consider redundant storage as an EMBEDDEDSET directly in the component document.
    Set<EntityId> assetIds = Sets.newHashSet();
    final String query = String.format("SELECT FROM %s WHERE %s = ?", AssetAdapter.BASE_CLASS_NAME, P_COMPONENT);
    ORID componentRid = componentDocument.getIdentity();
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(componentRid);
    for (ODocument assetDocument: results) {
      assetIds.add(new EntityId((String) assetDocument.field(EntityAdapter.P_ID)));
    }

    return componentFrom(componentAdapter, componentDocument, assetIds);
  }

  private <C extends Entity> C componentFrom(EntityAdapter<C> componentEntityAdapter, ODocument componentDocument,
      Set<EntityId> assetIds) {
    try {
      C entity = componentEntityAdapter.getEntityClass().newInstance();
      // set type-specific, then base/system-controlled properties
      componentEntityAdapter.populateEntity(componentDocument, entity);
      Component component = (Component) entity;
      component.setId(new EntityId((String) componentDocument.field(P_ID)));
      component.setAssetIds(assetIds);
      return entity;
    }
    catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }

  private OrientQueryBuilder orientQueryBuilder(ODatabaseDocumentTx db, Class<? extends Entity> entityClass,
      @Nullable MetadataQuery query) {
    if (query == null) {
      return new OrientQueryBuilder(new MetadataQuery());
    }
    else if (query.skipEntityId() == null) {
      return new OrientQueryBuilder(query);
    }
    else {
      ODocument skipDocument = retrieveExistingDocument(db, entityClass, query.skipEntityId());
      return new OrientQueryBuilder(query, skipDocument.getIdentity());
    }
  }

  private MetadataQuery queryWith(@Nullable MetadataQueryRestriction restriction) {
    if (restriction == null) {
      return new MetadataQuery();
    }
    else {
      return new MetadataQuery().restriction(restriction);
    }
  }

  private ODocument retrieveExistingDocument(ODatabaseDocumentTx db, Class<? extends Entity> entityClass,
      EntityId entityId) {
    ODocument document = retrieveDocument(db, entityClass, entityId);
    checkState(document != null, "No such %s: %s", entityClass.getSimpleName(), entityId.asUniqueString());
    return document;
  }

  @Nullable
  private ODocument retrieveDocument(ODatabaseDocumentTx db, Class<? extends Entity> entityClass,
      EntityId entityId) {
    String className = new OClassNameBuilder().type(entityClass).build();
    final String query = String.format("SELECT FROM %s WHERE %s = ?", className, EntityAdapter.P_ID);
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(entityId.asUniqueString());
    if (results.isEmpty()) {
      return null;
    }
    else {
      return results.get(0);
    }
  }

  @Nullable
  private Blob createBlob(BlobTx blobTx, Asset asset, EntityId assetId) {
    checkNotNull(asset);
    try {
      InputStream sourceStream = asset.openStream();
      if (sourceStream == null) {
        return null;
      }
      return blobTx.create(sourceStream, ImmutableMap.of(
          BlobStore.BLOB_NAME_HEADER, assetId.asUniqueString(),
          BlobStore.CREATED_BY_HEADER, "")); // TODO: Determine where this comes from
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private Blob getLocalBlob(ODocument assetDocument) {
    Map<String, String> blobRefs = assetDocument.field(P_BLOB_REFS);
    checkState(blobRefs.containsKey(localBlobStoreId), "Local blobRef does not exist for that asset");
    BlobId blobId = new BlobId(blobRefs.get(localBlobStoreId));
    Blob blob = blobStore.get(new BlobId(blobRefs.get(localBlobStoreId)));
    checkState(blob != null, "Blob not found in local store: %", blobId.asUniqueString());
    return blob;
  }

  private <A extends Entity> ODocument createAssetDocument(ODatabaseDocumentTx db, Class<A> assetClass, A sourceAsset,
      EntityAdapter<A> assetEntityAdapter, EntityId assetId, Blob blob, ORID componentDocumentRid) {
    // create asset document using source data
    ODocument assetDocument = db.newInstance(new OClassNameBuilder().type(assetClass).build());
    assetEntityAdapter.populateDocument(sourceAsset, assetDocument);

    // amend asset document with base/system-controlled properties
    assetDocument.field(EntityAdapter.P_ID, assetId.asUniqueString());
    assetDocument.field(P_FIRST_CREATED, blob.getMetrics().getCreationTime().toDate());
    assetDocument.field(P_COMPONENT, componentDocumentRid);
    Map<String, String> blobRefs = ImmutableMap.of(localBlobStoreId, blob.getId().asUniqueString());
    assetDocument.field(P_BLOB_REFS, blobRefs);
    Asset asset = (Asset) sourceAsset;
    assetDocument.field(P_CONTENT_TYPE, asset.getContentType());
    assetDocument.field(P_PATH, asset.getPath());

    // store and return asset document
    return assetDocument.save();
  }

  private static void checkAllNotNull(Object... objects) {
    for (Object o: objects) {
      checkNotNull(o);
    }
  }
}
