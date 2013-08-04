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
 * A walker filter that accepts items with a path that starts with a specified path.
 *
 * @author Alin Dreghiciu
 */
public class StartOfItemPathWalkerFilter
    implements WalkerFilter
{

  /**
   * The path that item path should start with.
   */
  private final String path;

  /**
   * Constructor.
   *
   * @param path the path that item path should start with
   */
  public StartOfItemPathWalkerFilter(final String path) {
    assert path != null : "Path must be specified (cannot be null)";

    this.path = path;
  }

  /**
   * Return "true" if the item path starts with specified path.
   *
   * {@inheritDoc}
   */
  public boolean shouldProcess(final WalkerContext ctx,
                               final StorageItem item)
  {
    return item.getPath().matches(path);
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldProcessRecursively(final WalkerContext ctx,
                                          final StorageCollectionItem coll)
  {
    return coll.getPath().matches(path);
  }

  /**
   * Builder method.
   *
   * @return new ItemNameWalkerFilter
   */
  public static StartOfItemPathWalkerFilter pathStartsWith(final String path) {
    return new StartOfItemPathWalkerFilter(path);
  }

}
