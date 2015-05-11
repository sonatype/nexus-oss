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
  private SearchFacet searchFacet;

  private final List<Component> indexable;

  private final List<SearchItemId> deindexable;

  public SearchPostCommitHook(final SearchFacet searchFacet) {
    this.searchFacet = checkNotNull(searchFacet);
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
      deindexable.add(searchFacet.identifier(component));
    }
  }

  @Override
  public void execute() {
    for (SearchItemId searchItemId : deindexable) {
      searchFacet.delete(searchItemId);
    }
    for (Component component : indexable) {
      searchFacet.put(component);
    }
  }
}
