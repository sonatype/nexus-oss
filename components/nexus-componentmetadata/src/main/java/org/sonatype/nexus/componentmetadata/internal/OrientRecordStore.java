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
package org.sonatype.nexus.componentmetadata.internal;

import javax.inject.Inject;

import org.sonatype.nexus.componentmetadata.RecordStore;
import org.sonatype.nexus.componentmetadata.RecordStoreSession;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.orient.DatabasePool;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OrientDB implementation of {@link RecordStore}.
 *
 * @since 3.0
 */
public class OrientRecordStore
    extends LifecycleSupport
    implements RecordStore
{
  public static final String DB_NAME = "componentmd";

  private final DatabaseManager databaseManager;

  private final RecordIdObfuscator recordIdObfuscator;

  private DatabasePool databasePool;

  @Inject
  public OrientRecordStore(final DatabaseManager databaseManager,
                           final RecordIdObfuscator recordIdObfuscator) {
    this.databaseManager = checkNotNull(databaseManager);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  @Override
  protected void doStart() {
    // make sure the db exists and init connection pool
    databaseManager.connect(DB_NAME, true).close();
    databasePool = databaseManager.pool(DB_NAME);
  }

  @Override
  protected void doStop() {
    databasePool.close();
    databasePool = null;
  }

  @Override
  public RecordStoreSession openSession() {
    return new OrientRecordStoreSession(openDb(), recordIdObfuscator);
  }

  protected ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databasePool.acquire();
  }
}
