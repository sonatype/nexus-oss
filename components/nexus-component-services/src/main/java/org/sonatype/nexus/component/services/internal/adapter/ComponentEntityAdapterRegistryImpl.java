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

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapterRegistry;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link ComponentEntityAdapterRegistry} implementation.
 *
 * @since 3.0
 */
public class ComponentEntityAdapterRegistryImpl
    extends EntityAdapterSupport
    implements ComponentEntityAdapterRegistry
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final ConcurrentMap<Class<? extends Component>, ComponentEntityAdapter> map = Maps.newConcurrentMap();

  @Inject
  public ComponentEntityAdapterRegistryImpl(@Named("componentMetadata") Provider<DatabaseInstance> databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
    registerCoreStorageClasses();
  }

  private void registerCoreStorageClasses() {
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      OSchema schema = db.getMetadata().getSchema();
      // Component
      if (!schema.existsClass(ComponentEntityAdapter.ORIENT_BASE_CLASS_NAME)) {
        OClass oClass = schema.createAbstractClass(ComponentEntityAdapter.ORIENT_BASE_CLASS_NAME);
        createRequiredAutoIndexedProperty(oClass, ComponentEntityAdapter.P_ID, OType.STRING, true);
        createRequiredProperty(oClass, ComponentEntityAdapter.P_ASSETS, OType.LINKSET);
        logCreatedClassInfo(oClass);
      }
      // Asset
      new AssetEntityAdapter().registerStorageClass(db);
    }
  }

  @Override
  public <T extends Component> void registerAdapter(final ComponentEntityAdapter<T> adapter) {
    checkState(map.putIfAbsent(adapter.getComponentClass(), adapter) == null,
        "Adapter already registered for class %s", adapter.getComponentClass());
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      adapter.registerStorageClass(db);
    }
  }

  @Override
  public <T extends Component> void unregisterAdapter(final Class<T> componentClass) {
    map.remove(componentClass);
  }

  @Nullable
  @Override
  @SuppressWarnings({"unchecked"})
  public <T extends Component> ComponentEntityAdapter<T> getAdapter(final Class<T> componentClass) {
    return map.get(componentClass);
  }

  @Override
  public Set<Class<? extends Component>> componentClasses() {
    return map.keySet();
  }
}
