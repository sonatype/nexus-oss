/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import org.sonatype.nexus.repository.FacetSupport;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link StorageFacet} implementation.
 */
@Named
public class StorageFacetImpl
  extends FacetSupport
  implements StorageFacet
{
  private final StorageService storage;

  @Inject
  public StorageFacetImpl(final StorageService storage) {
    this.storage = checkNotNull(storage);
  }

  @Override
  protected void doInit() throws Exception {
    // TODO:
  }

  @Override
  public Graph getGraphTx() {
    return null;
  }

  @Override
  public Iterable<Vertex> browseAssets(final Graph graph) {
    return null;
  }

  @Override
  public Iterable<Vertex> browseComponents(final Graph graph) {
    return null;
  }

  @Nullable
  @Override
  public Vertex findAssetWithId(final Graph graph, final Object id) {
    return null;
  }

  @Nullable
  @Override
  public Vertex findAssetWithPath(final Graph graph, final String path) {
    return null;
  }

  @Nullable
  @Override
  public Vertex findComponentWithId(final Graph graph, final Object id) {
    return null;
  }

  @Override
  public Vertex createAsset(final Graph graph) {
    return null;
  }

  @Override
  public Vertex createComponent(final Graph graph) {
    return null;
  }

  @Override
  public void delete(final Vertex vertex) {

  }
}
