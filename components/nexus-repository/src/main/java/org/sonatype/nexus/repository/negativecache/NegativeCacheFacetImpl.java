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
package org.sonatype.nexus.repository.negativecache;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.sisu.goodies.common.Time;

import com.google.common.annotations.VisibleForTesting;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.sf.ehcache.Status.STATUS_ALIVE;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * EHCache based {@link NegativeCacheFacet} implementation.
 *
 * @since 3.0
 */
@Named("default")
public class NegativeCacheFacetImpl
    extends FacetSupport
    implements NegativeCacheFacet
{
  private final CacheManager cacheManager;

  @VisibleForTesting
  static final String CONFIG_KEY = "negativeCache";

  @VisibleForTesting
  static class Config
  {
    public boolean enabled;

    /**
     * Time-to-live seconds.
     */
    public int timeToLive = Time.hours(24).toSecondsI();

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "enabled=" + enabled +
          ", timeToLive=" + timeToLive +
          '}';
    }
  }

  private Config config;

  private Ehcache cache;

  @Inject
  public NegativeCacheFacetImpl(final CacheManager cacheManager) {
    this.cacheManager = checkNotNull(cacheManager);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);
  }

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);

    // create cache if enabled
    if (config.enabled) {
      maybeCreateCache();
    }
  }

  @Override
  protected void doUpdate(final Configuration configuration) throws Exception {
    Config previous = config;
    super.doUpdate(configuration);

    // re-create cache if enabled or cache settings changed
    if (config.enabled) {
      if (config.timeToLive != previous.timeToLive) {
        maybeDestroyCache();
        maybeCreateCache();
      }
    }
    else {
      // else destroy cache if disabled
      maybeDestroyCache();
    }
  }

  @Override
  protected void doDestroy() throws Exception {
    maybeDestroyCache();
    config = null;
  }

  private void maybeCreateCache() {
    if (cache == null) {
      log.debug("Creating negative-cache for: {}", getRepository());
      cache = newCache("negative-cache-" + getRepository().getName(), config.timeToLive);
      cacheManager.addCache(cache);
    }
  }

  @VisibleForTesting
  Ehcache newCache(final String name, final int timeToLiveSeconds) {
    return new Cache(
        name,
        10000, // maxElementsInMemory
        false, // overflowToDisk
        false, // eternal
        timeToLiveSeconds,
        0 // timeToIdleSeconds
    );
  }

  private void maybeDestroyCache() {
    if (cache != null) {
      log.debug("Destroying negative-cache for: {}", getRepository());
      if (STATUS_ALIVE.equals(cacheManager.getStatus())) {
        cacheManager.removeCache(cache.getName());
      }
      cache = null;
    }
  }

  @Override
  @Guarded(by = STARTED)
  public Status get(final NegativeCacheKey key) {
    checkNotNull(key);
    if (cache != null) {
      Element element = cache.get(key);
      return element == null || element.isExpired() ? null : (Status) element.getObjectValue();
    }
    return null;
  }

  @Override
  @Guarded(by = STARTED)
  public void put(final NegativeCacheKey key, final Status status) {
    checkNotNull(key);
    checkNotNull(status);
    if (cache != null) {
      log.debug("Adding {}={} to negative-cache of {}", key, status, getRepository());
      cache.put(new Element(key, status));
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void invalidate(final NegativeCacheKey key) {
    checkNotNull(key);
    if (cache != null) {
      log.debug("Removing {} from negative-cache of {}", key, getRepository());
      cache.remove(key);
    }
  }

  @Override
  public void invalidateSubset(final NegativeCacheKey key) {
    invalidate(key);
    for (Object entry : cache.getKeys()) {
      if (!key.equals(entry) && key.isParentOf((NegativeCacheKey) entry)) {
        invalidate((NegativeCacheKey) entry);
      }
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void invalidate() {
    if (cache != null) {
      log.debug("Removing all from negative-cache of {}", getRepository());
      cache.removeAll();
    }
  }

  @Override
  public NegativeCacheKey getCacheKey(final Context context) {
    return new PathNegativeCacheKey(context.getRequest().getPath());
  }
}
