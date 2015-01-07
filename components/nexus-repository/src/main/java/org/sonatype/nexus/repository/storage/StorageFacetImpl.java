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

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.entity.EntityId;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

/**
 * Default {@link StorageFacet} implementation.
 */
@Named
public class StorageFacetImpl
  extends FacetSupport
  implements StorageFacet
{
  private final BlobStore blobStore;

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  @Inject
  public StorageFacetImpl(BlobStore blobStore,
                          @Named(ComponentDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider) {
    this.blobStore = blobStore;
    this.databaseInstanceProvider = databaseInstanceProvider;

    // initialize schema if needed
    final OrientGraphNoTx graph = new OrientGraphNoTx(databaseInstanceProvider.get().acquire());
    try {
      initVertexType(graph, V_ASSET, new Predicate<OrientVertexType>() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(final OrientVertexType type) {
          type.createProperty(P_PATH, OType.STRING);
          graph.createKeyIndex(P_PATH, Vertex.class, new Parameter("class", V_ASSET));
          return true;
        }
      });
      initVertexType(graph, V_BUCKET, null);
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

  private void initVertexType(OrientGraphNoTx graph, String name, @Nullable Predicate<OrientVertexType> predicate) {
    if (graph.getVertexType(name) == null) {
      OrientVertexType type = graph.createVertexType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  private void initEdgeType(OrientGraphNoTx graph, String name, @Nullable Predicate<OrientEdgeType> predicate) {
    if (graph.getEdgeType(name) == null) {
      OrientEdgeType type = graph.createEdgeType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  @Override
  public OrientGraph getGraphTx() {
    ODatabaseDocumentTx documentTx = databaseInstanceProvider.get().acquire();
    return new OrientGraph(documentTx);
  }

  @Override
  public Iterable<Vertex> browseAssets(final OrientGraph graph) {
    return graph.getVerticesOfClass(V_ASSET);
  }

  @Override
  public Iterable<Vertex> browseBuckets(final OrientGraph graph) {
    return graph.getVerticesOfClass(V_BUCKET);
  }

  @Override
  public Iterable<Vertex> browseComponents(final OrientGraph graph) {
    return graph.getVerticesOfClass(V_COMPONENT);
  }

  @Override
  public Iterable<Vertex> browseLabels(final OrientGraph graph) {
    return graph.getVerticesOfClass(V_LABEL);
  }

  @Nullable
  @Override
  public Vertex findById(final OrientGraph graph, final Object id) {
    if (id instanceof EntityId) {
      EntityId entityId = (EntityId) id;
      return graph.getVertex(entityId.getValue());
    }
    else {
      return graph.getVertex(id);
    }
  }

  @Nullable
  @Override
  public Vertex findByPath(final OrientGraph graph, final String path) {
    return Iterables.getFirst(graph.getVertices(V_ASSET + "." + P_PATH, path), null);
  }

  @Override
  public Vertex createAsset(final OrientGraph graph) {
    return graph.addVertex(V_ASSET, (String) null);
  }

  @Override
  public Vertex createBucket(final OrientGraph graph) {
    return graph.addVertex(V_BUCKET, (String) null);
  }

  @Override
  public Vertex createComponent(final OrientGraph graph) {
    return graph.addVertex(V_COMPONENT, (String) null);
  }

  @Override
  public Vertex createLabel(final OrientGraph graph) {
    return graph.addVertex(V_LABEL, (String) null);
  }

  @Override
  public void delete(final OrientGraph graph, final Vertex vertex) {
    graph.removeVertex(vertex);
  }

  @Override
  public BlobRef createBlob(final InputStream inputStream, Map<String, String> headers) {
    org.sonatype.nexus.blobstore.api.Blob blob = blobStore.create(inputStream, headers);
    return new BlobRef("NODE", "STORE", blob.getId().asUniqueString());
  }

  @Nullable
  @Override
  public org.sonatype.nexus.blobstore.api.Blob getBlob(final BlobRef blobRef) {
    return blobStore.get(blobRef.getBlobId());
  }

  @Override
  public boolean deleteBlob(final BlobRef blobRef) {
    return blobStore.delete(blobRef.getBlobId());
  }
}
