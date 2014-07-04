package org.sonatype.nexus.internal.orient;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;

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

  private final File databasesDirectory;

  @Inject
  public DatabaseManagerImpl(final ApplicationDirectories applicationDirectories) {
    checkNotNull(applicationDirectories);
    this.databasesDirectory = applicationDirectories.getWorkDirectory(WORK_PATH);
    log.debug("Databases directory: {}", databasesDirectory);
  }

  private String buildUri(final String name) {
    try {
      File dir = new File(databasesDirectory, name);
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

    String uri = buildUri(name);
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(uri);

    if (db.exists()) {
      db.open(SYSTEM_USER, SYSTEM_PASSWORD);
      log.debug("Opened database: {} -> {}", name, db);
    }
    else {
      if (create) {
        db.create();
        log.debug("Created database: {} -> {}", name, db);
      }
      else {
        log.debug("Database does not exist: {}", name);
      }
    }

    return db;
  }

  @Override
  public ODatabaseDocumentTx connect(final String name) {
    return connect(name, true);
  }

  @Override
  public ODatabaseDocumentPool pool(final String name) {
    checkNotNull(name);

    String uri = buildUri(name);
    ODatabaseDocumentPool pool = new ODatabaseDocumentPool(uri, SYSTEM_USER, SYSTEM_PASSWORD);
    pool.setName(String.format("%s-database-pool", name));
    pool.setup(1, 25);
    log.debug("Created database pool: {} -> {}", name, pool);

    return pool;
  }
}
