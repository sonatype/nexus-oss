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

package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The event fired on Expiring Not Found cache.
 * <p>
 * Deprecation note: for historical reasons, before Nexus 1.4, there was only one method {@code expireCaches()}. After
 * Nexus 1.4 we separated out the "purge NFC" part of it into a new method {@code expireNotFoundCaches()}, but the
 * actual event being fired on those two were never split. Today, we are adding new event but also providing all the
 * needed bits to keep backward compatible code in place. See newly added {@link RepositoryEventExpireNotFoundCaches}
 * and {@link RepositoryEventExpireProxyCaches}.
 * <p>
 * Related change: https://github.com/sonatype/nexus/commit/decd41b64c6515a8822b248dc970c3bcb204faaf
 *
 * @author cstamas
 * @deprecated This event is superseded by {@link RepositoryEventExpireNotFoundCaches}. For now, this class is kept,
 *             but
 *             is made abstract. The new event does {@link RepositoryEventExpireNotFoundCaches} extends this class and
 *             in future will be removed.
 */
public abstract class RepositoryEventExpireCaches
    extends RepositoryMaintenanceEvent
{
  /**
   * From where it happened
   */
  private final String path;

  protected RepositoryEventExpireCaches(final Repository repository, final String path) {
    super(repository);
    this.path = checkNotNull(path);
  }

  public String getPath() {
    return path;
  }
}
