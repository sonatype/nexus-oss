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
import java.nio.file.Path;
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
import org.mapdb.Fun.Tuple2;
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
  private final Path root;

  private TxMaker database;

  public MapdbBlobMetadataStore(final Path root) {
    this.root = checkNotNull(root);
    log.debug("Root: {}", root);
  }

  @Override
  protected void doStart() throws Exception {
    DirSupport.mkdir(root);
    File file = root.resolve("db").toFile();
    this.database = DBMaker.newFileDB(file)
        .checksumEnable()
        .makeTxMaker();
  }

  @Override
  protected void doStop() throws Exception {
    database.close();
    database = null;
  }

  private HTreeMap<BlobId, BlobMetadata> entries(final DB db) {
    return db.getHashMap("entries");
  }

  private NavigableSet<Tuple2<BlobState, BlobId>> states(final DB db) {
    return db.getTreeSet("states");
  }

  private Atomic.Long idSequence(final DB db) {
    return db.getAtomicLong("id_sequence");
  }

  /**
   * Generate a new blob identifier.
   */
  private BlobId newId(final DB db) {
    long id = idSequence(db).incrementAndGet();
    return new BlobId(String.format("%016x", id));
  }

  @Override
  public BlobId add(final BlobMetadata metadata) {
    checkNotNull(metadata);
    ensureStarted();

    return database.execute(new Fun.Function1<BlobId, DB>()
    {
      @Override
      public BlobId run(final DB db) {
        BlobId id = newId(db);
        log.trace("Add: {}={}", id, metadata);

        entries(db).put(id, metadata);

        // track state
        states(db).add(Fun.t2(metadata.getBlobState(), id));

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

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        log.trace("Update: {}={}", id, metadata);

        Map<BlobId, BlobMetadata> entries = entries(db);
        BlobMetadata prev = entries.get(id);
        entries.put(id, metadata);

        // replace state
        NavigableSet<Tuple2<BlobState, BlobId>> states = states(db);
        states.remove(Fun.t2(prev.getBlobState(), id));
        states.add(Fun.t2(metadata.getBlobState(), id));
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
        BlobMetadata prev = entries.get(id);
        entries.remove(id);

        // remove state
        states(db).remove(Fun.t2(prev.getBlobState(), id));
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
      for (BlobId id : Fun.filter(states(db), state)) {
        ids.add(id);
      }
      log.trace("Found: {} with state: {}", ids.size(), state);
      return ids;
    }
    finally {
      db.close();
    }
  }

  private File[] listFiles() {
    File[] files = root.toFile().listFiles();
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
