/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package ehcache;

import net.sf.ehcache.Cache;

public class Ehcache162BenchmarkTest
    extends AbstractEhcacheBenchmarkTest
{

    /**
     * @return 1.6.2 compatible cache
     */
    protected Cache getTestCache()
    {
//        public Cache(String name,
//        int maxElementsInMemory,
//        MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
//        boolean overflowToDisk,
//        String diskStorePath,
//        boolean eternal,
//        long timeToLiveSeconds,
//        long timeToIdleSeconds,
//        boolean diskPersistent,
//        long diskExpiryThreadIntervalSeconds,
//        RegisteredEventListeners registeredEventListeners,
//        BootstrapCacheLoader bootstrapCacheLoader,
//        int maxElementsOnDisk,
//        int diskSpoolBufferSizeMB,
//        boolean clearOnFlush)

        Cache cache = new Cache( CACHE_NAME, MAX_ELEMENTS_IN_MEMORY,
                                 EVICTION_POLICY, OVERFLOW_TO_DISK, DISK_STORE_PATH,
                                 ETERNAL, TIME_TO_LIVE_SECONDS, TIME_TO_IDLE_SECONDS,
                                 DISK_PERSISTENT, DISK_EXPIRY_THREAD_INTERVAL_SECONDS, null, null, MAX_ELEMENTS_ON_DISK,
                                 DISK_POOL_BUFFER_SIZE_MB, CLEAR_ON_FLUSH );
        return cache;
    }

}
