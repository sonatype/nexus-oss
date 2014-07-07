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
import java.util.NavigableSet;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.HTreeMap;

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

  private DB database;

  private HTreeMap<BlobId,BlobMetadata> entries;

  private NavigableSet<Tuple2<BlobState,BlobId>> states;

  private Atomic.Long idSequence;

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
        .make();

    this.entries = database.getHashMap("entries");
    this.states = database.getTreeSet("states");
    this.idSequence = database.getAtomicLong("id_sequence");
  }

  @Override
  protected void doStop() throws Exception {
    entries = null;
    idSequence = null;
    database.close();
    database = null;
  }

  /**
   * Generate a new blob identifier.
   */
  private BlobId newId() {
    long id = idSequence.incrementAndGet();
    return new BlobId(String.format("%016x", id));
  }

  @Override
  public BlobId add(final BlobMetadata metadata) {
    checkNotNull(metadata);
    ensureStarted();

    BlobId id = newId();
    log.debug("Add: {}={}", id, metadata);

    entries.put(id, metadata);

    // track state
    states.add(Fun.t2(metadata.getBlobState(), id));

    database.commit();

    return id;
  }

  @Nullable
  @Override
  public BlobMetadata get(final BlobId id) {
    checkNotNull(id);
    ensureStarted();

    log.debug("Get: {}", id);

    return entries.get(id);
  }

  @Override
  public void update(final BlobId id, final BlobMetadata metadata) {
    checkNotNull(id);
    checkNotNull(metadata);
    ensureStarted();

    log.debug("Update: {}={}", id, metadata);

    entries.put(id, metadata);

    // replace state
    BlobState newState = metadata.getBlobState();
    for (BlobState state : BlobState.values()) {
      if (state == newState) {
        states.add(Fun.t2(newState, id));
      }
      else {
        states.remove(Fun.t2(newState, id));
      }
    }

    database.commit();
  }

  @Override
  public void delete(final BlobId id) {
    checkNotNull(id);

    log.debug("Delete: {}", id);

    entries.remove(id);
    database.commit();
  }

  @Override
  public Iterable<BlobId> findWithState(final BlobState state) {
    checkNotNull(state);
    ensureStarted();

    log.debug("Find with state: {}", state);

    return Fun.filter(states, state);
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

    long count = entries.sizeLong();

    // sum all file bytes in the database root
    long bytes = 0;
    for (File file : listFiles()) {
      bytes += file.length();
    }

    return new MetadataMetrics(count, bytes);
  }

  public void compact() {
    ensureStarted();

    log.debug("Compacting");
    database.compact();
  }
}
