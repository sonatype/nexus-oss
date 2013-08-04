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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * A walker filter that is based on Google Guava Predicates.
 *
 * @author cstamas
 * @since 2.1
 */
public class PredicateWalkerFilter
    implements WalkerFilter
{
  private final Predicate<StorageItem> itemPredicate;

  private final Predicate<StorageCollectionItem> collectionPredicate;

  public PredicateWalkerFilter(final Predicate<StorageItem> itemPredicate) {
    this(itemPredicate, Predicates.<StorageCollectionItem>alwaysTrue());
  }

  public PredicateWalkerFilter(final Predicate<StorageItem> itemPredicate,
                               final Predicate<StorageCollectionItem> collectionPredicate)
  {
    this.itemPredicate = Preconditions.checkNotNull(itemPredicate, "Item predicate is null!");
    this.collectionPredicate = Preconditions.checkNotNull(collectionPredicate, "Collection predicate is null!");
  }

  @Override
  public boolean shouldProcess(WalkerContext context, StorageItem item) {
    return itemPredicate.apply(item);
  }

  @Override
  public boolean shouldProcessRecursively(WalkerContext context, StorageCollectionItem coll) {
    return collectionPredicate.apply(coll);
  }
}
