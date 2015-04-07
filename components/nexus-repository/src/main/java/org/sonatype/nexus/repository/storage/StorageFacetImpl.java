/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.storage;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.repository.FacetSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * Default {@link StorageFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class StorageFacetImpl
    extends FacetSupport
    implements StorageFacet
{
  public static final String CONFIG_KEY = "storage";

  private final BlobStoreManager blobStoreManager;

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  private final BucketEntityType bucketEntityType;

  private final ComponentEntityType componentEntityType;

  private final AssetEntityType assetEntityType;

  private String blobStoreName;

  private ORID bucketId;

  private WritePolicy writePolicy;

  @Inject
  public StorageFacetImpl(final BlobStoreManager blobStoreManager,
                          final @Named(ComponentDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider)
  {
    this.blobStoreManager = checkNotNull(blobStoreManager);
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);

    bucketEntityType = new BucketEntityType();
    componentEntityType = new ComponentEntityType(bucketEntityType);
    assetEntityType = new AssetEntityType(bucketEntityType, componentEntityType);
  }

  @Override
  protected void doConfigure() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    blobStoreName = attributes.get("blobStoreName", String.class, "default");
    log.debug("BLOB-store name: {}", blobStoreName);
    writePolicy = null;
    String writePolicyAttr = attributes.get("writePolicy", String.class);
    if (writePolicyAttr != null) {
      writePolicy = WritePolicy.valueOf(writePolicyAttr);
    }
    log.debug("Write Policy: {}", writePolicy);
  }

  @Override
  protected void doInit() throws Exception {
    initSchema();
    initBucket();
    super.doInit();
  }

  private void initSchema() {
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().connect()) {
      bucketEntityType.register(db);
      componentEntityType.register(db);
      assetEntityType.register(db);
    }
  }

  private void initBucket() {
    // get or create the bucket for the repository and set bucketId for fast lookup later
    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      String repositoryName = getRepository().getName();
      ODocument doc = getByRepositoryName(db, repositoryName);
      if (doc == null) {
        doc = db.newInstance(V_BUCKET);
        doc.field(P_REPOSITORY_NAME, repositoryName);
        doc.save();
        db.commit();
      }
      bucketId = doc.getIdentity();
    }
  }

  @Override
  protected void doDelete() throws Exception {
    // TODO: Make this a soft delete and cleanup later so it doesn't block for large repos.
    try (StorageTx tx = openStorageTx()) {
      tx.deleteBucket(tx.getBucket());
    }
  }

  @Override
  @Guarded(by = STARTED)
  public StorageTx openTx() {
    return openStorageTx();
  }

  private StorageTx openStorageTx() {
    BlobStore blobStore = blobStoreManager.get(blobStoreName);
    return new StorageTxImpl(new BlobTx(blobStore), databaseInstanceProvider.get().acquire(), bucketId, writePolicy);
  }

  @Nullable
  private ODocument getByRepositoryName(final ODatabaseDocumentTx db, final String repositoryName) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "SELECT FROM " + V_BUCKET + " WHERE " + P_REPOSITORY_NAME + " = ?"
    );
    List<ODocument> results = db.command(query).execute(repositoryName);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }

}
