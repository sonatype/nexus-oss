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

package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

/**
 * AttributeStorage that actually delegates the work to other instance of AttributeStorage, and having an option of
 * "fallback" to some secondary instance. Usable for scenarios where "transitioning" (smooth upgrade for example) is to
 * be used, the "main" attribute storage would be "upgraded" from "legacy" attribute storage as the attributes are
 * requested over the time from this instance. This class is not a component, but is used by AttributesHandler when
 * "transitioning" is needed.
 *
 * @author cstamas
 * @since 2.0
 */
public class TransitioningAttributeStorage
    implements AttributeStorage
{
  /**
   * All attributes "leaving" fallback storage are marked (by presence) of this key.
   */
  public static final String FALLBACK_MARKER_KEY = LegacyFSAttributeStorage.class.getName();

  private final AttributeStorage mainAttributeStorage;

  private final AttributeStorage fallbackAttributeStorage;

  public TransitioningAttributeStorage(final AttributeStorage mainAttributeStorage,
                                       final AttributeStorage fallbackAttributeStorage)
  {
    super();
    this.mainAttributeStorage = mainAttributeStorage;
    this.fallbackAttributeStorage = fallbackAttributeStorage;
  }

  @Override
  public Attributes getAttributes(final RepositoryItemUid uid)
      throws IOException
  {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.read);

    try {
      Attributes result = mainAttributeStorage.getAttributes(uid);

      if (result == null && fallbackAttributeStorage != null) {
        result = fallbackAttributeStorage.getAttributes(uid);
        if (result != null) {
          // mark it as legacy
          result.put(FALLBACK_MARKER_KEY, Boolean.TRUE.toString());
        }
      }

      return result;
    }
    finally {
      uidLock.unlock();
    }
  }

  @Override
  public void putAttributes(final RepositoryItemUid uid, final Attributes item)
      throws IOException
  {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.create);

    try {
      if (fallbackAttributeStorage != null) {
        // shave the legacy marker if any
        item.remove(FALLBACK_MARKER_KEY);
      }

      mainAttributeStorage.putAttributes(uid, item);

      if (fallbackAttributeStorage != null) {
        try {
          fallbackAttributeStorage.deleteAttributes(uid);
        }
        catch (IOException e) {
          // legacy swallows them, this is needed only to satisfy it's signature
        }
      }
    }
    finally {
      uidLock.unlock();
    }
  }

  @Override
  public boolean deleteAttributes(final RepositoryItemUid uid)
      throws IOException
  {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.delete);

    try {
      final boolean mainResult = mainAttributeStorage.deleteAttributes(uid);

      if (fallbackAttributeStorage != null) {
        try {
          return fallbackAttributeStorage.deleteAttributes(uid) || mainResult;
        }
        catch (IOException e) {
          // legacy swallows them, this is needed only to satisfy it's signature
        }
      }

      return mainResult;
    }
    finally {
      uidLock.unlock();
    }
  }
}
