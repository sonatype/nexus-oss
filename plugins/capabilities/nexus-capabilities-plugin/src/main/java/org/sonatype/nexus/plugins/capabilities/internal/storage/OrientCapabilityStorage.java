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
package org.sonatype.nexus.plugins.capabilities.internal.storage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OrientDB implementation of {@link CapabilityStorage}.
 *
 * @since 3.0
 */
@Named("orient")
@Singleton
public class OrientCapabilityStorage
    extends LifecycleSupport
    implements CapabilityStorage
{
  public static final String DB_LOCATION = "db/capability";

  private final File databaseDirectory;

  private OObjectDatabasePool databasePool;

  @Inject
  public OrientCapabilityStorage(final ApplicationDirectories applicationDirectories) {
    checkNotNull(applicationDirectories);
    this.databaseDirectory = applicationDirectories.getWorkDirectory(DB_LOCATION);
    log.info("Database directory: {}", databaseDirectory);
  }

  @Override
  protected void doStart() throws Exception {
    String databaseUri = "plocal:" + databaseDirectory;

    // create database and register entities
    try (OObjectDatabaseTx db = new OObjectDatabaseTx(databaseUri)) {
      if (!db.exists()) {
        db.create();
        log.info("Created database: {}", db);
      }
      else {
        db.open("admin", "admin");
        log.info("Opened database: {}", db);
      }

      // register entities
      db.setAutomaticSchemaGeneration(true);
      db.getEntityManager().registerEntityClass(CapabilityStorageItem.class);
    }

    this.databasePool = new OObjectDatabasePool(databaseUri, "admin", "admin");
    databasePool.setName("capability-database-pool");
    databasePool.setup(1, 10);
    log.info("Created pool: {}", databasePool);
  }

  @Override
  protected void doStop() throws Exception {
    databasePool.close();
    databasePool = null;
  }

  /**
   * Open a database connection using the pool.
   */
  private OObjectDatabaseTx openDb() {
    ensureStarted();
    return databasePool.acquire();
  }

  private CapabilityIdentity convert(final ORID rid) {
    String encoded = RecordIdObfuscator.encode(rid);
    return new CapabilityIdentity(encoded);
  }

  private ORID convert(final CapabilityIdentity id) {
    return RecordIdObfuscator.decode(id.toString());
  }

  @Override
  public CapabilityIdentity add(final CapabilityStorageItem item) throws IOException {
    ORID rid;
    try (OObjectDatabaseTx db = openDb()) {
      CapabilityStorageItem record = db.save(item);
      rid = db.getIdentity(record);
    }

    log.debug("Added item with RID: {}", rid);
    return convert(rid);
  }

  @Override
  public boolean update(final CapabilityIdentity id, final CapabilityStorageItem item) throws IOException {
    ORID rid = convert(id);

    try (OObjectDatabaseTx db = openDb()) {
      // load record and apply updated item attributes
      ODocument record = db.getUnderlying().getRecord(rid);
      if (record == null) {
        log.debug("Unable to update item with RID: {}", rid);
        return false;
      }
      db.pojo2Stream(item, record).save();
    }

    log.debug("Updated item with RID: {}", rid);
    return true;
  }

  @Override
  public boolean remove(final CapabilityIdentity id) throws IOException {
    ORID rid = convert(id);

    try (OObjectDatabaseTx db = openDb()) {
      // if we can't get the metadata, then abort
      ORecordMetadata md = db.getRecordMetadata(rid);
      if (md == null) {
        log.debug("Unable to delete item with RID: {}", rid);
        return false;
      }
      // else delete the record
      db.delete(rid);
    }

    log.debug("Deleted item with RID: {}", rid);
    return true;
  }

  @Override
  public Map<CapabilityIdentity, CapabilityStorageItem> getAll() throws IOException {
    Map<CapabilityIdentity, CapabilityStorageItem> items = Maps.newHashMap();

    try (OObjectDatabaseTx db = openDb()) {
      for (CapabilityStorageItem item : db.browseClass(CapabilityStorageItem.class)) {
        ORID rid = db.getIdentity(item);
        item = db.detach(item, true);
        items.put(convert(rid), item);
      }
    }

    return items;
  }
}
