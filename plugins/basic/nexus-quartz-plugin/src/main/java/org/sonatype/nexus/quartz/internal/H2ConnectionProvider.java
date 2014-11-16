/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.quartz.internal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.h2.Driver;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.util.JdbcUtils;
import org.quartz.utils.ConnectionProvider;

import static com.google.common.base.Preconditions.checkState;

/**
 * H2-based {@link ConnectionProvider}.
 */
@Singleton
@Named
public class H2ConnectionProvider
    extends ComponentSupport
    implements ConnectionProvider
{
  private static final String H2_SETTINGS =
      "${nexus-quartz-h2-settings:-;FILE_LOCK=SOCKET;QUERY_CACHE_SIZE=0;CACHE_SIZE=0;CACHE_TYPE=SOFT_LRU;MULTI_THREADED=1;LOCK_TIMEOUT=60000;MAX_MEMORY_ROWS=1000000}";

  private static final String DATABASE_NAME = "quartz";

  private final ApplicationDirectories cfg;

  private final String settings;

  private JdbcConnectionPool pool;

  @Inject
  public H2ConnectionProvider(final ApplicationDirectories cfg, @Named(H2_SETTINGS) final String settings)
  {
    this.cfg = cfg;
    this.settings = settings;
  }

  @Override
  public Connection getConnection() throws SQLException {
    checkState(pool != null, "Pool not initialized!");
    return pool.getConnection();
  }

  @Override
  public void initialize() throws SQLException {
    if (null != pool) {
      return; // already started
    }
    final File workDir = cfg.getWorkDirectory("db").getAbsoluteFile();
    final String database = workDir + File.separator + DATABASE_NAME + File.separator + DATABASE_NAME;
    final File databaseFile = new File(database + ".h2.db");
    try {
      try {
        pool = open(database, settings);
      }
      catch (final SQLException e) {
        if (Strings.nullToEmpty(e.getMessage()).contains("Locked")) {
          log.warn("Database locked, is another instance of Nexus running? ({})", e.getMessage());
          final File backup = new File(workDir, DATABASE_NAME + ".backup." + System.currentTimeMillis());
          if (databaseFile.exists()) {
            log.info("Backing up locked database to {} to limit potential corruption", backup);
            Files.move(databaseFile, backup);
            Files.copy(backup, databaseFile);
          }
          Files.move(new File(database + ".lock.db"), new File(backup.getPath() + ".lock"));
          pool = open(database, settings);
        }
        else {
          throw e; // not locked, re-throw as DB is probably corrupt...
        }
      }
    }
    catch (final SQLException e) {
      log.warn("Database corrupt, starting with empty Quartz DB ({})", e.getMessage());
      try {
        final File backup = new File(workDir, DATABASE_NAME + ".corrupt." + System.currentTimeMillis());
        log.info("Moving corrupt database to {}", backup);
        Files.move(databaseFile, backup);
        pool = open(database, settings);
      }
      catch (final Exception ignore) {
        throw new SQLException("Problem rebooting Quartz DB", e);
      }
    }
    catch (final Exception e) {
      throw new SQLException("Problem starting Quartz DB", e);
    }
  }

  @Override
  public void shutdown() throws SQLException {
    if (null != pool) {
      try {
        pool.dispose();
      }
      finally {
        pool = null;
        Driver.unload();
      }
    }
  }

  private static JdbcConnectionPool open(final String database, final String settings)
      throws Exception
  {
    JdbcConnectionPool pool = null;
    try {
      Driver.load();
      final String url = "jdbc:h2:" + database + (settings.startsWith(";") ? settings : ';' + settings);
      Connection conn = null;
      try {
        // eagerly test the connection
        conn = DriverManager.getConnection(url, "sa", "");
      }
      finally {
        JdbcUtils.closeSilently(conn);
      }
      // connection checks out, so now create lazy pool
      pool = JdbcConnectionPool.create(url, "sa", "");
      pool.setMaxConnections(8);
      return pool;
    }
    catch (final Exception e) {
      if (null != pool) {
        pool.dispose();
      }
      throw e;
    }
  }
}