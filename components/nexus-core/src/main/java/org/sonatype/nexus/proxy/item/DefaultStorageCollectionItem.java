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

import java.util.Collection;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageCollectionItem.
 */
public class DefaultStorageCollectionItem
    extends AbstractStorageItem
    implements StorageCollectionItem
{

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -7329636330511885938L;

  /**
   * Instantiates a new default storage collection item.
   *
   * @param repository the repository
   * @param path       the path
   * @param canRead    the can read
   * @param canWrite   the can write
   */
  public DefaultStorageCollectionItem(Repository repository, ResourceStoreRequest request, boolean canRead,
                                      boolean canWrite)
  {
    super(repository, request, canRead, canWrite);
  }

  /**
   * Shotuct method.
   *
   * @deprecated supply resourceStoreRequest always
   */
  public DefaultStorageCollectionItem(Repository repository, String path, boolean canRead, boolean canWrite) {
    this(repository, new ResourceStoreRequest(path, true, false), canRead, canWrite);
  }

  /**
   * Instantiates a new default storage collection item.
   *
   * @param router   the router
   * @param path     the path
   * @param virtual  the virtual
   * @param canRead  the can read
   * @param canWrite the can write
   */
  public DefaultStorageCollectionItem(RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                      boolean canWrite)
  {
    super(router, request, canRead, canWrite);
  }

  /**
   * Shortcut method.
   *
   * @deprecated supply resourceStoreRequest always
   */
  public DefaultStorageCollectionItem(RepositoryRouter router, String path, boolean canRead, boolean canWrite) {
    this(router, new ResourceStoreRequest(path, true, false), canRead, canWrite);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.item.StorageCollectionItem#list()
   */
  public Collection<StorageItem> list()
      throws AccessDeniedException, NoSuchResourceStoreException, IllegalOperationException, ItemNotFoundException,
             StorageException
  {
    if (isVirtual()) {
      return getStore().list(getResourceStoreRequest());
    }
    else {
      Repository repo = getRepositoryItemUid().getRepository();

      Collection<StorageItem> result = repo.list(false, this);

      correctPaths(result);

      return result;
    }
  }

  /**
   * This method "normalizes" the paths back to the "level" from where the original item was requested.
   */
  protected void correctPaths(Collection<StorageItem> list) {
    for (StorageItem item : list) {
      if (getPath().endsWith(RepositoryItemUid.PATH_SEPARATOR)) {
        ((AbstractStorageItem) item).setPath(getPath() + item.getName());
      }
      else {
        ((AbstractStorageItem) item).setPath(getPath() + RepositoryItemUid.PATH_SEPARATOR + item.getName());
      }
    }
  }

  // --

  public String toString() {
    return String.format("%s (coll)", super.toString());
  }

}
