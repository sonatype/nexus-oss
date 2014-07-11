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
package org.sonatype.nexus.orient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * Orient database manager.
 *
 * @since 3.0
 */
public interface DatabaseManager
{
  ODatabaseDocumentTx connect(String name, boolean create);

  ODatabaseDocumentPool pool(String name);

  //
  // Backup and Restore
  //

  /**
   * Backup database.  Output format is a compressed ZIP file.
   *
   * @see #restore(String, InputStream)
   */
  void backup(String name, OutputStream output) throws IOException;

  /**
   * Restore database.
   *
   * @see #backup(String, OutputStream)
   */
  void restore(String name, InputStream input) throws IOException;

  //
  // Export and Import
  //

  /**
   * Standard file name inside of database export in instance-specific database directory.
   */
  String EXPORT_FILENAME = "export.json";

  /**
   * Compressed variant of {@link #EXPORT_FILENAME}.
   */
  String EXPORT_GZ_FILENAME = EXPORT_FILENAME + ".gz";

  /**
   * Export database.  Output format is a JSON file.
   *
   * @see #import_(String, InputStream)
   */
  void export(String name, OutputStream output) throws IOException;

  /**
   * Import database.
   *
   * @see #export(String, OutputStream)
   */
  void import_(String name, InputStream input) throws IOException;
}