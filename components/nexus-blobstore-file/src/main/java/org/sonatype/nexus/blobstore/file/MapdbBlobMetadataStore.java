/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.file;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.common.collect.AutoClosableIterable;
import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;
import org.mapdb.TxBlock;
import org.mapdb.TxMaker;
import org.mapdb.TxRollbackException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.blobstore.file.internal.ExternalizationHelper.readNullableLong;
import static org.sonatype.nexus.blobstore.file.internal.ExternalizationHelper.readNullableString;
import static org.sonatype.nexus.blobstore.file.internal.ExternalizationHelper.writeNullableLong;
import static org.sonatype.nexus.blobstore.file.internal.ExternalizationHelper.writeNullableString;

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

  private MapdbBlobMetadataStore(final File directory) {
    checkNotNull(directory);
    this.file = new File(directory, directory.getName() + ".db");
    log.debug("File: {}", file);
  }

  /**
   * MapDB uses a classloading strategy incompatible with OSGi (it uses the current thread's context class loader).
   * This method produces a BlobMetadataStore that has been wrapped with a proxy that ensures the right classloader
   * is used for mapdb's serializing/deserializing operations.
   */
  public static BlobMetadataStore create(final File directory) {
    final MapdbBlobMetadataStore inner = new MapdbBlobMetadataStore(directory);

    return (BlobMetadataStore) Proxy.newProxyInstance(BlobMetadataStore.class.getClassLoader(),
        new Class[]{BlobMetadataStore.class}, new OsgiCompatibleClassloaderAdvice(inner));
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

  private HTreeMap<BlobId, MetadataRecord> entries(final DB db) {
    return db.getHashMap("entries");
  }

  private NavigableSet<BlobId> states(final DB db, final BlobState state) {
    return db.getTreeSet("state_" + state.name());
  }

  /**
   * Metadata record for internal storage in MapDB.
   */
  static class MetadataRecord
      implements Externalizable
  {
    private final static int FORMAT_VERSION = 1;

    private BlobState state;

    private Map<String, String> headers;

    private boolean metrics;

    private DateTime created;

    private String sha1;

    private Long size;

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MetadataRecord that = (MetadataRecord) o;

      if (!Objects.equals(state, that.state)) {
        return false;
      }
      if (!Objects.equals(headers, that.headers)) {
        return false;
      }
      if (metrics != that.metrics) {
        return false;
      }
      if (!Objects.equals(created, that.created)) {
        return false;
      }
      if (!Objects.equals(sha1, that.sha1)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(state, headers, metrics, created, sha1, size);
    }

    public MetadataRecord(final BlobMetadata source) {
      this.state = source.getBlobState();
      this.headers = Maps.newHashMap(source.getHeaders());
      BlobMetrics metrics = source.getMetrics();
      if (metrics != null) {
        this.metrics = true;
        this.created = metrics.getCreationTime();
        this.sha1 = metrics.getSHA1Hash();
        this.size = metrics.getContentSize();
      }
      else {
        this.metrics = false;
        this.created = null;
        this.sha1 = null;
        this.size = null;
      }
    }

    /**
     * Deserialization constructor.
     */
    @SuppressWarnings("unused")
    public MetadataRecord()
    {
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "state=" + state +
          ", headers=" + headers +
          ", metrics=" + metrics +
          ", created=" + created +
          ", sha1='" + sha1 + '\'' +
          ", size=" + size +
          '}';
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
      out.writeInt(FORMAT_VERSION);

      out.writeInt(state.ordinal());

      out.writeInt(headers.size());
      for (Map.Entry<String, String> header : headers.entrySet()) {
        writeNullableString(out, header.getKey());
        writeNullableString(out, header.getValue());
      }

      out.writeBoolean(metrics);

      if (metrics) {
        // writeObject preserves nulls
        writeNullableLong(out, created == null ? null : created.getMillis());
        writeNullableString(out, sha1);
        writeNullableLong(out, size);
      }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
      final int version = in.readInt();
      checkState(version == 1, "Version must be 1.");

      state = BlobState.values()[in.readInt()];

      headers = Maps.newHashMap();
      final int numberOfHeaders = in.readInt();
      for (int i = 0; i < numberOfHeaders; i++) {
        headers.put(readNullableString(in), readNullableString(in));
      }

      metrics = in.readBoolean();
      if (metrics) {
        final Long createdMillis = readNullableLong(in);
        if (createdMillis != null) {
          created = new DateTime(createdMillis);
        }
        sha1 = readNullableString(in);
        size = readNullableLong(in);
      }
    }
  }

  private MetadataRecord convert(final BlobMetadata source) {
    return new MetadataRecord(source);
  }

  private BlobMetadata convert(final MetadataRecord source) {
    BlobMetadata target = new BlobMetadata(source.state, Maps.newHashMap(source.headers));
    if (source.metrics) {
      target.setMetrics(new BlobMetrics(source.created, source.sha1, source.size));
    }
    return target;
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

    final MetadataRecord record = convert(metadata);

    return database.execute(new Fun.Function1<BlobId, DB>()
    {
      @Override
      public BlobId run(final DB db) {
        BlobId id = newId(db);
        log.trace("Add: {}={}", id, record);

        MetadataRecord prev = entries(db).put(id, record);
        checkState(prev == null, "Duplicate blob-id: %s", id);

        // track state
        states(db, record.state).add(id);

        return id;
      }
    });
  }

  @Nullable
  @Override
  public BlobMetadata get(final BlobId id) {
    checkNotNull(id);
    ensureStarted();

    log.trace("Get: {}", id);

    DB db = database.makeTx();
    try {
      MetadataRecord record = entries(db).get(id);
      if (record != null) {
        return convert(record);
      }
      return null;
    }
    finally {
      db.close();
    }
  }

  @Override
  public void update(final BlobId id, final BlobMetadata metadata) {
    checkNotNull(id);
    checkNotNull(metadata);
    ensureStarted();

    final MetadataRecord record = convert(metadata);
    log.trace("Update: {}={}", id, record);

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        MetadataRecord prev = entries(db).put(id, record);
        checkState(prev != null, "Can not update non-existent blob-id: %s", id);

        // replace state
        states(db, prev.state).remove(id);
        states(db, record.state).add(id);
      }
    });
  }

  @Override
  public void delete(final BlobId id) {
    checkNotNull(id);
    ensureStarted();

    log.trace("Delete: {}", id);

    database.execute(new TxBlock()
    {
      @Override
      public void tx(final DB db) throws TxRollbackException {
        MetadataRecord prev = entries(db).remove(id);
        checkState(prev != null, "Can not delete non-existent blob-id: %s", id);

        // remove state
        states(db, prev.state).remove(id);
      }
    });
  }

  @Override
  public AutoClosableIterable<BlobId> findWithState(final BlobState state) {
    checkNotNull(state);
    ensureStarted();

    log.trace("Find with state: {}", state);

    final DB db = database.makeTx().snapshot();

    return new AutoClosableIterable<BlobId>()
    {
      private volatile boolean closed = false;

      @Override
      public Iterator<BlobId> iterator() {
        return states(db, state).iterator();
      }

      @Override
      public void close() throws Exception {
        db.close();
        closed = true;
      }

      @Override
      protected void finalize() throws Throwable {
        try {
          if (!closed) {
            log.warn("Leaked database connection: {}", db);
            db.close();
          }
        }
        finally {
          super.finalize();
        }
      }
    };
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
  public long getBlobCount() {
    ensureStarted();
    DB db = database.makeTx();
    try {
      return entries(db).sizeLong();
    }
    finally {
      db.close();
    }
  }

  @Override
  public long getTotalSize() {
    ensureStarted();

    // sum all file bytes in the database root
    long bytes = 0;
    for (File file : listFiles()) {
      bytes += file.length();
    }
    return bytes;
  }

  @Override
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

  /**
   * An invocation handler that ensures the context classloader is set up correctly for OSGi before MapDB attempts to
   * use it to resolve classes for serialized/externalized objects.
   */
  private static class OsgiCompatibleClassloaderAdvice
      implements InvocationHandler
  {
    private final BlobMetadataStore inner;

    private OsgiCompatibleClassloaderAdvice(final BlobMetadataStore inner) {this.inner = inner;}

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
      ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(MapdbBlobMetadataStore.class.getClassLoader());
        return method.invoke(inner, args);
      }
      finally {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
      }
    }
  }
}
