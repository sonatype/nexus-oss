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
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nullable;
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
import com.google.common.base.Throwables;

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
  private static final String CONFIG_FILE = "ehcache.xml";

  private static final String DEFAULT_RESOURCE = "ehcache-default.xml";

  private CacheManager cacheManager;

  @Inject
  public EhCacheManagerProvider(final ApplicationDirectories directories) {
    checkNotNull(directories);

    URI uri = null;
    File file = new File(directories.getAppDirectory("etc"), CONFIG_FILE);
    if (file.exists()) {
      uri = file.toURI();
    }
    else {
      log.warn("Missing configuration: {}", file.getAbsolutePath());
    }
    this.cacheManager = create(uri);
  }

  @VisibleForTesting
  public EhCacheManagerProvider(@Nullable final URI uri) {
    this.cacheManager = create(uri);
  }

  @VisibleForTesting
  synchronized CacheManager create(@Nullable URI uri) {
    if (uri == null) {
      // load the configuration from defaults, this is mainly used for test environments
      log.warn("Using default configuration");
      URL url = getClass().getResource(DEFAULT_RESOURCE);
      checkState(url != null, "Missing configuration resource: %s", DEFAULT_RESOURCE);
      try {
        uri = url.toURI();
      }
      catch (URISyntaxException e) {
        throw Throwables.propagate(e);
      }
    }

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
    checkState(cacheManager != null);
    return cacheManager;
  }

  @PreDestroy
  public synchronized void destroy() {
    if (cacheManager != null) {
      cacheManager.close();
      log.info("Cache-manager closed");
      cacheManager = null;
    }
  }
}
