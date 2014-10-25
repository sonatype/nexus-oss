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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.internal.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.query.MetadataQueryService;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
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

  private final ComponentEntityAdapterRegistry componentAdapterRegistry;

  private final AssetEntityAdapter assetAdapter;

  @Inject
  public MetadataQueryServiceImpl(@Named("componentMetadata") Provider<DatabaseInstance> databaseInstanceProvider,
                                  BlobStore blobStore,
                                  ComponentEntityAdapterRegistry componentAdapterRegistry) {
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.blobStore = checkNotNull(blobStore);
    this.componentAdapterRegistry = checkNotNull(componentAdapterRegistry);
    this.assetAdapter = new AssetEntityAdapter();
  }

  @Override
  public Set<Class<? extends Component>> componentClasses() {
    return componentAdapterRegistry.componentClasses();
  }

  @Override
  public <T extends Component> long countComponents(final Class<T> componentClass,
                                                    final @Nullable MetadataQueryRestriction restriction) {
    String orientClassName;
    if (componentClass == null) {
      orientClassName = ComponentEntityAdapter.ORIENT_BASE_CLASS_NAME;
    }
    else {
      orientClassName = oClassName(componentClass);
    }
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, queryWith(restriction));
      String countQuery = queryBuilder.buildComponentQuery(orientClassName, true);
      return executeCount(db, countQuery, queryBuilder.getParameters());
    }
  }

  @Override
  public long countAssets(final @Nullable MetadataQueryRestriction restriction) {
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, queryWith(restriction));
      String countQuery = queryBuilder.buildAssetQuery(true);
      return executeCount(db, countQuery, queryBuilder.getParameters());
    }
  }

  @Override
  public <T extends Component> List<T> findComponents(final Class<T> componentClass,
                                                      @Nullable final MetadataQuery metadataQuery) {
    final ComponentEntityAdapter<T> componentAdapter = registeredComponentAdapter(componentClass);
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, metadataQuery);
      String query = queryBuilder.buildComponentQuery(oClassName(componentClass), false);
      return FluentIterable
          .from(execute(db, query, queryBuilder.getParameters()))
          .transform(new Function<ODocument, T>() {
            @Override
            public T apply(final ODocument document) {
              return componentAdapter.convertToComponent(document);
            }
          }).toList();
    }
  }

  @Override
  public List<Asset> findAssets(@Nullable final MetadataQuery metadataQuery) {
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      OrientQueryBuilder queryBuilder = orientQueryBuilder(db, metadataQuery);
      String query = queryBuilder.buildAssetQuery(false);
      return FluentIterable
          .from(execute(db, query, queryBuilder.getParameters()))
          .transform(new Function<ODocument, Asset>() {
            @Override
            public Asset apply(final ODocument document) {
              return assetFromDocument(document);
            }
          }).toList();
    }
  }

  private <T extends Component> ComponentEntityAdapter<T> registeredComponentAdapter(final Class<T> componentClass) {
    ComponentEntityAdapter<T> adapter = componentAdapterRegistry.getAdapter(componentClass);
    checkState(adapter != null, "No adapter registered for class %", componentClass);
    return adapter;
  }

  private Asset assetFromDocument(ODocument document) {
    Map<String, String> blobRefs = document.field(AssetEntityAdapter.P_BLOB_REFS);
    final String localBlobStoreId = "someBlobStoreId"; // TODO: Eventually use the actual local blob store id here
    checkState(blobRefs.containsKey(localBlobStoreId), "Local blobRef does not exist for that asset");
    BlobId blobId = new BlobId(blobRefs.get(localBlobStoreId));
    Blob blob = blobStore.get(new BlobId(blobRefs.get(localBlobStoreId)));
    checkState(blob != null, "Blob not found in local store: %", blobId.asUniqueString());
    return assetAdapter.convertToAsset(document, blob);
  }

  private static String oClassName(Class type) {
    return new OClassNameBuilder().type(type).build();
  }

  private static OrientQueryBuilder orientQueryBuilder(ODatabaseDocumentTx db, @Nullable MetadataQuery query) {
    if (query == null) {
      return new OrientQueryBuilder(new MetadataQuery());
    }
    else if (query.skipComponentId() == null) {
      return new OrientQueryBuilder(query);
    }
    else {
      ODocument skipDocument = null;
      if (query.skipAssetPath() != null) {
        skipDocument = getExistingDocumentWithId(db, AssetEntityAdapter.ORIENT_CLASS_NAME,
            AssetEntityAdapter.assetId(query.skipComponentId(), query.skipAssetPath()));
      }
      else {
        skipDocument = getExistingDocumentWithId(db, ComponentEntityAdapter.ORIENT_BASE_CLASS_NAME,
            query.skipComponentId().asUniqueString());
      }
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
    final String query = String.format("SELECT FROM %s WHERE id = ?", className);
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
