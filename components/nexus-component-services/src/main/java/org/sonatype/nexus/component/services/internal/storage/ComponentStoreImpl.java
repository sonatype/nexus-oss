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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.Blob;
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
import org.sonatype.nexus.component.services.id.EntityIdFactory;
import org.sonatype.nexus.component.services.internal.query.OrientQueryBuilder;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_BLOB_REFS;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_COMPONENT;
import static org.sonatype.nexus.component.services.adapter.ComponentAdapter.P_ASSETS;
import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;

/**
 * Default {@link ComponentStore} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentStoreImpl
    extends ComponentStoreImplSupport
    implements ComponentStore
{
  @Inject
  public ComponentStoreImpl(
      @Named(ComponentMetadataDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider,
      @Named(ComponentBlobStoreProvider.NAME) BlobStore blobStore,
      EntityAdapterRegistry entityAdapterRegistry,
      EntityIdFactory entityIdFactory) {
    super(databaseInstanceProvider, blobStore, entityAdapterRegistry, entityIdFactory);
  }

  @Override
  public void prepareStorage(final String... classNames) {
    for (String className: checkNotNull(classNames)) {
      entityAdapterRegistry.getAdapter(className);
    }
  }

  @Override
  public long count(@Nullable final String className, @Nullable final MetadataQueryRestriction restriction) {
    final EntityAdapter adapter = registeredAdapter(className);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = queryBuilder(db, adapter, queryWith(restriction));
      String countQuery = queryBuilder.buildQuery(adapter.getClassName(), true, adapter instanceof ComponentAdapter);
      return executeQuery(db, countQuery, queryBuilder.getParameters()).get(0).field("COUNT");
    }
  }

  @Override
  public List<Component> findComponents(@Nullable final String className, @Nullable final MetadataQuery metadataQuery) {
    final EntityAdapter adapter = registeredComponentAdapter(className);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = queryBuilder(db, adapter, metadataQuery);
      String query = queryBuilder.buildQuery(adapter.getClassName(), false, true);
      return FluentIterable
          .from(executeQuery(db, query, queryBuilder.getParameters()))
          .transform(new Function<ODocument, Component>()
          {
            @Override
            public Component apply(final ODocument document) {
              return componentFrom(db, adapter, document);
            }
          })
          .toList();
    }
  }

  @Override
  public List<Asset> findAssets(@Nullable final String className, @Nullable final MetadataQuery metadataQuery) {
    final EntityAdapter adapter = registeredAssetAdapter(className);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = queryBuilder(db, adapter, metadataQuery);
      String query = queryBuilder.buildQuery(adapter.getClassName(), false, false);
      return FluentIterable
          .from(executeQuery(db, query, queryBuilder.getParameters()))
          .transform(new Function<ODocument, Asset>()
          {
            @Override
            public Asset apply(final ODocument document) {
              return assetFrom(adapter, document);
            }
          })
          .toList();
    }
  }



  @Override
  public Component createComponent(final Component sourceComponent) {
    checkNotNull(sourceComponent);

    return createComponentWithAssets(new Envelope(sourceComponent)).getComponent();
  }

  @Override
  public Asset createAsset(final EntityId componentId, final Asset sourceAsset) {
    checkNotNull(componentId);
    checkNotNull(sourceAsset);

    final EntityAdapter assetAdapter = registeredAssetAdapter(sourceAsset.getClassName());
    final EntityAdapter componentAdapter = registeredComponentAdapter(ComponentAdapter.CLASS_NAME);
    final BlobTx blobTx = beginBlobTx();

    return executeInTransaction(new Function<ODatabaseDocumentTx, Asset>()
    {
      @Override
      public Asset apply(final ODatabaseDocumentTx db) {
        ODocument componentDocument = retrieveExistingDocument(db, componentAdapter, componentId);

        EntityId assetId = entityIdFactory.newId();
        Blob blob = createBlob(blobTx, sourceAsset, assetId);
        checkArgument(blob != null, "Cannot store new asset without a content stream");

        ODocument assetDocument = createAssetDocument(db, assetAdapter, sourceAsset, assetId, blob,
            componentDocument.getIdentity());

        Set<ORID> assetDocumentRids = componentDocument.field(P_ASSETS);
        assetDocumentRids.add(assetDocument.getIdentity());
        componentDocument.field(P_ASSETS, assetDocumentRids);
        componentDocument.save();

        return assetFrom(assetAdapter, assetDocument, componentId, blob);
      }
    }, blobTx);
  }

  @Override
  public Envelope createComponentWithAssets(final Envelope sourceEnvelope) {
    checkNotNull(sourceEnvelope);

    final BlobTx blobTx = beginBlobTx();

    return executeInTransaction(new Function<ODatabaseDocumentTx, Envelope>()
    {
      @Override
      public Envelope apply(final ODatabaseDocumentTx db) {
        return createComponentWithAssets(db, blobTx, sourceEnvelope);
      }
    }, blobTx);
  }

  @Override
  public boolean createComponentsWithAssets(final Iterable<Envelope> sourceEnvelopes,
                                            @Nullable final Predicate<List<EntityId>> progressListener,
                                            final int commitFrequency)
  {
    checkNotNull(sourceEnvelopes);
    checkArgument(commitFrequency >= 0);
    final Iterator<Envelope> iterator = sourceEnvelopes.iterator();

    boolean stopRequested = false;
    while (iterator.hasNext() && !stopRequested) {
      final BlobTx blobTx = beginBlobTx();
      stopRequested = !executeInTransaction(new Function<ODatabaseDocumentTx, Boolean>()
      {
        @Override
        public Boolean apply(final ODatabaseDocumentTx db) {
          // create all components with assets for this transaction
          List<EntityId> componentIds = Lists.newArrayList();
          while (iterator.hasNext() && (commitFrequency == 0 || componentIds.size() < commitFrequency))
          {
            Entity component = createComponentWithAssets(db, blobTx, iterator.next()).getComponent();
            componentIds.add(component.get(P_ID, EntityId.class));
          }
          // pass the ids to the listener and return the result, which is false if stop is requested
          return progressListener == null || progressListener.apply(componentIds);
        }
      }, blobTx);
    }

    return !iterator.hasNext();
  }

  @Override
  public Component readComponent(@Nullable final String className, final EntityId entityId) {
    checkNotNull(entityId);

    final EntityAdapter componentAdapter = registeredComponentAdapter(className);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      return componentFrom(db, componentAdapter, retrieveExistingDocument(db, componentAdapter, entityId));
    }
  }

  @Override
  public Asset readAsset(@Nullable final String className, final EntityId entityId) {
    checkNotNull(entityId);

    final EntityAdapter assetAdapter = registeredAssetAdapter(className);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      return assetFrom(assetAdapter, retrieveExistingDocument(db, assetAdapter, entityId));
    }
  }

  @Override
  public Envelope readComponentWithAssets(@Nullable final String componentClassName, final EntityId componentId) {
    checkNotNull(componentId);

    final EntityAdapter componentAdapter = registeredComponentAdapter(
        componentClassName == null ? ComponentAdapter.CLASS_NAME : componentClassName);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      ODocument componentDocument = retrieveExistingDocument(db, componentAdapter, componentId);

      Set<Asset> assets = Sets.newHashSet();
      Set<EntityId> assetIds = Sets.newHashSet();
      Set<ODocument> assetDocuments = componentDocument.field(P_ASSETS);
      for (ODocument assetDocument: assetDocuments) {
        Asset asset = assetFrom(registeredAssetAdapter(assetDocument.getClassName()), assetDocument);
        assets.add(asset);
        assetIds.add(asset.get(P_ID, EntityId.class));
      }

      return new Envelope(componentFrom(componentAdapter, componentDocument, assetIds), assets);
    }
  }

  @Override
  public Component updateComponent(final EntityId componentId, final Component sourceComponent) {
    checkNotNull(componentId);
    checkNotNull(sourceComponent);

    final EntityAdapter componentAdapter = registeredComponentAdapter(sourceComponent.getClassName());

    return execute(new Function<ODatabaseDocumentTx, Component>()
    {
      @Override
      public Component apply(final ODatabaseDocumentTx db) {
        ODocument componentDocument = retrieveExistingDocument(db, componentAdapter, componentId);

        populateDocument(sourceComponent, componentDocument, ComponentAdapter.SYSTEM_PROPS);
        componentDocument.save();

        return componentFrom(db, componentAdapter, componentDocument);
      }
    });
  }

  @Override
  public Asset updateAsset(final EntityId assetId, final Asset sourceAsset) {
    checkNotNull(assetId);
    checkNotNull(sourceAsset);

    final EntityAdapter assetAdapter = registeredAssetAdapter(sourceAsset.getClassName());

    final BlobTx blobTx = beginBlobTx();

    return executeInTransaction(new Function<ODatabaseDocumentTx, Asset>()
    {
      @Override
      public Asset apply(final ODatabaseDocumentTx db) {
        ODocument assetDocument = retrieveExistingDocument(db, assetAdapter, assetId);
        ODocument componentDocument = assetDocument.field(P_COMPONENT);
        EntityId componentId = entityId(componentDocument);

        populateDocument(sourceAsset, assetDocument, AssetAdapter.SYSTEM_PROPS);

        Blob currentBlob = getLocalBlob(assetDocument);
        Blob newBlob = createBlob(blobTx, sourceAsset, assetId);
        if (newBlob != null) {
          blobTx.delete(currentBlob);
          currentBlob = newBlob;
          Map<String, String> blobRefs = ImmutableMap.of(localBlobStoreId, currentBlob.getId().asUniqueString());
          assetDocument.field(P_BLOB_REFS, blobRefs);
        }

        assetDocument.save();

        return assetFrom(assetAdapter, assetDocument, componentId, currentBlob);
      }
    }, blobTx);
  }

  @Override
  public boolean delete(final @Nullable String className, final EntityId entityId) {
    checkNotNull(entityId);

    final EntityAdapter adapter = registeredAdapter(className);
    final BlobTx blobTx = beginBlobTx();

    return executeInTransaction(new Function<ODatabaseDocumentTx, Boolean>()
    {
      @Override
      public Boolean apply(final ODatabaseDocumentTx db) {
        ODocument document = retrieveDocument(db, adapter, entityId);
        if (document == null) {
          return false;
        }
        EntityAdapter specificAdapter = registeredAdapter(document.getClassName());
        if (specificAdapter instanceof ComponentAdapter) {
          deleteComponent(db, blobTx, specificAdapter, document);
        }
        else {
          deleteAsset(db, blobTx, specificAdapter, document);
        }
        return true;
      }
    }, blobTx);
  }
}
