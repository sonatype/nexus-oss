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
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.nexus.orient.Hex;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.mapdb.DB;
import org.mapdb.DBMaker;
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
  private final File directory;

  private final SecureRandom random = new SecureRandom();

  private DB database;

  private HTreeMap<BlobId,BlobMetadata> entries;

  public MapdbBlobMetadataStore(final File directory) {
    this.directory = checkNotNull(directory);
    log.debug("Directory: {}", directory);
  }

  @Override
  protected void doStart() throws Exception {
    DirSupport.mkdir(directory);
    this.database = DBMaker.newFileDB(new File(directory, "db")).make();
    this.entries = database.getHashMap("entries");
    log.debug("Opened entries; count: {}", entries.sizeLong());
  }

  @Override
  protected void doStop() throws Exception {
    entries = null;
    database.close();
    database = null;
  }

  private BlobId newId() {
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    return new BlobId(Hex.encode(bytes));
  }

  @Override
  public BlobId add(final BlobMetadata metadata) {
    checkNotNull(metadata);
    ensureStarted();

    BlobId id = newId();
    log.debug("Add: {}={}", id, metadata);

    entries.put(id, metadata);
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
  }

  @Override
  public void delete(final BlobId id) {
    checkNotNull(id);

    log.debug("Delete: {}", id);

    entries.remove(id);
    database.commit();
  }

  @Override
  public Iterator<BlobId> findWithState(final BlobState state) {
    checkNotNull(state);
    ensureStarted();

    log.debug("Find with state: {}", state);

    // filter entries in given state
    Iterator<Entry<BlobId,BlobMetadata>> filter = Iterators.filter(entries.entrySet().iterator(), new Predicate<Entry<BlobId,BlobMetadata>>()
    {
      @Override
      public boolean apply(final @Nullable Entry<BlobId,BlobMetadata> input) {
        return input != null && state == input.getValue().getBlobState();
      }
    });

    // transform filtered entries to return blob-id
    return Iterators.transform(filter, new Function<Entry<BlobId,BlobMetadata>,BlobId>()
    {
      @Nullable
      @Override
      public BlobId apply(final @Nullable Entry<BlobId, BlobMetadata> input) {
        if (input != null) {
          return input.getKey();
        }
        return null;
      }
    });
  }

  private File[] listFiles() {
    File[] files = directory.listFiles();
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

    // sum all file bytes in the database directory
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
