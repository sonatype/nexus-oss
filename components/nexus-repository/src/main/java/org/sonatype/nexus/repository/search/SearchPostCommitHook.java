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

import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.entity.EntityHelper;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTxPostCommitHook;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Search related {@link StorageTxPostCommitHook}.
 *
 * @since 3.0
 */
public class SearchPostCommitHook
    extends StorageTxPostCommitHook
{
  private final SearchService searchService;

  private final Repository repository;

  private final List<Component> indexable;

  private final List<EntityId> deindexable;

  public SearchPostCommitHook(final SearchService searchService, final Repository repository) {
    this.searchService = checkNotNull(searchService);
    this.repository = checkNotNull(repository);
    this.indexable = Lists.newArrayList();
    this.deindexable = Lists.newArrayList();
  }

  @Override
  public void createComponent(final Component... components) {
    updateComponent(components);
  }

  @Override
  public void updateComponent(final Component... components) {
    for (Component component : components) {
      indexable.add(component);
    }
  }

  @Override
  public void deleteComponent(final Component... components) {
    for (Component component : components) {
      deindexable.add(EntityHelper.id(component));
    }
  }

  @Override
  public void createAsset(@Nullable final Component component, final Asset... assets) {
    if (component != null) {
      updateComponent(component);
    }
  }

  @Override
  public void updateAsset(@Nullable final Component component, final Asset... assets) {
    if (component != null) {
      updateComponent(component);
    }
  }

  @Override
  public void deleteAsset(@Nullable final Component component, final Asset... assets) {
    if (component != null) {
      updateComponent(component);
    }
  }

  @Override
  public void postCommit() {
    for (EntityId entityId : deindexable) {
      searchService.delete(repository, entityId.toString());
    }
    for (Component component : indexable) {
      searchService.put(repository, component, EntityHelper.id(component).toString());
    }
  }
}
