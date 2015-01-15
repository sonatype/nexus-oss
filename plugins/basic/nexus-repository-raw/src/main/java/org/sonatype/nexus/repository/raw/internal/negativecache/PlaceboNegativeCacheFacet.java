package org.sonatype.nexus.repository.raw.internal.negativecache;

import org.sonatype.nexus.repository.FacetSupport;

/**
 * @since 3.0
 */
public class PlaceboNegativeCacheFacet
    extends FacetSupport
    implements NegativeCacheFacet
{
  @Override
  public void cacheNotFound(final NegativeCacheKey key) {
    // do nothing
  }

  @Override
  public boolean isNotFound(final NegativeCacheKey key) {
    return false;
  }

  @Override
  public void uncacheNotFound(final NegativeCacheKey key) {
    // do nothing
  }
}
