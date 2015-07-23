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

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCacheManager
  implements CacheManager, Initializable, Destroyable
{
  private static final Logger log = LoggerFactory.getLogger(JCacheManager.class);

  private final javax.cache.CacheManager cacheManager;

  public JCacheManager(final javax.cache.CacheManager cacheManager) { this.cacheManager = cacheManager; }

  /**
   * Acquires the cache with the specified <code>name</code>.  If a cache does not yet exist with that name, a new one
   * will be created with that name and returned.
   *
   * @param name the name of the cache to acquire.
   * @return the Cache with the given name
   * @throws CacheException if there is an error acquiring the Cache instance.
   */
  @Override
  public <K, V> Cache<K, V> getCache(final String name) throws CacheException {
    try {
      return new JCache(maybeCreateCache(name));
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  /**
   * Called when this object is being destroyed, allowing any necessary cleanup of internal resources.
   *
   * @throws Exception if an exception occurs during object destruction.
   */
  @Override
  public void destroy() throws Exception {
    try {
      cacheManager.close();
    }
    catch (Throwable t) {
      throw new Exception(t);
    }
  }

  /**
   * Initializes this object.
   *
   * @throws ShiroException if an exception occurs during initialization.
   */
  @Override
  public void init() throws ShiroException {

  }

  private <K, V> javax.cache.Cache<K, V> maybeCreateCache(String name) {
    javax.cache.Cache<K, V> cache = cacheManager.getCache(name);
    if (cache == null) {
      log.debug("Creating a shiro jcache named: {}", name);

      MutableConfiguration<K, V> cacheConfig = new MutableConfiguration<>();
      cacheConfig.setManagementEnabled(true);
      cacheConfig.setStatisticsEnabled(true);

      cache = cacheManager.createCache(name, cacheConfig);
      log.debug("Created a shiro cache: {}", cache);
    }
    return cache;
  }

}
