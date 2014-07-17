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

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.orient.DatabasePool;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link DatabaseManager} implementations.
 *
 * @since 3.0
 */
public abstract class DatabaseManagerSupport
    extends ComponentSupport
    implements DatabaseManager
{
  public static final String SYSTEM_USER = "admin";

  public static final String SYSTEM_PASSWORD = "admin";

  protected abstract String connectionUri(final String name);

  @Override
  public ODatabaseDocumentTx connect(final String name, final boolean create) {
    checkNotNull(name);

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

    // TODO: refine more control over how pool settings are configured per-database or globally

    String uri = connectionUri(name);
    final ODatabaseDocumentPool pool = new ODatabaseDocumentPool(uri, SYSTEM_USER, SYSTEM_PASSWORD);
    pool.setName(String.format("%s-database-pool", name));
    pool.setup(1, 25);
    log.debug("Created database pool: {} -> {}", name, pool);

    return new DatabasePool()
    {
      @Override
      public ODatabaseDocumentTx acquire() {
        return pool.acquire();
      }

      @Override
      public void close() {
        pool.close();
      }

      @Override
      public String toString() {
        return pool.toString();
      }
    };
  }

  @Override
  public DatabaseInstance instance(final String name) {
    checkNotNull(name);
    // TODO:
    return null;
  }

  // NOTE: Exposing impl type here for sub-classes

  @Override
  public DatabaseExternalizerImpl externalizer(final String name) {
    checkNotNull(name);
    return new DatabaseExternalizerImpl(this, name);
  }
}
