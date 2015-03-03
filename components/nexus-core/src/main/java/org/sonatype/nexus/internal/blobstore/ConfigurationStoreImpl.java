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
package org.sonatype.nexus.internal.blobstore;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.configuration.BlobStoreConfiguration;
import org.sonatype.nexus.blobstore.api.configuration.ConfigurationStore;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * @since 3.0
 */
@Named
@Singleton
public class ConfigurationStoreImpl
    extends StateGuardLifecycleSupport
    implements ConfigurationStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final ConfigurationEntityAdapter entityAdapter;

  @Inject
  public ConfigurationStoreImpl(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                                final ConfigurationEntityAdapter entityAdapter)
  {
    this.databaseInstance = databaseInstance;
    this.entityAdapter = entityAdapter;
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      entityAdapter.register(db);
    }
  }

  private ODatabaseDocumentTx openDb() {
    return databaseInstance.get().acquire();
  }

  @Override
  @Guarded(by = STARTED)
  public List<BlobStoreConfiguration> list() {
    return asConfig(internalList());
  }

  @Override
  public BlobStoreConfiguration newInstance() {
    return entityAdapter.newEntity();
  }

  @Override
  @Guarded(by = STARTED)
  public void create(final BlobStoreConfiguration configuration) {
    checkNotNull(configuration);

    try (ODatabaseDocumentTx db = openDb()) {
      entityAdapter.add(db, new BlobStoreConfigurationEntity(configuration.getName(), configuration.getPath()));
    }
  }
  
  private BlobStoreConfigurationEntity find(final BlobStoreConfiguration configuration) {
    return Iterables.find(internalList(), new Predicate<BlobStoreConfigurationEntity>()
    {
      @Override
      public boolean apply(@Nullable final BlobStoreConfigurationEntity input) {
        return input.getName().equals(configuration.getName());
      }
    });
  }

  @Override
  @Guarded(by = STARTED)
  public void update(final BlobStoreConfiguration configuration) {
    checkNotNull(configuration);
    BlobStoreConfigurationEntity blobStoreConfigurationEntity = find(configuration);
    blobStoreConfigurationEntity.setPath(configuration.getPath());
    try (ODatabaseDocumentTx db = openDb()) {
      entityAdapter.edit(db, blobStoreConfigurationEntity);
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void delete(final BlobStoreConfiguration configuration) {
    checkNotNull(configuration);

    try (ODatabaseDocumentTx db = openDb()) {
      entityAdapter.delete(db, find(configuration));
    }
  }

  private List<BlobStoreConfigurationEntity> internalList() {
    try (ODatabaseDocumentTx db = openDb()) {
      return entityAdapter.browse(db, Lists.<BlobStoreConfigurationEntity>newArrayList());
    }
  }

  private List<BlobStoreConfiguration> asConfig(final List<BlobStoreConfigurationEntity> browse) {
    return Lists.transform(browse, new Function<BlobStoreConfigurationEntity, BlobStoreConfiguration>()
    {
      @Nullable
      @Override
      public BlobStoreConfiguration apply(final BlobStoreConfigurationEntity input) {
        return input;
      }
    });
  }
}
