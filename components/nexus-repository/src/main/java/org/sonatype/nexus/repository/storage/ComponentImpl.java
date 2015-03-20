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

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.E_PART_OF_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_VERSION;

/**
 * Stored metadata about a component.
 *
 * @since 3.0
 */
class ComponentImpl
    extends MetadataNodeSupport
    implements Component
{
  ComponentImpl(OrientVertex vertex) {
    super(vertex);
  }

  @Override
  @Nullable
  public String group() {
    return get(P_GROUP);
  }

  @Override
  public String requireGroup() {
    return require(P_GROUP);
  }

  @Override
  public Component group(@Nullable String group) {
    set(P_GROUP, group);
    return this;
  }

  @Override
  public String name() {
    return require(P_NAME);
  }

  @Override
  public Component name(String name) {
    checkNotNull(name);
    set(P_NAME, name);
    return this;
  }

  @Override
  @Nullable
  public String version() {
    return get(P_VERSION);
  }

  @Override
  public String requireVersion() {
    return require(P_VERSION);
  }

  @Override
  public Component version(@Nullable String version) {
    set(P_VERSION, version);
    return this;
  }

  @Override
  public List<Asset> assets() {
    List<Asset> assets = Lists.newArrayList();
    for (Vertex assetVertex : vertex.getVertices(Direction.IN, E_PART_OF_COMPONENT)) {
      assets.add(new AssetImpl((OrientVertex) assetVertex, this));
    }
    return assets;
  }

  @Override
  public Asset firstAsset() {
    List<Asset> assets = assets();
    checkState(!assets.isEmpty(), "No assets found for component");
    return assets.get(0);
  }
}
