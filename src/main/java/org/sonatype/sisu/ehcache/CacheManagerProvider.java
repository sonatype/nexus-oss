package org.sonatype.sisu.ehcache;

import net.sf.ehcache.CacheManager;

/**
 * Component responsible for creation and keeping a singleton EHCache CacheManager instance (to make it usable across
 * application, and allow having more then 1 of them in whole JVM).
 * 
 * @author cstamas
 * @since 1.0
 */
public interface CacheManagerProvider
{
    /**
     * Returns you the preconfigured instance of CacheManager.
     * 
     * @return the configured cache manager singleton instance.
     */
    CacheManager getCacheManager();

    /**
     * Stops the CacheManager cleanly. (cstamas: we need SISU-93, exposing lifecycle over Component contract is just bad!)
     */
    void shutdown();
}
