/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.sisu.ehcache;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.CacheException;
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
     * Returns the pre-configured singleton instance of CacheManager.
     * 
     * @return the configured cache manager singleton instance.
     */
    CacheManager getCacheManager();

    /**
     * Returns new instance of EHCache manager, usable to support uses cases described in link below. Also, note that
     * manager instances got by this method should be handler by callers (cleanup for example), as this method will
     * construct new instance with every method invocation. Since version 2.5, EHCache enforces that in single VM you
     * must have only one manager with given manager name, so it is caller duty to ensure there is no collision
     * happening.
     * 
     * @param file the EHCache XML configuration file to use for configuration, or {@code null} if you want defaults.
     * @return new CacheManager instance for every invocation.
     * @throws IOException if there is a fatal problem in accessing the provided configuration file.
     * @throws CacheException if there is a problem with constructing the cache (for example, manager name collision).
     * @see <a
     *      href="http://ehcache.org/documentation/faq#can-you-use-more-than-one-instance-of-ehcache-in-a-single-vm">Multiple
     *      instances of EHCache in single VM</a>
     */
    CacheManager buildCacheManager( final File file )
        throws IOException, CacheException;

    /**
     * Cleanly shuts down the pre-configured singleton EHCache instance (see {@link #getCacheManager()} method), freeing
     * all resources and allocated caches. Call of this method is a must in case you are about to create another
     * instance of this component (or any EHCache Manager instance, even manually created one) that would have same name
     * as this manager had (depends on how you configure it, it is usually left as "default"), since EHCache (since
     * version 2.5) is very strict and does not allow more than one instance having same named manager within one JVM.
     * If CacheManagerComponent is already stopped, this call does not have any effect. The use of this method is only
     * needed if you really want to control the "lifecycle" of this component (like in a Unit Test or so) to start and
     * stop multiple instances of Cache Manager or so in same JVM. In other cases you should not tamper or care about
     * this, as the underlying EHCache will cleanup itself on JVM exit anyway.
     * 
     * @since 1.1
     */
    void shutdown();
}
