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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.search.ComponentMetadataFactory;
import org.sonatype.nexus.repository.search.SearchFacet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.hibernate.validator.constraints.NotEmpty;

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
  private final BlobStoreManager blobStoreManager;

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  private final ComponentMetadataFactory componentMetadataFactory;

  @VisibleForTesting
  static final String CONFIG_KEY = "storage";

  @VisibleForTesting
  static class Config
  {
    @NotEmpty
    public String blobStoreName = "default";

    //@NotNull
    public WritePolicy writePolicy;

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "blobStoreName='" + blobStoreName + '\'' +
          ", writePolicy=" + writePolicy +
          '}';
    }
  }

  private Config config;

  private ORID bucketId;

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
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);
  }

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    initSchema();
    initBucket();
    super.doInit(configuration);
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
    try (GraphTx graphTx = openGraphTx(false)) {
      String repositoryName = getRepository().getName();
      Vertex bucketVertex = Iterables.getFirst(graphTx.getVertices(P_REPOSITORY_NAME, repositoryName), null);
      if (bucketVertex == null) {
        bucketVertex = graphTx.addVertex(V_BUCKET, (String) null);
        bucketVertex.setProperty(P_REPOSITORY_NAME, repositoryName);
        graphTx.commit();
      }
      bucketId = (ORID) bucketVertex.getId();
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
    BlobStore blobStore = blobStoreManager.get(config.blobStoreName);
    return new StorageTxImpl(new BlobTx(blobStore), openGraphTx(withHooks), bucketId, config.writePolicy);
  }

  private GraphTx openGraphTx(boolean withHooks) {
    ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire();
    GraphTx graphTx = new GraphTx(db);

    if (withHooks) {
      graphTx.registerHook(new LastUpdatedHook());
      try {
        SearchFacet searchFacet = facet(SearchFacet.class);
        graphTx.registerHook(new IndexingHook(graphTx, searchFacet));
      }
      catch (MissingFacetException e) {
        // no search facet, no indexing
      }
    }

    return graphTx;
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
    private final GraphTx graphTx;

    private final SearchFacet searchFacet;

    public IndexingHook(final GraphTx graphTx, final SearchFacet searchFacet) {
      this.graphTx = graphTx;
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
      if (doc.getIdentity().isPersistent()) {
        searchFacet.put(componentMetadataFactory.from(new ComponentImpl(new OrientVertex(graphTx, doc))));
      }
    }

    @Override
    public void onRecordAfterUpdate(final ODocument doc) {
      onRecordAfterCreate(doc);
    }

    @Override
    public void onRecordAfterDelete(final ODocument doc) {
      // TODO should indexing failures affect storage? (catch and log?)
      searchFacet.delete(new OrientVertex(graphTx, doc).getId().toString());
    }
  }
}
