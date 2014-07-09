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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link DatabaseManager} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DatabaseManagerImpl
    extends ComponentSupport
    implements DatabaseManager
{
  public static final String WORK_PATH = "db";

  public static final String SYSTEM_USER = "admin";

  public static final String SYSTEM_PASSWORD = "admin";

  public static final int BACKUP_BUFFER_SIZE = 16 * 1024;

  public static final int IMPORT_BUFFER_SIZE = 16 * 1024;

  public static final int BACKUP_COMPRESSION_LEVEL = 9;

  private final File databasesDirectory;

  @Inject
  public DatabaseManagerImpl(final ApplicationDirectories applicationDirectories) {
    checkNotNull(applicationDirectories);
    this.databasesDirectory = applicationDirectories.getWorkDirectory(WORK_PATH);
    log.debug("Databases directory: {}", databasesDirectory);
  }

  @VisibleForTesting
  public DatabaseManagerImpl(final File databasesDirectory) {
    this.databasesDirectory = checkNotNull(databasesDirectory);
    log.debug("Databases directory: {}", databasesDirectory);
  }

  /**
   * Returns the directory for the given named database.  Directory may or may not exist.
   */
  private File directory(final String name) {
    return new File(databasesDirectory, name);
  }

  /**
   * Build connection URI for given named database.  This will create the database directory if needed.
   */
  private String connectionUri(final String name) {
    try {
      File dir = directory(name);
      DirSupport.mkdir(dir);
      return "plocal:" + dir.getCanonicalPath();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

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

        // Attempt to import previously exported database
        try {
          maybeImport(name, db);
        }
        catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
      else {
        log.debug("Database does not exist: {}", name);
      }
    }

    return db;
  }

  @Override
  public ODatabaseDocumentPool pool(final String name) {
    checkNotNull(name);

    String uri = connectionUri(name);
    ODatabaseDocumentPool pool = new ODatabaseDocumentPool(uri, SYSTEM_USER, SYSTEM_PASSWORD);
    pool.setName(String.format("%s-database-pool", name));
    pool.setup(1, 25);
    log.debug("Created database pool: {} -> {}", name, pool);

    return pool;
  }

  /**
   * Helper to log prefixed command output messages.
   */
  private class LoggingCommandOutputListener
      implements OCommandOutputListener
  {
    private final String prefix;

    private LoggingCommandOutputListener(final String prefix) {
      this.prefix = prefix;
    }

    @Override
    public void onMessage(final String text) {
      if (log.isDebugEnabled()) {
        log.debug("{}: {}", prefix, text.trim());
      }
    }
  }

  @Override
  public void backup(final String name, final OutputStream output) throws IOException {
    checkNotNull(name);
    checkNotNull(output);

    log.debug("Backup database: {}", name);

    try (ODatabaseDocumentTx db = connect(name, false)) {
      checkState(db.exists(), "Database does not exist: %s", name);

      log.debug("Starting backup");
      db.backup(output, null, null, new LoggingCommandOutputListener("BACKUP"),
          BACKUP_COMPRESSION_LEVEL, BACKUP_BUFFER_SIZE);
      log.debug("Completed backup");
    }
  }

  @Override
  public void restore(final String name, final InputStream input) throws IOException {
    checkNotNull(name);
    checkNotNull(input);

    log.debug("Restoring database: {}", name);

    try (ODatabaseDocumentTx db = connect(name, false)) {
      checkState(!db.exists(), "Database already exists: %s", name);
      db.create();

      log.debug("Starting restore");
      db.restore(input, null, null, new LoggingCommandOutputListener("RESTORE"));
      log.debug("Completed import");
    }
  }

  @Override
  public void export(final String name, final OutputStream output) throws IOException {
    checkNotNull(name);
    checkNotNull(output);

    log.debug("Exporting database: {}", name);

    try (ODatabaseDocumentTx db = connect(name, false)) {
      checkState(db.exists(), "Database does not exist: %s", name);

      log.debug("Starting export");
      ODatabaseExport exporter = new ODatabaseExport(db, output, new LoggingCommandOutputListener("EXPORT"));
      exporter.exportDatabase();
      log.debug("Completed export");
    }
  }

  @Override
  public void import_(final String name, final InputStream input) throws IOException {
    checkNotNull(name);
    checkNotNull(input);

    log.debug("Importing database: {}", name);

    try (ODatabaseDocumentTx db = connect(name, false)) {
      checkState(!db.exists(), "Database already exists: %s", name);
      db.create();

      import_(db, input);
    }
  }

  private void import_(final ODatabaseDocumentTx db, final InputStream input) throws IOException {
    checkNotNull(input);

    log.debug("Starting import");
    ODatabaseImport importer = new ODatabaseImport(db, input, new LoggingCommandOutputListener("IMPORT"));
    importer.importDatabase();
    log.debug("Completed import");
  }

  /**
   * Maybe import the database if there is an export file in the standard location.
   *
   * @see #EXPORT_FILENAME
   * @see #EXPORT_GZ_FILENAME
   */
  private void maybeImport(final String name, final ODatabaseDocumentTx db) throws IOException {
    InputStream input = null;

    File dir = directory(name);
    File file = new File(dir, EXPORT_FILENAME);
    if (file.exists()) {
      input = new BufferedInputStream(new FileInputStream(file), IMPORT_BUFFER_SIZE);
    }
    else {
      file = new File(dir, EXPORT_GZ_FILENAME);
      if (file.exists()) {
        input = new GZIPInputStream(new FileInputStream(file), IMPORT_BUFFER_SIZE);
      }
    }

    if (input != null) {
      log.debug("Importing database: {} from: {}", name, file);

      try {
        import_(db, input);
      }
      finally {
        input.close();
      }

      // TODO: Rename file now that its processed?  Or maybe delete it?
    }
  }
}
