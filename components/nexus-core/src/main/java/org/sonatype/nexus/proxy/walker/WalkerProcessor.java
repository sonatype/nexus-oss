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
 * A walker processors are units that are "attachable" to a single storage walk, hence the result will be combined but
 * having only one walk. If in any method and exception is thrown, the walker will stop.
 *
 * @author cstamas
 */
public interface WalkerProcessor
{
  boolean isActive();

  void beforeWalk(WalkerContext context)
      throws Exception;

  void onCollectionEnter(WalkerContext context, StorageCollectionItem coll)
      throws Exception;

  void processItem(WalkerContext context, StorageItem item)
      throws Exception;

  void onCollectionExit(WalkerContext context, StorageCollectionItem coll)
      throws Exception;

  void afterWalk(WalkerContext context)
      throws Exception;
}
