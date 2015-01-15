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

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.FacetSupport;

import com.tinkerpop.blueprints.Vertex;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
import static org.sonatype.nexus.repository.storage.StorageService.P_REPOSITORY_NAME;
import static org.sonatype.nexus.repository.storage.StorageService.V_BUCKET;

/**
 * Default {@link StorageFacet} implementation.
 */
@Named
public class StorageFacetImpl
  extends FacetSupport
  implements StorageFacet
{
  private final StorageService delegate;

  private Object bucketId;

  @Inject
  public StorageFacetImpl(final StorageService delegate) {
    this.delegate = checkNotNull(delegate);
  }

  @Override
  protected void doStart() throws Exception {
    // determine the bucket id for the repository, creating the bucket if needed
    try (GraphTx graph = delegate.getGraphTx()) {
      String repositoryName = getRepository().getName();
      Vertex bucket = delegate.findVertexWithProperty(graph, P_REPOSITORY_NAME, repositoryName, V_BUCKET);
      if (bucket == null) {
        bucket = delegate.createVertex(graph, V_BUCKET);
        bucket.setProperty(P_REPOSITORY_NAME, repositoryName);
        graph.commit();
      }
      bucketId = bucket.getId();
    }
  }

  private Vertex bucket(final GraphTx graph) {
    return delegate.findVertex(graph, bucketId, null);
  }

  @Override
  @Guarded(by=STARTED)
  public GraphTx getGraphTx() {
    return delegate.getGraphTx();
  }

  @Override
  @Guarded(by=STARTED)
  public Iterable<Vertex> browseAssets(GraphTx graph) {
    return delegate.browseAssetsOwnedBy(bucket(graph));
  }

  @Override
  @Guarded(by=STARTED)
  public Iterable<Vertex> browseComponents(GraphTx graph) {
    return delegate.browseComponentsOwnedBy(bucket(graph));
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findAsset(final GraphTx graph, final Object vertexId) {
    return delegate.findAssetOwnedBy(graph, vertexId, bucket(graph));
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findAssetWithProperty(final GraphTx graph, final String propName, final Object propValue) {
    return delegate.findAssetWithPropertyOwnedBy(graph, propName, propValue, bucket(graph));
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findComponent(final GraphTx graph, final Object vertexId) {
    return delegate.findComponentOwnedBy(graph, vertexId, bucket(graph));
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findComponentWithProperty(final GraphTx graph, final String propName, final Object propValue) {
    return delegate.findComponentWithPropertyOwnedBy(graph, propName, propValue, bucket(graph));
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex createAsset(final GraphTx graph) {
    return delegate.createAssetOwnedBy(graph, bucket(graph));
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex createComponent(final GraphTx graph) {
    return delegate.createComponentOwnedBy(graph, bucket(graph));
  }

  @Override
  @Guarded(by=STARTED)
  public void deleteVertex(final GraphTx graph, final Vertex vertex) {
    delegate.deleteVertex(graph, vertex);
  }

  @Override
  @Guarded(by=STARTED)
  public BlobRef createBlob(final InputStream inputStream, final Map<String, String> headers) {
    return delegate.createBlob(inputStream, headers);
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Blob getBlob(final BlobRef blobRef) {
    return delegate.getBlob(blobRef);
  }

  @Override
  @Guarded(by=STARTED)
  public boolean deleteBlob(final BlobRef blobRef) {
    return delegate.deleteBlob(blobRef);
  }
}
