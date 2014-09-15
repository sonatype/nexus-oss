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
package org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link RawBinaryStore} that delegates artifact storage to a private {@link BlobStore}, and
 * stores artifact metadata in an Orient database.
 *
 * @since 3.0
 */
@Named
@Singleton
public class OrientRawBinaryStore
    extends LifecycleSupport
    implements RawBinaryStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final RawBinaryEntityAdapter entityAdapter = new RawBinaryEntityAdapter();

  private final BlobStore blobStore;

  private OClass entityType;

  @Inject
  // TODO: Find out where the @Named("config") db came from so I can make sure there's a "rawBinaryMetadata" one available
  public OrientRawBinaryStore(final @Named(OrientDBProvider.NAME) Provider<DatabaseInstance> databaseInstance,
                              final RecordIdObfuscator recordIdObfuscator,
                              @Named("rawBinaryBlobs") final BlobStore blobStore)
  {
    this.blobStore = checkNotNull(blobStore);
    this.databaseInstance = checkNotNull(databaseInstance);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  @Override
  protected void doStart() throws Exception {
    blobStore.start();
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityType = entityAdapter.register(db);
    }
    log.info("OrientRawBinaryStore started.");
  }

  @Override
  protected void doStop() throws Exception {
    blobStore.stop();
    entityType = null;
  }

  @Override
  public List<RawBinary> getForPath(final String prefix) {

    try (ODatabaseDocumentTx db = openDb()) {
      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + RawBinaryEntityAdapter.DB_CLASS + " WHERE path LIKE ?");

      List<RawBinary> binaries = new ArrayList<>();

      for (ODocument document : db.command(query).<List<ODocument>>execute(prefix + "%")) {
        final RawBinaryMetadata metadata = entityAdapter.read(document);

        final Blob blob = blobStore.get(new BlobId(metadata.getBlobId()));

        binaries.add(new RawBinary()
        {
          @Override
          public String getPath() {
            return metadata.getPath();
          }

          @Override
          public InputStream getInputStream() {
            return blob.getInputStream();
          }

          @Override
          public String getMimeType() {
            return metadata.getMimeType();
          }

          @Override
          public DateTime getModifiedDate() {
            return blob.getMetrics().getCreationTime();
          }
        });
      }

      return binaries;
    }
  }

  @Override
  public boolean create(final String path, final String mimeType, final InputStream inputStream) {
    if (exists(path)) {
      return false;
    }

    Map<String, String> blobMetadata = new HashMap<>();
    blobMetadata.put(BlobStore.BLOB_NAME_HEADER, path);
    blobMetadata.put(BlobStore.CREATED_BY_HEADER, "Unknown");
    blobMetadata.put("mimeType", mimeType);

    final Blob blob = blobStore.create(inputStream, blobMetadata);

    final RawBinaryMetadata item = new RawBinaryMetadata(path, blob.getId().getId(), mimeType);

    ORID rid;
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.create(db, item);
      rid = doc.getIdentity();
    }

    log.debug("Added item with RID: {}", rid);

    return true;
  }

  private boolean exists(final String path) {
    try (ODatabaseDocumentTx db = openDb()) {
      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + RawBinaryEntityAdapter.DB_CLASS + " WHERE path = ?");
      final List<ODocument> results = db.command(query).execute(path);
      return !results.isEmpty();
    }
  }

  @Override
  public boolean delete(final String path) {
    try (ODatabaseDocumentTx db = openDb()) {

      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + RawBinaryEntityAdapter.DB_CLASS + " WHERE path = ?");
      final List<ODocument> results = db.command(query).execute(path);

      if (results.isEmpty()) {
        return false;
      }

      final ORID rid = results.get(0).getIdentity();

      // if we can't get the metadata, then abort
      ORecordMetadata md = db.getRecordMetadata(rid);
      if (md == null) {
        log.debug("Unable to delete item with RID: {}", rid);
        return false;
      }
      // else delete the record
      db.delete(rid);
      log.debug("Deleted item with RID: {}", rid);
      return true;
    }
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private RawBinaryMetadataIdentity convertId(final ORID rid) {
    String encoded = recordIdObfuscator.encode(entityType, rid);
    return new RawBinaryMetadataIdentity(encoded);
  }

  private ORID convertId(final RawBinaryMetadataIdentity id) {
    return recordIdObfuscator.decode(entityType, id.toString());
  }

}
