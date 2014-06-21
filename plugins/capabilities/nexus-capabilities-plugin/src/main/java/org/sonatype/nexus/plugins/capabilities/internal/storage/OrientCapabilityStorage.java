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
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.util.Tokens;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.Key;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.keyvalue.KeyValueStoreIteration.SortDirection;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.schema.TypeValidation;
import io.kazuki.v0.store.schema.model.Attribute.Type;
import io.kazuki.v0.store.schema.model.Schema;
import org.apache.shiro.codec.Hex;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
      }

      // register entities
      db.setAutomaticSchemaGeneration(true);
      db.getEntityManager().registerEntityClass(CapabilityStorageItem.class);
    }

    this.databasePool = new OObjectDatabasePool(databaseUri, "admin", "admin");
    databasePool.setName("capability-database-pool");
    databasePool.setup(1,10);
    log.info("Created pool: {}", databasePool);
  }

  @Override
  protected void doStop() throws Exception {
    databasePool.close();
    databasePool = null;
  }

  private OObjectDatabaseTx openDb() {
    ensureStarted();
    return databasePool.acquire();
  }

  // TODO: Sort out Hex encoding, perhaps use commons-codec?

  private CapabilityIdentity convert(final ORID rid) {
    String encoded = Hex.encodeToString(rid.toStream());
    return new CapabilityIdentity(encoded);
  }

  private ORID convert(final CapabilityIdentity id) {
    byte[] decoded = Hex.decode(id.toString());
    return new ORecordId().fromStream(decoded);
  }

  @Override
  public CapabilityIdentity add(final CapabilityStorageItem item) throws IOException {
    try (OObjectDatabaseTx db = openDb()) {
      ODocument doc = db.save(item);
      return convert(doc.getIdentity());
    }
  }

  @Override
  public boolean update(final CapabilityIdentity id, final CapabilityStorageItem item) throws IOException {
    ORID rid = convert(id);

    try (OObjectDatabaseTx db = openDb()) {
      // ignore if there is no record for the given id
      if (!db.existsUserObjectByRID(rid)) {
        return false;
      }

      // load record and apply updated item attributes
      CapabilityStorageItem record = db.load(rid);
      record.setType(item.getType());
      record.setVersion(item.getVersion());
      record.setEnabled(item.isEnabled());
      record.setNotes(item.getNotes());
      record.setProperties(item.getProperties());

      db.save(record);
      return true;
    }
  }

  @Override
  public boolean remove(final CapabilityIdentity id) throws IOException {
    ORID rid = convert(id);

    try (OObjectDatabaseTx db = openDb()) {
      // ignore if there is no record for the given id
      if (!db.existsUserObjectByRID(rid)) {
        return false;
      }

      db.delete(convert(id));
      return true;
    }
  }

  @Override
  public Map<CapabilityIdentity, CapabilityStorageItem> getAll() throws IOException {
    Map<CapabilityIdentity, CapabilityStorageItem> items = Maps.newHashMap();
    try (OObjectDatabaseTx db = openDb()) {
      for (CapabilityStorageItem item : db.browseClass(CapabilityStorageItem.class)) {
        ORID rid = db.getIdentity(item);
        assert rid != null;
        item = db.detach(item, true);
        items.put(convert(rid), item);
      }
    }
    return items;
  }
}
