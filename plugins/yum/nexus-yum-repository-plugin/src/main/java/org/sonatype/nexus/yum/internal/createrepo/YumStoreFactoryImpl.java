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
package org.sonatype.nexus.yum.internal.createrepo;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Orient DB {@link YumStoreFactory} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class YumStoreFactoryImpl
    extends LifecycleSupport
    implements YumStoreFactory, EventSubscriber
{

  private final Provider<DatabaseInstance> databaseInstance;

  private final YumPackageEntityAdapter entityAdapter;

  @Inject
  public YumStoreFactoryImpl(final @Named("yum") Provider<DatabaseInstance> databaseInstance) {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.entityAdapter = new YumPackageEntityAdapter();
  }

  @Override
  public YumStore create(final String repositoryId) {
    return new YumStoreImpl(repositoryId);
  }

  @Subscribe
  public void on(final NexusInitializedEvent event) throws Exception {
    start();
  }

  @Subscribe
  public void on(final NexusStoppingEvent event) throws Exception {
    stop();
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      entityAdapter.register(db);
    }
  }

  private class YumStoreImpl
      implements YumStore
  {

    private String repositoryId;

    private YumStoreImpl(final String repositoryId) {
      this.repositoryId = repositoryId;
    }

    @Override
    public void put(final YumPackage yumPackage) {
      try (ODatabaseDocumentTx db = openDb()) {
        ODocument existing = entityAdapter.get(db, repositoryId, yumPackage.getLocation());
        if (existing == null) {
          entityAdapter.create(db, repositoryId, yumPackage);
        }
        else {
          entityAdapter.write(db, existing, repositoryId, yumPackage);
        }
      }
    }

    @Override
    public Iterable<YumPackage> get() {
      try (ODatabaseDocumentTx db = openDb()) {
        // NOTE: this will read all packages fact that can lead to high memory consumption, depending on the number of
        // packages in repository. To avoid this, we could get onl the RIDs from db and then use guava
        // Iterables.transform which will read the package from db on iterable next()
        List<YumPackage> packages = Lists.newArrayList();
        for (ODocument document : entityAdapter.get(db, repositoryId)) {
          packages.add(entityAdapter.read(document));
        }
        return packages;
      }
    }

    @Override
    public void delete(final String location) {
      try (ODatabaseDocumentTx db = openDb()) {
        entityAdapter.delete(db, repositoryId, location);
      }
    }

    @Override
    public void deleteAll() {
      try (ODatabaseDocumentTx db = openDb()) {
        entityAdapter.delete(db, repositoryId);
      }
    }

    private ODatabaseDocumentTx openDb() {
      ensureStarted();
      return databaseInstance.get().acquire();
    }
  }

}
