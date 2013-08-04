/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A logical OR between two or more walker filters.
 *
 * @author Alin Dreghiciu
 */
public class DisjunctionWalkerFilter
    implements WalkerFilter
{

  /**
   * OR-ed filters (can be null or empty).
   */
  private final WalkerFilter[] m_filters;

  /**
   * Constructor.
   *
   * @param filters OR-ed filters (can be null or empty)
   */
  public DisjunctionWalkerFilter(final WalkerFilter... filters) {
    m_filters = filters;
  }

  /**
   * Performs a logical OR between results of calling {@link #shouldProcess(WalkerContext, StorageItem)} on all
   * filters. It will exit at first filter that returns true. <br/>
   * If no filters were provided returns true.
   *
   * {@inheritDoc}
   */
  public boolean shouldProcess(final WalkerContext context,
                               final StorageItem item)
  {
    if (m_filters == null || m_filters.length == 0) {
      return true;
    }
    for (WalkerFilter filter : m_filters) {
      if (filter.shouldProcess(context, item)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Performs a logical OR between results of calling
   * {@link #shouldProcessRecursively(WalkerContext, StorageCollectionItem)}  on all filters. It will exit at first
   * filter that returns true.<br/>
   * If no filters were provided returns true.
   *
   * {@inheritDoc}
   */
  public boolean shouldProcessRecursively(final WalkerContext context,
                                          final StorageCollectionItem coll)
  {
    if (m_filters == null || m_filters.length == 0) {
      return true;
    }
    for (WalkerFilter filter : m_filters) {
      if (filter.shouldProcessRecursively(context, coll)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builder method.
   *
   * @param filters OR-ed filters (can be null or empty)
   * @return disjunction between filters
   */
  public static DisjunctionWalkerFilter statisfiesOneOf(final WalkerFilter... filters) {
    return new DisjunctionWalkerFilter(filters);
  }

}
