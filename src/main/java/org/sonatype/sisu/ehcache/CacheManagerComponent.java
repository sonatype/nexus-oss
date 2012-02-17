package org.sonatype.sisu.ehcache;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheManager;

/**
 * Provider responsible for creation, configuration and keeping a shared singleton EHCache CacheManager instance (to
 * make it usable across application, but also allow having more then 1 of them in whole JVM).
 * 
 * @author cstamas
 * @since 1.0
 */
public interface CacheManagerComponent
{
    /**
     * Returns the preconfigured singleton instance of CacheManager.
     * 
     * @return the configured cache manager singleton instance.
     */
    CacheManager getCacheManager();

    /**
     * Returns new instance of EHCache manager, usable to support uses cases described in
     * http://ehcache.org/documentation/faq#can-you-use-more-than-one-instance-of-ehcache-in-a-single-vm
     * 
     * @param file the EHCache XML configuration file to use for configuration, or {@code null} if you want defaults.
     * @return new CacheManager instance for every invocation.
     * @throws IOException if there is a fatal problem in accessing the provided configuration file.
     */
    CacheManager buildCacheManager( final File file )
        throws IOException;

    /**
     * Stops the CacheManager cleanly. (cstamas: we need SISU-93, exposing lifecycle over Component contract is just
     * bad!)
     */
    void shutdown();
}
