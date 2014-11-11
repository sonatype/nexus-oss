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

import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link EntityAdapterRegistry} implementation.
 *
 * @since 3.0
 */
public class EntityAdapterRegistryImpl
    implements EntityAdapterRegistry
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final ConcurrentMap<Class<? extends Entity>, EntityAdapter> map = Maps.newConcurrentMap();

  @Inject
  public EntityAdapterRegistryImpl(@Named("componentMetadata") Provider<DatabaseInstance> databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
  }

  @Override
  public <T extends Entity> void registerAdapter(final EntityAdapter<T> adapter) {
    checkState(map.putIfAbsent(adapter.getEntityClass(), adapter) == null,
        "Adapter already registered for class %s", adapter.getEntityClass());
    try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
      adapter.registerStorageClass(db);
    }
  }

  @Override
  public <T extends Entity> void unregisterAdapter(final Class<T> entityClass) {
    map.remove(entityClass);
  }

  @Nullable
  @Override
  @SuppressWarnings({"unchecked"})
  public <T extends Entity> EntityAdapter<T> getAdapter(final Class<T> entityClass) {
    return map.get(entityClass);
  }

  @Override
  public Set<Class<? extends Entity>> entityClasses() {
    return map.keySet();
  }
}
