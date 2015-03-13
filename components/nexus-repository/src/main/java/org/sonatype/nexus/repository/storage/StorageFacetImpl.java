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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.search.ComponentMetadataFactory;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

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

  private static final long DELETE_BATCH_SIZE = 100L;

  private final BlobStoreManager blobStoreManager;

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  private final ComponentMetadataFactory componentMetadataFactory;

  private String blobStoreName;

  private Object bucketId;

  @Inject
  public StorageFacetImpl(final BlobStoreManager blobStoreManager,
                          final @Named(ComponentDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider,
                          final ComponentMetadataFactory componentMetadataFactory)
  {
    this.blobStoreManager = checkNotNull(blobStoreManager);
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.componentMetadataFactory = checkNotNull(componentMetadataFactory);
  }

  @Override
  protected void doConfigure() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    blobStoreName = attributes.get("blobStoreName", String.class, "default");
    log.debug("BLOB-store name: {}", blobStoreName);
  }

  @Override
  protected void doInit() throws Exception {
    initSchema();
    initBucket();
    super.doInit();
  }

  private void initSchema() {
    // initialize the graph schema if needed
    final CheckedGraphNoTx graph = new CheckedGraphNoTx(databaseInstanceProvider.get().acquire());
    graph.setUseLightweightEdges(true);
    try {
      initVertexType(graph, V_ASSET, new Predicate<OrientVertexType>()
      {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(final OrientVertexType type) {
          type.createProperty(P_PATH, OType.STRING);
          graph.createKeyIndex(P_PATH, Vertex.class, new Parameter("class", V_ASSET));
          return true;
        }
      });
      initVertexType(graph, V_BUCKET, new Predicate<OrientVertexType>()
      {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(final OrientVertexType type) {
          type.createProperty(P_REPOSITORY_NAME, OType.STRING);
          graph.createKeyIndex(P_REPOSITORY_NAME, Vertex.class, new Parameter("type", "UNIQUE"),
              new Parameter("class", V_BUCKET));
          return true;
        }
      });
      initVertexType(graph, V_COMPONENT, null);
      initVertexType(graph, V_LABEL, null);

      initEdgeType(graph, E_CONTAINS_COMPONENTS_WITH_LABEL, null);
      initEdgeType(graph, E_HAS_LABEL, null);
      initEdgeType(graph, E_OWNS_ASSET, null);
      initEdgeType(graph, E_OWNS_COMPONENT, null);
      initEdgeType(graph, E_PART_OF_COMPONENT, null);
    }
    finally {
      graph.shutdown();
    }
  }

  private static class CheckedGraphNoTx
      extends OrientGraphNoTx
  {
    public CheckedGraphNoTx(ODatabaseDocumentTx db) {
      super(db);
      checkForGraphSchema(db);
    }
  }

  private void initVertexType(CheckedGraphNoTx graph, String name, @Nullable Predicate<OrientVertexType> predicate) {
    if (graph.getVertexType(name) == null) {
      OrientVertexType type = graph.createVertexType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  private void initEdgeType(CheckedGraphNoTx graph, String name, @Nullable Predicate<OrientEdgeType> predicate) {
    if (graph.getEdgeType(name) == null) {
      OrientEdgeType type = graph.createEdgeType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  private void initBucket() {
    // get or create the bucket for the repository and set bucketId for fast lookup later
    try (GraphTx graphTx = openGraphTx()) {
      String repositoryName = getRepository().getName();
      Vertex bucket = Iterables.getFirst(graphTx.getVertices(P_REPOSITORY_NAME, repositoryName), null);
      if (bucket == null) {
        bucket = graphTx.addVertex(V_BUCKET, (String) null);
        bucket.setProperty(P_REPOSITORY_NAME, repositoryName);
        graphTx.commit();
      }
      bucketId = bucket.getId();
    }
  }

  @Override
  protected void doDelete() throws Exception {
    // delete all assets, blobs, components, and finally, the bucket.
    // TODO: This could take a while for large repos.
    //       Hide the bucket right away, but figure out a way to do the deletions asynchronously.
    try (StorageTx tx = openStorageTx()) {
      OrientVertex bucket = tx.getBucket();
      deleteAll(tx, tx.browseAssets(bucket), new Predicate<OrientVertex>()
      {
        @Override
        public boolean apply(final OrientVertex vertex) {
          BlobRef blobRef = BlobRef.parse(checkNotNull((String) vertex.getProperty(P_BLOB_REF)));
          tx.deleteBlob(blobRef);
          return true;
        }
      });
      deleteAll(tx, tx.browseComponents(bucket), null);
      tx.deleteVertex(bucket);
      tx.commit();
    }
  }

  /**
   * Deletes all given vertices in batches. If a predicate is specified, it will be executed before each delete.
   */
  private void deleteAll(StorageTx tx, Iterable<OrientVertex> vertices, @Nullable Predicate<OrientVertex> predicate) {
    long count = 0;
    for (OrientVertex vertex : vertices) {
      if (predicate != null) {
        predicate.apply(vertex);
      }
      tx.deleteVertex(vertex);
      count++;
      if (count == DELETE_BATCH_SIZE) {
        tx.commit();
        count = 0;
      }
    }
    tx.commit();
  }

  @Override
  @Guarded(by = STARTED)
  public StorageTx openTx() {
    return openStorageTx();
  }

  private StorageTx openStorageTx() {
    BlobStore blobStore = blobStoreManager.get(blobStoreName);
    return new StorageTxImpl(new BlobTx(blobStore), openGraphTx(), bucketId);
  }

  private GraphTx openGraphTx() {
    ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire();
    try {
      return new IndexHookedGraphTx(db, getRepository().facet(SearchFacet.class));
    }
    catch (MissingFacetException e) {
      // no search facet, no indexing
      return new GraphTx(db);
    }
  }

  private class IndexHookedGraphTx
      extends GraphTx
  {

    private final ORecordHook hook;

    public IndexHookedGraphTx(final ODatabaseDocumentTx db, final SearchFacet searchFacet) {
      super(db);
      database.registerHook(hook = new ODocumentHookAbstract()
      {
        {
          setIncludeClasses(V_COMPONENT);
        }

        @Override
        public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
          return DISTRIBUTED_EXECUTION_MODE.TARGET_NODE;
        }

        @Override
        public void onRecordAfterCreate(final ODocument doc) {
          // TODO should indexing failures affect storage? (catch and log?)
          if (doc.getIdentity().isPersistent()) {
            searchFacet.put(componentMetadataFactory.from(new OrientVertex(IndexHookedGraphTx.this, doc)));
          }
        }

        @Override
        public void onRecordAfterUpdate(final ODocument doc) {
          onRecordAfterCreate(doc);
        }

        @Override
        public void onRecordAfterDelete(final ODocument doc) {
          // TODO should indexing failures affect storage? (catch and log?)
          searchFacet.delete(new OrientVertex(IndexHookedGraphTx.this, doc).getId().toString());
        }
      });
    }

    @Override
    public void close() {
      database.unregisterHook(hook);
      super.close();
    }
  }
}
