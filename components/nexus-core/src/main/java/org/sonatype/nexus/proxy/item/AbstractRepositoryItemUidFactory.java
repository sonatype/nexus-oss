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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.locks.ResourceLockFactory;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.StringUtils;

/**
 * Abstract factory for UIDs.
 *
 * @author cstamas
 */
public abstract class AbstractRepositoryItemUidFactory
    implements RepositoryItemUidFactory, Disposable
{
  @Override
  public DefaultRepositoryItemUid createUid(final Repository repository, String path) {
    // path corrections
    if (!StringUtils.isEmpty(path)) {
      if (!path.startsWith(RepositoryItemUid.PATH_ROOT)) {
        path = RepositoryItemUid.PATH_ROOT + path;
      }
    }
    else {
      path = RepositoryItemUid.PATH_ROOT;
    }

    return new DefaultRepositoryItemUid(this, repository, path);
  }

  @Override
  public abstract DefaultRepositoryItemUid createUid(final String uidStr)
      throws IllegalArgumentException, NoSuchRepositoryException;

  // ==

  @Inject
  @Nullable
  @Named("${sisu-resource-locks:-disabled}")
  private ResourceLockFactory sisuLockFactory;

  private WeakHashMap<DefaultRepositoryItemUidLock, WeakReference<DefaultRepositoryItemUidLock>> locks =
      new WeakHashMap<DefaultRepositoryItemUidLock, WeakReference<DefaultRepositoryItemUidLock>>();

  @Override
  public DefaultRepositoryItemUidLock createUidLock(final RepositoryItemUid uid) {
    final String key = new String(uid.getKey());

    return doCreateUidLockForKey(key);
  }

  // ==

  protected synchronized DefaultRepositoryItemUidLock doCreateUidLockForKey(final String key) {
    final LockResource lockResource;
    if (sisuLockFactory != null) {
      lockResource = new SisuLockResource(sisuLockFactory.getResourceLock(key));
    }
    else {
      lockResource = new SimpleLockResource();
    }

    final DefaultRepositoryItemUidLock newLock = new DefaultRepositoryItemUidLock(key, lockResource);

    final WeakReference<DefaultRepositoryItemUidLock> oldLockRef = locks.get(newLock);

    if (oldLockRef != null) {
      final RepositoryItemUidLock oldLock = oldLockRef.get();

      if (oldLock != null) {
        return oldLockRef.get();
      }
    }

    locks.put(newLock, new WeakReference<DefaultRepositoryItemUidLock>(newLock));

    return newLock;
  }

  /**
   * For UTs, not to be used in production code!
   */
  protected int locksInMap() {
    return locks.size();
  }

  @Override
  public void dispose() {
    if (sisuLockFactory != null) {
      sisuLockFactory.shutdown();
    }
  }
}
