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

import org.sonatype.nexus.blobstore.api.BlobRef;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static org.sonatype.nexus.repository.storage.StorageFacet.E_PART_OF_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CONTENT_TYPE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;

/**
 * Default {@link Asset} implementation.
 *
 * @since 3.0
 */
class AssetImpl
    extends MetadataNodeSupport
    implements Asset
{
  private Component component;

  private boolean componentKnown;

  AssetImpl(OrientVertex vertex) {
    super(vertex);
  }

  AssetImpl(OrientVertex vertex, @Nullable Component component) {
    this(vertex);
    this.component = component;
    this.componentKnown = true;
  }

  @Override
  @Nullable
  public Component component() {
    if (!componentKnown) {
      Vertex componentVertex = Iterables.getFirst(
          vertex.getVertices(Direction.OUT, E_PART_OF_COMPONENT), null);
      if (componentVertex != null) {
        component = new ComponentImpl((OrientVertex) componentVertex);
      }
      componentKnown = true;
    }
    return component;
  }

  @Override
  @Nullable
  public Long size() {
    return get(P_SIZE, Long.class);
  }

  @Override
  public Long requireSize() {
    return require(P_SIZE, Long.class);
  }

  @Override
  public Asset size(@Nullable Long size) {
    set(P_SIZE, size);
    return this;
  }

  @Override
  @Nullable
  public String contentType() {
    return get(P_CONTENT_TYPE);
  }

  @Override
  public String requireContentType() {
    return require(P_CONTENT_TYPE);
  }

  @Override
  public Asset contentType(@Nullable String contentType) {
    set(P_CONTENT_TYPE, contentType);
    return this;
  }

  @Override
  @Nullable
  public BlobRef blobRef() {
    String value = get(P_BLOB_REF);
    if (value != null) {
      return BlobRef.parse(value);
    }
    return null;
  }

  @Override
  public BlobRef requireBlobRef() {
    require(P_BLOB_REF);
    return blobRef();
  }

  @Override
  public Asset blobRef(@Nullable BlobRef blobRef) {
    if (blobRef == null) {
      set(P_BLOB_REF, null);
    }
    else {
      set(P_BLOB_REF, blobRef.toString());
    }
    return this;
  }
}
