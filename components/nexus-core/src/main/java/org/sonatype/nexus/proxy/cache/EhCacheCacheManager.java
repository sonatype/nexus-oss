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

package org.sonatype.nexus.proxy.cache;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 *
 * @author cstamas
 */
@Component(role = CacheManager.class)
public class EhCacheCacheManager
    extends AbstractLoggingComponent
    implements CacheManager, Disposable
{
  @Requirement
  private CacheManagerComponent cacheManagerComponent;

  public static final String SINGLE_PATH_CACHE_NAME = "path-cache";

  public PathCache getPathCache(String cache) {
    final net.sf.ehcache.CacheManager ehCacheManager = cacheManagerComponent.getCacheManager();

    if (!ehCacheManager.cacheExists(SINGLE_PATH_CACHE_NAME)) {
      ehCacheManager.addCache(SINGLE_PATH_CACHE_NAME);
    }

    return new EhCachePathCache(cache, ehCacheManager.getEhcache(SINGLE_PATH_CACHE_NAME));
  }

  @Override
  public void dispose() {
    cacheManagerComponent.shutdown();
  }
}
