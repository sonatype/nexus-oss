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
package org.sonatype.nexus.cache;

import java.io.File;
import java.net.URI;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * EhCache JCache {@link CacheManager} provider.
 *
 * @since 3.0
 */
@Named("ehcache")
@Singleton
public class EhCacheManagerProvider
    extends ComponentSupport
    implements Provider<CacheManager>
{
  private final URI uri;

  private CacheManager cacheManager;

  @Inject
  public EhCacheManagerProvider(final ApplicationDirectories directories) {
    File file = new File(directories.getAppDirectory("etc"), "ehcache.xml");
    checkState(file.exists(), "Missing required file: %s", file.getAbsolutePath());
    this.uri = file.toURI();
  }

  @VisibleForTesting
  public EhCacheManagerProvider(final URI uri) {
    this.uri = checkNotNull(uri);
  }

  @VisibleForTesting
  synchronized CacheManager create() {
    // EHCache bundle has dynamic import, so it's the preferred choice here
    ClassLoader ehcacheLoader = net.sf.ehcache.Cache.class.getClassLoader();

    CachingProvider provider = Caching.getCachingProvider("org.ehcache.jcache.JCacheCachingProvider", ehcacheLoader);
    log.info("Creating cache-manager with configuration: {}", uri);
    CacheManager cacheManager = provider.getCacheManager(uri, ehcacheLoader);
    log.debug("Created cache-manager: {}", cacheManager);
    return cacheManager;
  }

  @Override
  public synchronized CacheManager get() {
    if (cacheManager == null) {
      cacheManager = create();
    }
    return cacheManager;
  }

  @PreDestroy
  public synchronized void destroy() {
    if (cacheManager != null) {
      cacheManager.close();
      log.info("Cache-manager closed");
    }
  }
}
