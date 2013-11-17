package org.sonatype.sisu.ehcache;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.sf.ehcache.CacheManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link CacheManager} provider.
 *
 * @since 2.8
 */
@Named
@Singleton
public class CacheManagerProvider
  implements Provider<CacheManager>
{
  private final CacheManagerComponent factory;

  @Inject
  public CacheManagerProvider(final CacheManagerComponent factory) {
    this.factory = checkNotNull(factory);
  }

  @Override
  public CacheManager get() {
    return factory.getCacheManager();
  }
}
