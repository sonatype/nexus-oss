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

package org.sonatype.nexus.blobstore.file.internal;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;
import org.mapdb.TxBlock;
import org.mapdb.TxMaker;
import org.mapdb.TxRollbackException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * MapDB implementation of {@link BlobMetadataStore}.
 *
 * @since 3.0
 */
public class MapdbBlobMetadataStore
    extends LifecycleSupport
    implements BlobMetadataStore
{
  private final File file;

  private TxMaker database;

  public MapdbBlobMetadataStore(final File directory) {
    checkNotNull(directory);
    this.file = new File(directory, directory.getName() + ".db");
    log.debug("File: {}", file);
  }

  /**
   * Returns the primary database file.  MapDB has additional files which are based on this filename.
   */
  public File getFile() {
    return file;
  }

  @Override
  protected void doStart() throws Exception {
    DirSupport.mkdir(file.getParentFile());
    this.database = DBMaker.newFileDB(file)
        .checksumEnable()
        .makeTxMaker();
  }

  @Override
  protected void doStop() throws Exception {
    database.close();
    database = null;
  }

  private Atomic.Long idSequence(final DB db) {
    return db.getAtomicLong("id_sequence");
  }

  private HTreeMap<BlobId, BlobMetadata> entries(final DB db) {
    return db.getHashMap("entries");
  }

  private NavigableSet<BlobId> states(final DB db, final BlobState state) {
    return db.getTreeSet("state_" + state.name());
  }

  /**
   * Generate a new blob identifier.
   */
  private BlobId newId(final DB db) {
    long id = idSequence(db).incrementAndGet();
    return new BlobId(String.format("%016x", id));
  }

  //
  // FIXME: values need to be immutable
  // FIXME: BlobMetadata is a non-trivial object with nested members which makes simple clone not enough to isolate it.
  //

  @Override
  public BlobId add(final BlobMetadata metadata) {
    checkNotNull(metadata);
    ensureStarted();

    // copy as values need to be immutable
    final BlobMetadata copy = metadata.copy();

    return database.execute(new Fun.Function1<BlobId, DB>()
    {
      @Override
      public BlobId run(final DB db) {
        BlobId id = newId(db);
        log.trace("Add: {}={}", id, copy);

        entries(db).put(id, copy);

        // track state
        states(db, copy.getBlobState()).add(id);

        return id;
      }
    });
  }

  @Nullable
  @Override
  public BlobMetadata get(final BlobId id) {
    checkNotNull(id);
    ensureStarted();

    return database.execute(new Fun.Function1<BlobMetadata, DB>()
    {
      @Override
      public BlobMetadata run(final DB db) {
        log.trace("Get: {}", id);
        return entries(db).get(id);
      }
    });
  }

  @Override
  public void update(final BlobId id, final BlobMetadata metadata) {
    checkNotNull(id);
    checkNotNull(metadata);
    ensureStarted();

    // copy as values need to be immutable
    final BlobMetadata copy = metadata.copy();

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        log.trace("Update: {}={}", id, copy);

        Map<BlobId, BlobMetadata> entries = entries(db);
        BlobMetadata prev = entries.put(id, copy);

        // replace state
        states(db, prev.getBlobState()).remove(id);
        states(db, copy.getBlobState()).add(id);
      }
    });
  }

  @Override
  public void delete(final BlobId id) {
    checkNotNull(id);
    ensureStarted();

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        log.trace("Delete: {}", id);

        Map<BlobId, BlobMetadata> entries = entries(db);
        BlobMetadata prev = entries.remove(id);

        // remove state
        states(db, prev.getBlobState()).remove(id);
      }
    });
  }

  @Override
  public Iterable<BlobId> findWithState(final BlobState state) {
    checkNotNull(state);
    ensureStarted();

    log.trace("Find with state: {}", state);

    DB db = database.makeTx();
    try {
      // copy ids avoid leaking transactional scope, may cause memory pressure
      List<BlobId> ids = Lists.newLinkedList();
      for (BlobId id : states(db, state)) {
        ids.add(id);
      }
      log.trace("Found #{} with state: {}", ids.size(), state);
      return ids;
    }
    finally {
      db.close();
    }
  }

  private File[] listFiles() {
    File[] files = file.getParentFile().listFiles();
    if (files == null) {
      // should never happen
      return new File[0];
    }
    return files;
  }

  @Override
  public MetadataMetrics getMetadataMetrics() {
    ensureStarted();

    DB db = database.makeTx();
    try {
      long count = entries(db).sizeLong();

      // sum all file bytes in the database root
      long bytes = 0;
      for (File file : listFiles()) {
        bytes += file.length();
      }

      return new MetadataMetrics(count, bytes);
    }
    finally {
      db.close();
    }
  }

  public void compact() {
    ensureStarted();

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        log.trace("Compacting");
        db.compact();
      }
    });
  }
}
