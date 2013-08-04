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

package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageLinkItem.
 */
public class DefaultStorageLinkItem
    extends AbstractStorageItem
    implements StorageLinkItem
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 4494595788515460394L;

  /**
   * The target.
   */
  private transient RepositoryItemUid targetUid;

  /**
   * Instantiates a new default storage link item.
   *
   * @param repository the repository
   * @param path       the path
   * @param canRead    the can read
   * @param canWrite   the can write
   * @param targetUid  the target uid
   */
  public DefaultStorageLinkItem(Repository repository, ResourceStoreRequest request, boolean canRead,
                                boolean canWrite, RepositoryItemUid targetUid)
  {
    super(repository, request, canRead, canWrite);

    setTarget(targetUid);
  }

  /**
   * Shortcut method.
   *
   * @deprecated supply resourceStoreRequest always
   */
  public DefaultStorageLinkItem(Repository repository, String path, boolean canRead, boolean canWrite,
                                RepositoryItemUid targetUid)
  {
    this(repository, new ResourceStoreRequest(path, true, false), canRead, canWrite, targetUid);
  }

  /**
   * Instantiates a new default storage link item.
   *
   * @param router    the router
   * @param path      the path
   * @param canRead   the can read
   * @param canWrite  the can write
   * @param targetUid the target uid
   */
  public DefaultStorageLinkItem(RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                boolean canWrite, RepositoryItemUid targetUid)
  {
    super(router, request, canRead, canWrite);

    setTarget(targetUid);
  }

  public RepositoryItemUid getTarget() {
    return targetUid;
  }

  public void setTarget(RepositoryItemUid target) {
    this.targetUid = target;
  }

  public void overlay(StorageItem item)
      throws IllegalArgumentException
  {
    super.overlay(item);
  }

  // ==

  public String toString() {
    if (getTarget() != null) {
      return String.format("%s (link to %s)", super.toString(), getTarget().toString());
    }
    else {
      return String.format("%s (link NO-TARGET)", super.toString());
    }
  }
}
