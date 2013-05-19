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
import net.sf.ehcache.config.CacheConfiguration;

public class Ehcache251BenchmarkTest extends AbstractEhcacheBenchmarkTest {

    /**
     * @return 2.5.1 compatible cache
     */
    protected Cache getTestCache(){
        Cache cache = new Cache(
            new CacheConfiguration(CACHE_NAME, MAX_ELEMENTS_IN_MEMORY)
                .memoryStoreEvictionPolicy(EVICTION_POLICY)
                .overflowToDisk(OVERFLOW_TO_DISK)
                .eternal(ETERNAL)
                .timeToLiveSeconds(TIME_TO_LIVE_SECONDS)
                .timeToIdleSeconds(TIME_TO_IDLE_SECONDS)
                .diskPersistent(DISK_PERSISTENT)
                .diskExpiryThreadIntervalSeconds(DISK_EXPIRY_THREAD_INTERVAL_SECONDS));

        return cache;
    }

}
