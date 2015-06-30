/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.search;

import java.util.Set;

import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTxHook;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.entity.EntityHelper.id;

/**
 * Search related {@link StorageTxHook}.
 *
 * @since 3.0
 */
public class SearchHook
    extends StorageTxHook
{
  private final SearchFacetImpl searchFacet;

  private final Set<EntityId> indexable;

  private final Set<EntityId> deindexable;

  public SearchHook(final SearchFacetImpl searchFacet) {
    this.searchFacet = checkNotNull(searchFacet);
    this.indexable = Sets.newIdentityHashSet(); // use Object identity, as new entity hashCode changes
    this.deindexable = Sets.newHashSet();
  }

  @Override
  public void createComponent(final Component... components) {
    updateComponent(components);
  }

  @Override
  public void updateComponent(final Component... components) {
    for (Component component : components) {
      indexable.add(id(component));
    }
  }

  @Override
  public void deleteComponent(final Component... components) {
    for (Component component : components) {
      deindexable.add(id(component));
    }
  }

  @Override
  public void createAsset(final Asset... assets) {
    updateAsset(assets);
  }

  @Override
  public void updateAsset(final Asset... assets) {
    for (Asset asset : assets) {
      final EntityId componentId = asset.componentId();
      if (componentId != null) {
        indexable.add(componentId);
      }
    }
  }

  @Override
  public void deleteAsset(final Asset... assets) {
    updateAsset(assets);
  }

  @Override
  public void postCommit() {
    for (EntityId entityId : deindexable) {
      searchFacet.delete(entityId);
    }
    for (EntityId entityId : indexable) {
      searchFacet.put(entityId);
    }
  }
}
