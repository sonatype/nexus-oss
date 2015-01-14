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
package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.storage.BlobRef;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.google.common.collect.ImmutableMap;
import com.tinkerpop.blueprints.Vertex;

import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageService.P_PATH;

/**
 * A {@link RawStorageFacet} that persists to a {@link StorageFacet}.
 *
 * @since 3.0
 */
public class RawStorageFacetImpl
    extends FacetSupport
    implements RawStorageFacet
{
  public static final String CONTENT_TYPE_PROPERTY = "content_type";

  public static final String BLOB_REF_PROPERTY = "blob_ref";

  @Inject
  public RawStorageFacetImpl() {
  }

  @Nullable
  @Override
  public RawContent get(final String path) throws IOException {
    return (RawContent) inTx(new GraphOperation()
    {
      @Override
      public Object execute(final GraphTx graph, final StorageFacet storage) {
        final Vertex asset = storage.findAssetWithProperty(graph, P_PATH, path);
        if (asset == null) {
          return null;
        }

        final BlobRef blobRef = getBlobRef(path, asset);

        final Blob blob = storage.getBlob(blobRef);
        checkState(blob != null, "asset at path %s refers to missing blob %s", path, blobRef);

        return marshall(asset, blob);
      }
    });
  }

  @Nullable
  @Override
  public void put(final String path, final RawContent content) throws IOException {
    inTx(new GraphOperation()
    {
      @Override
      public Object execute(final GraphTx graph, final StorageFacet storage) throws IOException {

        final Vertex asset = storage.createAsset(graph);

        // TODO: Figure out created-by header
        final ImmutableMap<String, String> headers = ImmutableMap
            .of(BlobStore.BLOB_NAME_HEADER, path, BlobStore.CREATED_BY_HEADER, "unknown");

        final BlobRef blobRef = storage.createBlob(content.openInputStream(), headers);

        asset.setProperty(BLOB_REF_PROPERTY, blobRef.toString());
        asset.setProperty(CONTENT_TYPE_PROPERTY, content.getContentType());
        return null;
      }
    });
  }

  @Override
  public boolean delete(final String path) throws IOException {
    return (boolean) inTx(new GraphOperation()
    {
      @Override
      public Object execute(final GraphTx graph, final StorageFacet storage) {
        final Vertex asset = storage.findAssetWithProperty(graph, P_PATH, path);
        if (asset == null) {
          return false;
        }

        final BlobRef blobRef = getBlobRef(path, asset);
        final boolean delete = storage.deleteBlob(blobRef);
        if (!delete) {
          log.warn("Deleted asset {} referenced missing blob {}", path, blobRef);
        }

        storage.deleteVertex(graph, asset);

        return true;
      }
    });
  }

  private static interface GraphOperation
  {
    Object execute(GraphTx graph, final StorageFacet storage) throws IOException;
  }

  private Object inTx(GraphOperation operation) throws IOException {
    final StorageFacet storage = getStorage();
    try (GraphTx graph = storage.getGraphTx()) {
      final Object result = operation.execute(graph, storage);
      graph.commit();
      return result;
    }
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

  private BlobRef getBlobRef(final String path, final Vertex asset) {
    String blobRefStr = asset.getProperty(BLOB_REF_PROPERTY);
    checkState(blobRefStr != null, "asset at path %s has missing blob reference", path);
    return new BlobRef(blobRefStr);
  }

  private RawContent marshall(final Vertex asset, final Blob blob) {
    final String contentType = asset.getProperty(CONTENT_TYPE_PROPERTY);

    return new RawContent()
    {
      @Override
      public String getContentType() {
        return contentType;
      }

      @Override
      public long getSize() {
        return blob.getMetrics().getContentSize();
      }

      @Override
      public InputStream openInputStream() {
        return blob.getInputStream();
      }
    };
  }
}
