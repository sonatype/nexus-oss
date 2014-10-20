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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.component.assetstore.AssetStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.query.MetadataQueryService;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link MetadataQueryService} implementation.
 *
 * @since 3.0
 */
public class MetadataQueryServiceImpl
    implements MetadataQueryService
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final AssetStore assetStore;

  private final ComponentEntityAdapterRegistry adapterRegistry;

  @Inject
  public MetadataQueryServiceImpl(@Named("metadata") Provider<DatabaseInstance> databaseInstance,
                                  AssetStore assetStore,
                                  ComponentEntityAdapterRegistry adapterRegistry) {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.assetStore = checkNotNull(assetStore);
    this.adapterRegistry = checkNotNull(adapterRegistry);
  }

  @Override
  public <T extends Component> long countComponents(final Class<T> componentClass,
                                                    final @Nullable MetadataQueryRestriction restriction) {
    ComponentEntityAdapter<T> mapper = registeredAdapter(componentClass);
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      String query = orientQueryBuilder(db, queryWith(restriction)).buildComponentQuery(oClassName(componentClass), true);
      // TODO: execute and return count
    }
    return 0;
  }

  @Override
  public long countAssets(final @Nullable MetadataQueryRestriction restriction) {
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      String query = orientQueryBuilder(db, queryWith(restriction)).buildAssetQuery(true);
      // TODO: execute and return count
    }
    return 0;
  }

  @Override
  public <T extends Component> List<T> findComponents(final Class<T> componentClass,
                                                      final MetadataQuery metadataQuery) {
    ComponentEntityAdapter<T> mapper = registeredAdapter(componentClass);
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      String query = orientQueryBuilder(db, metadataQuery).buildComponentQuery(oClassName(componentClass), false);
      // TODO: execute and convert results to Component domain objects using mapper
    }
    return null;
  }

  @Override
  public List<Asset> findAssets(final MetadataQuery metadataQuery) {
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      String query = orientQueryBuilder(db, metadataQuery).buildAssetQuery(false);
      // TODO: execute and convert results to Asset domain objects
    }
    return null;
  }

  private <T extends Component> ComponentEntityAdapter<T> registeredAdapter(final Class<T> componentClass) {
    ComponentEntityAdapter<T> mapper = adapterRegistry.getAdapter(componentClass);
    checkState(mapper != null, "No adapter registered for class %", componentClass);
    return mapper;
  }

  private String oClassName(Class type) {
    return new OClassNameBuilder().prefix("metadata").type(type).build();
  }

  private OrientQueryBuilder orientQueryBuilder(ODatabaseDocumentTx db, MetadataQuery query) {
    if (query.getSkipComponentId() == null) {
      return new OrientQueryBuilder(query);
    } else if (query.getSkipAssetPath() == null) {
      return new OrientQueryBuilder(query, skipComponentRid(db, query.getSkipComponentId()));
    } else {
      return new OrientQueryBuilder(query, skipAssetRid(db, query.getSkipComponentId(), query.getSkipAssetPath()));
    }
  }

  private ORID skipComponentRid(ODatabaseDocumentTx db, ComponentId skipComponentId) {
    // TODO: find orid of component where id = skipComponentId
    return null;
  }

  private ORID skipAssetRid(ODatabaseDocumentTx db, ComponentId skipComponentId, @Nullable String skipAssetPath) {
    // TODO: get orid of asset where id = skipComponentId:skipAssetPath
    return null;
  }

  private static MetadataQuery queryWith(@Nullable MetadataQueryRestriction restriction) {
    if (restriction == null) {
      return new MetadataQuery();
    } else {
      return new MetadataQuery().withRestriction(restriction);
    }
  }
}
