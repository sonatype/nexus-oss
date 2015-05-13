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
package org.sonatype.nexus.repository.search;

import java.util.LinkedHashSet;

import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTxHook;

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
  private final SearchService searchService;

  private final Repository repository;

  private final LinkedHashSet<EntityId> indexable;

  private final LinkedHashSet<EntityId> deindexable;

  public SearchHook(final SearchService searchService, final Repository repository) {
    this.searchService = checkNotNull(searchService);
    this.repository = checkNotNull(repository);
    this.indexable = new LinkedHashSet<>();
    this.deindexable = new LinkedHashSet<>();
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
      indexable.add(asset.componentId());
    }
  }

  @Override
  public void deleteAsset(final Asset... assets) {
    for (Asset asset : assets) {
      deindexable.add(asset.componentId());
    }
  }

  @Override
  public void postCommit() {
    for (EntityId entityId : deindexable) {
      searchService.delete(repository, entityId.toString());
    }
    for (EntityId entityId : indexable) {
      searchService.put(repository, entityId, entityId.toString());
    }
  }
}
