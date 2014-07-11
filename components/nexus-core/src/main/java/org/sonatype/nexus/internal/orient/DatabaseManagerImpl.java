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
    extends DatabaseManagerSupport
{
  public static final String WORK_PATH = "db";

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

  @Override
  protected String connectionUri(final String name) {
    try {
      File dir = directory(name);
      DirSupport.mkdir(dir);
      return "plocal:" + dir.getCanonicalPath();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Maybe import the database if there is an export file in the standard location.
   *
   * @see #EXPORT_FILENAME
   * @see #EXPORT_GZ_FILENAME
   */
  @Override
  protected void created(final ODatabaseDocumentTx db, final String name) throws Exception {
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
