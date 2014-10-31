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
package org.sonatype.nexus.component.services.internal.query;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.query.MetadataQueryService;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link MetadataQueryService} implementation.
 *
 * @since 3.0
 */
public class MetadataQueryServiceImpl
    extends ComponentSupport
    implements MetadataQueryService
{
  private final Provider<DatabaseInstance> databaseInstanceProvider;

  // TODO: Eventually use AssetStore here instead of directly using BlobStore.
  // What we really need is the ability to grab an Asset whose associated blob is stored on the local node
  // and might be replicated on-demand, given an ODocument. It's ultimately AssetStore's to do that sort of thing,
  // so directly using a BlobStore here is just a stop-gap until AssetStore exposes something like this:
  // public Asset getAsset(ODocument, boolean replicateContentFirstIfNeeded)
  private final BlobStore blobStore;

  private final EntityAdapterRegistry entityAdapterRegistry;

  @Inject
  public MetadataQueryServiceImpl(@Named("componentMetadata") Provider<DatabaseInstance> databaseInstanceProvider,
                                  BlobStore blobStore,
                                  EntityAdapterRegistry entityAdapterRegistry) {
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.blobStore = checkNotNull(blobStore);
    this.entityAdapterRegistry = checkNotNull(entityAdapterRegistry);
  }

  @Override
  public Set<Class<? extends Entity>> entityClasses() {
    return entityAdapterRegistry.entityClasses();
  }

  @Override
  public <T extends Entity> long count(final Class<T> entityClass,
                                       @Nullable final MetadataQueryRestriction restriction)
  {
    final String className = new OClassNameBuilder().type(entityClass).build();
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, className, queryWith(restriction));
      String countQuery = queryBuilder.buildQuery(entityClass, true);
      return executeCount(db, countQuery, queryBuilder.getParameters());
    }
  }

  @Override
  public <T extends Entity> List<T> find(final Class<T> entityClass, @Nullable final MetadataQuery metadataQuery) {
    final EntityAdapter<T> entityAdapter = registeredEntityAdapter(entityClass);
    final String className = new OClassNameBuilder().type(entityClass).build();
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, className, metadataQuery);
      String query = queryBuilder.buildQuery(entityClass, false);
      return FluentIterable
          .from(execute(db, query, queryBuilder.getParameters()))
          .transform(new Function<ODocument, T>() {
            @Override
            public T apply(final ODocument document) {
              T entity = entityAdapter.convertToEntity(document);
              populateRemainderOfEntity(db, document, entity);
              return entity;
            }
          }).toList();
    }
  }

  /**
   * Populates the remainder of the entity pojo with information that is not directly available in
   * the {@code ODocument}, and therefore cannot be populated by the {@link EntityAdapter}.
   *
   * For {@link Component}s, this populates the {@code assetIds}.
   *
   * For {@link Asset}s, this populates the {@code componentId}, {@code contentLength}, {@code lastModified},
   * and {@code streamSupplier}.
   */
  private <T extends Entity> void populateRemainderOfEntity(ODatabaseDocumentTx db, ODocument document, T entity) {
    if (entity instanceof Component) {
      Component component = (Component) entity;
      component.setAssetIds(fetchAssetIds(db, document.getIdentity()));
    } else {
      Asset asset = (Asset) entity;
      ODocument componentDocument = document.field(AssetEntityAdapter.P_COMPONENT);
      // TODO: For performance, consider storing componentId as a STRING directly in the asset document.
      asset.setComponentId(new EntityId((String) componentDocument.field(EntityAdapter.P_ID)));
      final Blob blob = getLocalBlob(document);
      BlobMetrics blobMetrics = blob.getMetrics();
      asset.setContentLength(blobMetrics.getContentSize());
      asset.setLastModified(blobMetrics.getCreationTime());
      asset.setStreamSupplier(new Supplier<InputStream>() {
        @Override
        public InputStream get() {
          return blob.getInputStream();
        }
      });
    }
  }

  private Blob getLocalBlob(ODocument assetDocument) {
    Map<String, String> blobRefs = assetDocument.field(AssetEntityAdapter.P_BLOB_REFS);
    final String localBlobStoreId = "someBlobStoreId"; // TODO: Eventually use the actual local blob store id here
    checkState(blobRefs.containsKey(localBlobStoreId), "Local blobRef does not exist for that asset");
    BlobId blobId = new BlobId(blobRefs.get(localBlobStoreId));
    Blob blob = blobStore.get(new BlobId(blobRefs.get(localBlobStoreId)));
    checkState(blob != null, "Blob not found in local store: %", blobId.asUniqueString());
    return blob;
  }

  // TODO: For performance, consider redundant storage as an EMBEDDEDSET directly in the component document.
  private static Set<EntityId> fetchAssetIds(ODatabaseDocumentTx db, ORID componentRid) {
    Set<EntityId> entityIds = Sets.newHashSet();
    final String query = String.format("SELECT FROM %s WHERE %s = ?",
        AssetEntityAdapter.ORIENT_CLASS_NAME, AssetEntityAdapter.P_COMPONENT);
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(componentRid);
    for (ODocument assetDocument: results) {
      entityIds.add(new EntityId((String) assetDocument.field(EntityAdapter.P_ID)));
    }
    return entityIds;
  }

  private <T extends Entity> EntityAdapter<T> registeredEntityAdapter(final Class<T> entityClass) {
    EntityAdapter<T> adapter = entityAdapterRegistry.getAdapter(entityClass);
    checkState(adapter != null, "No adapter registered for class %", entityClass);
    return adapter;
  }

  private static <T extends Entity> OrientQueryBuilder orientQueryBuilder(
      ODatabaseDocumentTx db, String className, @Nullable MetadataQuery query) {
    if (query == null) {
      return new OrientQueryBuilder(new MetadataQuery());
    }
    else if (query.skipEntityId() == null) {
      return new OrientQueryBuilder(query);
    }
    else {
      ODocument skipDocument = getExistingDocumentWithId(db, className, query.skipEntityId().asUniqueString());
      return new OrientQueryBuilder(query, skipDocument.getIdentity());
    }
  }

  private static ODocument getExistingDocumentWithId(ODatabaseDocumentTx db, String className, String id) {
    ODocument document = getDocumentWithId(db, className, id);
    checkState(document != null, "No such %: %", className, id);
    return document;
  }

  @Nullable
  private static ODocument getDocumentWithId(ODatabaseDocumentTx db, String className, String id) {
    final String query = String.format("SELECT FROM %s WHERE %s = ?", className, EntityAdapter.P_ID);
    List<ODocument> results = db.command(new OSQLSynchQuery<>(query)).execute(id);
    if (results.isEmpty()) {
      return null;
    }
    else {
      return results.get(0);
    }
  }

  private List<ODocument> execute(ODatabaseDocumentTx db, String query, Map<String, Object> parameters) {
    log.info("Executing query: {} with parameters: {}", query, parameters);
    return db.command(new OSQLSynchQuery<>(query)).execute(parameters);
  }

  private long executeCount(ODatabaseDocumentTx db, String countQuery, Map<String, Object> parameters) {
    return execute(db, countQuery, parameters).get(0).field("COUNT");
  }

  private static MetadataQuery queryWith(@Nullable MetadataQueryRestriction restriction) {
    if (restriction == null) {
      return new MetadataQuery();
    }
    else {
      return new MetadataQuery().restriction(restriction);
    }
  }
}
