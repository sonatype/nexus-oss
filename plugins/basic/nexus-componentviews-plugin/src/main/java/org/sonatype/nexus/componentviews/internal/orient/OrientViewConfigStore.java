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
package org.sonatype.nexus.componentviews.internal.orient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigId;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordMetadata;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link ViewConfigStore} that persists to the "config" OrientDb {@link DatabaseInstance}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class OrientViewConfigStore
    extends LifecycleSupport
    implements ViewConfigStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final ViewConfigEntityAdapter entityAdapter = new ViewConfigEntityAdapter();

  private final EventBus eventBus;

  private OClass entityType;

  @Inject
  public OrientViewConfigStore(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                               final RecordIdObfuscator recordIdObfuscator, final EventBus eventBus)
  {
    this.eventBus = eventBus;
    this.databaseInstance = checkNotNull(databaseInstance);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityType = entityAdapter.register(db);
    }
  }

  @Override
  protected void doStop() throws Exception {
    entityType = null;
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private ViewConfigId convertId(final ORID rid) {
    String encoded = recordIdObfuscator.encode(entityType, rid);
    return new ViewConfigId(encoded);
  }

  private ORID convertId(final ViewConfigId id) {
    return recordIdObfuscator.decode(entityType, id.toString());
  }

  @Override
  public ViewConfigId add(final ViewConfig item) throws IOException {
    ORID rid;
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.create(db, item);
      rid = doc.getIdentity();
    }

    log.debug("Added item with RID: {}", rid);
    return convertId(rid);
  }

  @Override
  public void update(final ViewConfigId id, final ViewConfig item) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // load record and apply updated item attributes
      ODocument doc = db.getRecord(rid);
      if (doc == null) {
        log.debug("Unable to update item with RID: {}", rid);
        throw new IllegalArgumentException("ViewConfigIdentity " + id + " is not present in the store.");
      }
      entityAdapter.write(doc, item);
    }

    log.debug("Updated item with RID: {}", rid);
  }

  @Override
  public void remove(final ViewConfigId id) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // if we can't get the metadata, then abort
      ORecordMetadata md = db.getRecordMetadata(rid);
      if (md == null) {
        log.debug("Unable to delete item with RID: {}", rid);
        return;
      }
      // else delete the record
      db.delete(rid);
    }

    log.debug("Deleted item with RID: {}", rid);
  }

  @Override
  public Map<ViewConfigId, ViewConfig> getAll() throws IOException {

    Map<ViewConfigId, ViewConfig> items = Maps.newHashMap();

    try (ODatabaseDocumentTx db = openDb()) {
      for (ODocument doc : entityAdapter.browse(db)) {
        ORID rid = doc.getIdentity();
        ViewConfig item = entityAdapter.read(doc);
        items.put(convertId(rid), item);
      }
    }

    return items;
  }

  @Override
  public ViewConfig get(final String viewName) {
    try (ODatabaseDocumentTx db = openDb()) {

      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + ViewConfigEntityAdapter.DB_CLASS + " WHERE viewName = ?");

      final List<ODocument> results = db.command(query).execute(viewName);

      if (results.isEmpty()) {
        return null;
      }

      return entityAdapter.read(results.get(0));
    }
  }
}
