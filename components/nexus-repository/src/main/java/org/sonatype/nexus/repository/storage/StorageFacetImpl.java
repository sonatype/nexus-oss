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

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.ODatabaseListenerSupport;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.search.ComponentMetadataFactory;
import org.sonatype.nexus.repository.search.SearchFacet;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

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

  private final ComponentMetadataFactory componentMetadataFactory;

  private final BucketEntityAdapter bucketEntityAdapter;

  private final ComponentEntityAdapter componentEntityAdapter;

  private final AssetEntityAdapter assetEntityAdapter;

  private String blobStoreName;

  private ORID bucketId;

  private WritePolicy writePolicy;

  @Inject
  public StorageFacetImpl(final BlobStoreManager blobStoreManager,
                          final @Named(ComponentDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider,
                          final ComponentMetadataFactory componentMetadataFactory)
  {
    this.blobStoreManager = checkNotNull(blobStoreManager);
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.componentMetadataFactory = checkNotNull(componentMetadataFactory);


    bucketEntityAdapter = new BucketEntityAdapter();
    componentEntityAdapter = new ComponentEntityAdapter(bucketEntityAdapter);
    assetEntityAdapter = new AssetEntityAdapter(bucketEntityAdapter, componentEntityAdapter);
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
      bucketEntityAdapter.register(db);
      componentEntityAdapter.register(db);
      assetEntityAdapter.register(db);
    }
  }

  private void initBucket() {
    // get or create the bucket for the repository and set bucketId for fast lookup later
    try (ODatabaseDocumentTx db = openDb(false)) {
      String repositoryName = getRepository().getName();
      ODocument doc = bucketEntityAdapter.getByRepositoryName(db, repositoryName);
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
    try (StorageTx tx = openStorageTx(false)) {
      tx.deleteBucket(tx.getBucket());
    }
  }

  @Override
  @Guarded(by = STARTED)
  public StorageTx openTx() {
    return openStorageTx(true);
  }

  private StorageTx openStorageTx(boolean withHooks) {
    BlobStore blobStore = blobStoreManager.get(blobStoreName);
    return new StorageTxImpl(new BlobTx(blobStore), openDb(withHooks), bucketId, writePolicy);
  }

  private ODatabaseDocumentTx openDb(boolean withHooks) {
    ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire();
    if (withHooks) {
      HooksListener hooksListener = new HooksListener();
      db.registerListener(hooksListener);
      hooksListener.registerHook(db, new LastUpdatedHook());
      try {
        SearchFacet searchFacet = getRepository().facet(SearchFacet.class);
        hooksListener.registerHook(db, new IndexingHook(searchFacet));
      }
      catch (MissingFacetException e) {
        // no search facet, no indexing
      }
    }
    return db;
  }

  private class HooksListener
      extends ODatabaseListenerSupport
  {
    private final List<ORecordHook> hooks = Lists.newArrayList();

    public void registerHook(final ODatabase db, final ORecordHook hook) {
      hooks.add(hook);
      db.registerHook(hook);
    }

    @Override
    public void onClose(final ODatabase db) {
      for (ORecordHook hook : hooks) {
        db.unregisterHook(hook);
      }
      db.registerListener(this);
    }
  }


  private class LastUpdatedHook
      extends ODocumentHookAbstract
  {
    public LastUpdatedHook() {
      setIncludeClasses(V_COMPONENT, V_ASSET);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
      return DISTRIBUTED_EXECUTION_MODE.TARGET_NODE;
    }

    public RESULT onRecordBeforeCreate(final ODocument doc) {
      setLastUpdatedToNow(doc);
      return RESULT.RECORD_CHANGED;
    }

    public RESULT onRecordBeforeUpdate(final ODocument doc) {
      setLastUpdatedToNow(doc);
      return RESULT.RECORD_CHANGED;
    }

    private void setLastUpdatedToNow(ODocument doc) {
      doc.field(P_LAST_UPDATED, new Date());
    }
  }

  private class IndexingHook
      extends ODocumentHookAbstract
  {
    private final SearchFacet searchFacet;

    public IndexingHook(final SearchFacet searchFacet) {
      this.searchFacet = searchFacet;
      setIncludeClasses(V_COMPONENT);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
      return DISTRIBUTED_EXECUTION_MODE.TARGET_NODE;
    }

    @Override
    public void onRecordAfterCreate(final ODocument doc) {
      // TODO should indexing failures affect storage? (catch and log?)
      //if (doc.getIdentity().isPersistent()) {
      //  searchFacet.put(componentMetadataFactory.from(new ComponentImpl(doc)));
      //}
    }

    @Override
    public void onRecordAfterUpdate(final ODocument doc) {
      onRecordAfterCreate(doc);
    }

    @Override
    public void onRecordAfterDelete(final ODocument doc) {
      // TODO should indexing failures affect storage? (catch and log?)
      searchFacet.delete(doc.getIdentity().toString());
    }
  }
}
