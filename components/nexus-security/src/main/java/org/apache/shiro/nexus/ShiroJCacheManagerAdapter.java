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
package org.apache.shiro.nexus;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.EternalExpiryPolicy;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shiro {@link javax.cache.CacheManager} to {@link CacheManager} adapter.
 *
 * @since 3.0
 */
public class ShiroJCacheManagerAdapter
  extends ComponentSupport
  implements CacheManager, Initializable, Destroyable
{
  private final javax.cache.CacheManager cacheManager;

  public ShiroJCacheManagerAdapter(final javax.cache.CacheManager cacheManager) {
    this.cacheManager = checkNotNull(cacheManager);
  }

  @Override
  public void init() {
    // empty
  }

  @Override
  public void destroy() throws Exception {
    cacheManager.close();
  }

  @Override
  public <K, V> Cache<K, V> getCache(final String name) {
    log.debug("Getting cache: {}", name);

    return new ShiroJCacheAdapter<>(this.<K,V>maybeCreateCache(name));
  }

  private <K, V> javax.cache.Cache<K, V> maybeCreateCache(final String name) {
    javax.cache.Cache<K, V> cache = cacheManager.getCache(name);
    if (cache == null) {
      log.debug("Creating cache: {}", name);

      MutableConfiguration<K, V> cacheConfig = new MutableConfiguration<K, V>()
          .setExpiryPolicyFactory(EternalExpiryPolicy.factoryOf())
          .setManagementEnabled(true)
          .setStatisticsEnabled(true);

      cache = cacheManager.createCache(name, cacheConfig);
      log.debug("Created cache: {}", cache);
    }
    else {
      log.debug("Re-using existing cache: {}", cache);
    }
    return cache;
  }
}
