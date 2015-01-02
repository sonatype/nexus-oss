/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.source.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.component.source.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigId;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordMetadata;

/**
 * A {@link ComponentSourceConfigStore} that persists source configs to the "config" orient DB instance.
 *
 * @since 3.0
 */
@Named("orient")
@Singleton
public class OrientComponentSourceConfigStore
    extends LifecycleSupport
    implements ComponentSourceConfigStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final ComponentSourceConfigEntityAdapter entityAdapter;

  private OClass entityType;

  @Inject
  public OrientComponentSourceConfigStore(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                                          final RecordIdObfuscator recordIdObfuscator,
                                          final ComponentSourceConfigEntityAdapter entityAdapter)
  {
    this.databaseInstance = databaseInstance;
    this.recordIdObfuscator = recordIdObfuscator;
    this.entityAdapter = entityAdapter;
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

  @Override
  public ComponentSourceId createId(final String name) {
    return new ComponentSourceId(name, UUID.randomUUID().toString());
  }

  @Override
  public ComponentSourceConfigId add(final ComponentSourceConfig config) throws IOException {
    ORID rid;
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.create(db, config);
      rid = doc.getIdentity();
    }

    log.debug("Added item with RID: {}", rid);
    return convertId(rid);
  }

  @Override
  public void update(final ComponentSourceConfigId id, final ComponentSourceConfig config) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // load record and apply updated item attributes
      ODocument doc = db.getRecord(rid);
      if (doc == null) {
        log.debug("Unable to update item with RID: {}", rid);
        throw new IllegalArgumentException(id + " is not present in the store.");
      }
      entityAdapter.write(doc, config);
    }

    log.debug("Updated item with RID: {}", rid);
  }

  @Override
  public void remove(final ComponentSourceConfigId id) throws IOException {
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
  public Map<ComponentSourceConfigId, ComponentSourceConfig> getAll() throws IOException {

    Map<ComponentSourceConfigId, ComponentSourceConfig> items = Maps.newHashMap();

    try (ODatabaseDocumentTx db = openDb()) {
      for (ODocument doc : entityAdapter.browse(db)) {
        ORID rid = doc.getIdentity();
        ComponentSourceConfig item = entityAdapter.read(doc);
        items.put(convertId(rid), item);
      }
    }

    return items;
  }

  @Nullable
  @Override
  public ComponentSourceConfig get(final String sourceName) throws IOException {
    try (ODatabaseDocumentTx db = openDb()) {

      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + ComponentSourceConfigEntityAdapter.DB_CLASS + " WHERE " +
              ComponentSourceConfigEntityAdapter.P_SOURCEID_NAME + " = ?");

      final List<ODocument> results = db.command(query).execute(sourceName);

      if (results.isEmpty()) {
        return null;
      }

      return entityAdapter.read(results.get(0));
    }
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private ComponentSourceConfigId convertId(final ORID rid) {
    String encoded = recordIdObfuscator.encode(entityType, rid);
    return new ComponentSourceConfigId(encoded);
  }

  private ORID convertId(final ComponentSourceConfigId id) {
    return recordIdObfuscator.decode(entityType, id.toString());
  }
}
