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

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

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
  public StorageFacetImpl(EventBus eventBus,
                          StorageService delegate) {
    installDependencies(eventBus); // TODO: whose responsibility is it to call this?
    this.delegate = delegate;
  }

  private Vertex bucket(OrientGraph graph) {
    synchronized (this) {
      if (bucketId == null) {
        bucketId = getOrCreateBucket(graph).getId();
      }
    }
    return delegate.findVertex(graph, bucketId, null);
  }

  private Vertex getOrCreateBucket(OrientGraph graph) {
    String repositoryName = getRepository().getName();
    Vertex bucket = delegate.findVertexWithProperty(graph, P_REPOSITORY_NAME, repositoryName, V_BUCKET);
    if (bucket == null) {
      bucket = delegate.createVertex(graph, V_BUCKET);
      bucket.setProperty(P_REPOSITORY_NAME, repositoryName);
    }
    return bucket;
  }

  @Override
  public OrientGraph getGraphTx() {
    return delegate.getGraphTx();
  }

  @Override
  public Iterable<Vertex> browseAssets(final OrientGraph graph) {
    return delegate.browseAssetsOwnedBy(graph, bucket(graph));
  }

  @Override
  public Iterable<Vertex> browseComponents(final OrientGraph graph) {
    return delegate.browseComponentsOwnedBy(graph, bucket(graph));
  }

  @Nullable
  @Override
  public Vertex findAsset(final OrientGraph graph, final Object vertexId) {
    return delegate.findAssetOwnedBy(graph, vertexId, bucket(graph));
  }

  @Nullable
  @Override
  public Vertex findAssetWithProperty(final OrientGraph graph, final String propName, final Object propValue) {
    return delegate.findAssetWithPropertyOwnedBy(graph, propName, propValue, bucket(graph));
  }

  @Nullable
  @Override
  public Vertex findComponent(final OrientGraph graph, final Object vertexId) {
    return delegate.findComponentOwnedBy(graph, vertexId, bucket(graph));
  }

  @Nullable
  @Override
  public Vertex findComponentWithProperty(final OrientGraph graph, final String propName, final Object propValue) {
    return delegate.findComponentWithPropertyOwnedBy(graph, propName, propValue, bucket(graph));
  }

  @Override
  public Vertex createAsset(final OrientGraph graph) {
    return delegate.createAssetOwnedBy(graph, bucket(graph));
  }

  @Override
  public Vertex createComponent(final OrientGraph graph) {
    return delegate.createComponentOwnedBy(graph, bucket(graph));
  }

  @Override
  public void deleteVertex(final OrientGraph graph, final Vertex vertex) {
    delegate.deleteVertex(graph, vertex);
  }

  @Override
  public BlobRef createBlob(final InputStream inputStream, final Map<String, String> headers) {
    return delegate.createBlob(inputStream, headers);
  }

  @Nullable
  @Override
  public org.sonatype.nexus.blobstore.api.Blob getBlob(final BlobRef blobRef) {
    return delegate.getBlob(blobRef);
  }

  @Override
  public boolean deleteBlob(final BlobRef blobRef) {
    return delegate.deleteBlob(blobRef);
  }
}
