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
package org.sonatype.nexus.internal.security.realm;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.nexus.security.realm.RealmConfigurationStore;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Orient {@link RealmConfigurationStore}.
 *
 * @since 3.0
 */
@Named("orient")
@Singleton
public class OrientRealmConfigurationStore
  extends LifecycleSupport
  implements RealmConfigurationStore, EventSubscriber
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RealmConfigurationEntityAdapter entityAdapter;

  @Inject
  public OrientRealmConfigurationStore(final @Named("security") Provider<DatabaseInstance> databaseInstance,
                                       final RealmConfigurationEntityAdapter entityAdapter)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.entityAdapter = checkNotNull(entityAdapter);
  }

  @Override
  protected void doStart() {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      entityAdapter.register(db);
    }
  }

  @Subscribe
  public void on(final NexusInitializedEvent event) throws Exception {
    start();
  }

  @Subscribe
  public void on(final NexusStoppingEvent event) throws Exception {
    stop();
  }

  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  @Nullable
  private ODocument get(final ODatabaseDocumentTx db) {
    Iterable<ODocument> documents = entityAdapter.browse(db);
    if (documents.iterator().hasNext()) {
      return documents.iterator().next();
    }
    return null;
  }

  @Override
  @Nullable
  public RealmConfiguration load() {
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = get(db);
      if (doc != null) {
        return entityAdapter.read(doc);
      }
    }
    return null;
  }

  @Override
  public void save(final RealmConfiguration configuration) {
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = get(db);
      if (doc == null) {
        entityAdapter.create(db, configuration);
      }
      else {
        entityAdapter.write(doc, configuration);
      }
    }
  }
}
