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
 * A logical NOT on a filter.
 *
 * @author Alin Dreghiciu
 */
public class NegationWalkerFilter
    implements WalkerFilter
{

  /**
   * Negated filter (can be null).
   */
  private final WalkerFilter m_filter;

  /**
   * Constructor.
   *
   * @param filter negated filter (can be null)
   */
  public NegationWalkerFilter(final WalkerFilter filter) {
    m_filter = filter;
  }

  /**
   * Performs a logical NOT on result of calling {@link #shouldProcess(WalkerContext, StorageItem)} on negated
   * filter.
   * <br/>
   * If no filter was provided returns true.
   *
   * {@inheritDoc}
   */
  public boolean shouldProcess(final WalkerContext context,
                               final StorageItem item)
  {
    return m_filter == null || !m_filter.shouldProcess(context, item);
  }

  /**
   * Performs a logical NOT on result of calling
   * {@link #shouldProcessRecursively(WalkerContext, StorageCollectionItem)} on negated filter.<br/>
   * If no filter was provided returns true.
   *
   * {@inheritDoc}
   */
  public boolean shouldProcessRecursively(final WalkerContext context,
                                          final StorageCollectionItem coll)
  {
    return m_filter == null || !m_filter.shouldProcessRecursively(context, coll);
  }

  /**
   * Builder method.
   *
   * @param filter negated filter (can be null or empty)
   * @return disjunction between filters
   */
  public static NegationWalkerFilter not(final WalkerFilter filter) {
    return new NegationWalkerFilter(filter);
  }

}