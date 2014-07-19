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

package org.sonatype.nexus.internal.orient;

import java.util.Iterator;
import java.util.Map;

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.orient.DatabasePool;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Support for {@link DatabaseManager} implementations.
 *
 * @since 3.0
 */
public abstract class DatabaseManagerSupport
    extends LifecycleSupport
    implements DatabaseManager
{
  public static final String SYSTEM_USER = "admin";

  public static final String SYSTEM_PASSWORD = "admin";

  private final Map<String,DatabasePoolImpl> pools = Maps.newHashMap();

  private final Map<String,DatabaseInstanceImpl> instances = Maps.newHashMap();

  @Override
  protected void doStart() throws Exception {
    checkState(pools.isEmpty());
    checkState(instances.isEmpty());

    // HACK:
    log.info("Started: {}", this);
  }

  @Override
  protected void doStop() throws Exception {
    // NOTE: Lifecycle lock and use of ensureStarted avoids need to synchronize around collections.

    // stop all instances
    if (!instances.isEmpty()) {
      log.info("Stopping {} instances", instances.size());

      Iterator<DatabaseInstanceImpl> iter = instances.values().iterator();
      while (iter.hasNext()) {
        DatabaseInstanceImpl instance = iter.next();

        log.info("Stopping instance: {}", instance);
        try {
          // TODO:
          //instance.stop();
        }
        catch (Exception e) {
          log.warn("Failed to stop database instance: {}", instance, e);
        }
        iter.remove();
      }
    }

    // close all pools
    if (!pools.isEmpty()) {
      log.info("Closing {} pools", pools.size());

      Iterator<DatabasePoolImpl> iter = pools.values().iterator();
      while (iter.hasNext()) {
        DatabasePoolImpl pool = iter.next();

        log.info("Closing pool: {}", pool);
        try {
          pool.close();
        }
        catch (Exception e) {
          log.warn("Failed to close database pool: {}", pool, e);
        }
        iter.remove();
      }
    }

    // HACK:
    log.info("Stopped: {}", this);
  }

  protected abstract String connectionUri(final String name);

  @Override
  public ODatabaseDocumentTx connect(final String name, final boolean create) {
    checkNotNull(name);
    ensureStarted();

    String uri = connectionUri(name);
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(uri);

    if (db.exists()) {
      db.open(SYSTEM_USER, SYSTEM_PASSWORD);
      log.debug("Opened database: {} -> {}", name, db);
    }
    else {
      if (create) {
        db.create();
        log.debug("Created database: {} -> {}", name, db);

        // invoke created callback
        try {
          created(db, name);
        }
        catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
      else {
        log.debug("Database does not exist: {}", name);
      }
    }

    return db;
  }

  /**
   * Callback invoked when database is being created.
   */
  protected void created(final ODatabaseDocumentTx db, final String name) throws Exception {
    // nop
  }

  @Override
  public DatabasePool pool(final String name) {
    checkNotNull(name);
    ensureStarted();

    synchronized (pools) {
      DatabasePoolImpl pool = pools.get(name);
      if (pool == null) {
        // TODO: refine more control over how pool settings are configured per-database or globally

        String uri = connectionUri(name);
        ODatabaseDocumentPool underlying = new ODatabaseDocumentPool(uri, SYSTEM_USER, SYSTEM_PASSWORD);
        underlying.setName(String.format("%s-database-pool", name));
        underlying.setup(1, 25);

        pool = new DatabasePoolImpl(underlying, name);
        log.debug("Created database pool: {}", pool);

        // TODO: pool.start()

        pools.put(name, pool);
      }
      return pool;
    }
  }

  /**
   * Exposing impl type here for sub-classes.
   */
  @Override
  public DatabaseExternalizerImpl externalizer(final String name) {
    checkNotNull(name);
    ensureStarted();

    return new DatabaseExternalizerImpl(this, name);
  }

  @Override
  public DatabaseInstance instance(final String name) {
    checkNotNull(name);
    ensureStarted();

    synchronized (instances) {
      DatabaseInstanceImpl instance = instances.get(name);
      if (instance == null) {
        instance = new DatabaseInstanceImpl(this, name);
        log.debug("Created database instance: {}", instance);

        // TODO: instance.start()

        instances.put(name, instance);
      }
      return instance;
    }
  }
}
