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
package org.sonatype.nexus.component.services.internal.adapter;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.component.services.internal.storage.ComponentMetadataDatabase;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OSchema;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link EntityAdapterRegistry} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class EntityAdapterRegistryImpl
    extends EntityAdapterSupport
    implements EntityAdapterRegistry
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final ConcurrentMap<Class<? extends Asset>, AssetAdapter<? extends Asset>>
      assetAdapters = Maps.newConcurrentMap();

  private final ConcurrentMap<Class<? extends Component>, ComponentAdapter<? extends Component>>
      componentAdapters = Maps.newConcurrentMap();

  @Inject
  public EntityAdapterRegistryImpl(@Named(ComponentMetadataDatabase.NAME) Provider<DatabaseInstance> databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
  }

  @Override
  public Set<Class<? extends Asset>> assetClasses() {
    return assetAdapters.keySet();
  }

  @Override
  public Set<Class<? extends Component>> componentClasses() {
    return componentAdapters.keySet();
  }

  @Override
  public <T extends Asset> void registerAssetAdapter(final AssetAdapter<T> adapter) {
    Class<T> entityClass = checkNotNull(adapter).getEntityClass();
    checkState(assetAdapters.putIfAbsent(entityClass, adapter) == null,
        "Asset adapter already registered for class %s", adapter.getEntityClass());
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      OSchema schema = db.getMetadata().getSchema();
      adapter.createStorageClass(schema);
    }
  }

  @Override
  public <T extends Component> void registerComponentAdapter(final ComponentAdapter<T> adapter) {
    Class<T> entityClass = checkNotNull(adapter).getEntityClass();
    checkState(componentAdapters.putIfAbsent(entityClass, adapter) == null,
        "Component adapter already registered for class %s", adapter.getEntityClass());
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      OSchema schema = db.getMetadata().getSchema();
      adapter.createStorageClass(schema);
    }
  }

  @Override
  public <T extends Asset> void unregisterAssetAdapter(final Class<T> entityClass) {
    assetAdapters.remove(entityClass);
  }

  @Override
  public <T extends Component> void unregisterComponentAdapter(final Class<T> entityClass) {
    componentAdapters.remove(entityClass);
  }

  @Nullable
  @Override
  @SuppressWarnings({"unchecked"})
  public <T extends Asset> AssetAdapter<T> getAssetAdapter(final Class<T> entityClass) {
    return (AssetAdapter<T>) assetAdapters.get(entityClass);
  }

  @Nullable
  @Override
  @SuppressWarnings({"unchecked"})
  public <T extends Component> ComponentAdapter<T> getComponentAdapter(final Class<T> entityClass) {
    return (ComponentAdapter<T>) componentAdapters.get(entityClass);
  }
}
