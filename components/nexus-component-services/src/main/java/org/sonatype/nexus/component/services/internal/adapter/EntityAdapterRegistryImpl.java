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
package org.sonatype.nexus.component.services.internal.adapter;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.component.services.internal.storage.ComponentMetadataDatabase;
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
@Named
@Singleton
public class EntityAdapterRegistryImpl
    extends EntityAdapterSupport
    implements EntityAdapterRegistry
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final ConcurrentMap<String, EntityAdapter> registeredAdapters = Maps.newConcurrentMap();

  @Inject
  public EntityAdapterRegistryImpl(@Named(ComponentMetadataDatabase.NAME) Provider<DatabaseInstance> databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
  }

  @Override
  public void registerAdapter(final EntityAdapter adapter) {
    String className = checkNotNull(adapter).getClassName();
    checkState(registeredAdapters.putIfAbsent(className, adapter) == null,
        "Entity adapter already registered for class %s", className);
  }

  @Override
  public void unregisterAdapter(final String className) {
    registeredAdapters.remove(className);
  }

  @Nullable
  @Override
  public EntityAdapter getAdapter(final String className) {
    return initializeIfNeeded(registeredAdapters.get(className));
  }

  @Nullable
  private EntityAdapter initializeIfNeeded(EntityAdapter adapter) {
    if (adapter == null || adapter.isInitialized()) {
      return adapter;
    }
    synchronized (this) { // only allow schema changes on one thread at a time
      try (ODatabaseDocumentTx db = databaseInstance.get().acquire()) {
        adapter.getClass(db.getMetadata().getSchema());
      }
    }
    return adapter;
  }
}
