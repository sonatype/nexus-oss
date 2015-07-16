/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.cache;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

/**
 * A support class which implements basic caching controller logic.
 *
 * @since 3.0
 */
public class CacheController
    extends ComponentSupport
{
  private final int contentMaxAgeMinutes;

  private volatile String cacheToken;

  public CacheController(final int contentMaxAgeMinutes, @Nullable final String cacheToken) {
    this.contentMaxAgeMinutes = contentMaxAgeMinutes;
    this.cacheToken = cacheToken;
  }

  public void invalidateCache() {
    this.cacheToken = Long.toString(System.nanoTime());
  }

  public CacheInfo current() {
    return new CacheInfo(DateTime.now(), cacheToken);
  }

  public boolean isStale(final CacheInfo cacheInfo) throws IOException {
    if (contentMaxAgeMinutes < 0) {
      log.trace("Content max age checking disabled");
      return false;
    }
    if (cacheToken != null && !cacheToken.equals(cacheInfo.getCacheToken())) {
      log.debug("Content expired (cacheToken)");
      return true;
    }
    if (cacheInfo.getLastVerified().isBefore(new DateTime().minusMinutes(contentMaxAgeMinutes))) {
      log.debug("Content expired (age)");
      return true;
    }
    return false;
  }
}
