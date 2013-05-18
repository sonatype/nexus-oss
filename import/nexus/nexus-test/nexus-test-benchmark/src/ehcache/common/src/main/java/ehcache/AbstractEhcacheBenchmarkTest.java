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
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import static net.sf.ehcache.store.MemoryStoreEvictionPolicy.LFU;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.String;

import org.codehaus.plexus.component.annotations.Requirement;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.*;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import org.databene.contiperf.*;

@PerfTest(invocations = 1000)
public abstract class AbstractEhcacheBenchmarkTest {

    public static final String CACHE_NAME = "path-cache";
    public static final int MAX_ELEMENTS_IN_MEMORY = 100000;
    public static final boolean OVERFLOW_TO_DISK = false;
    public static final String DISK_STORE_PATH = null;
    public static final boolean ETERNAL = false;
    public static final long TIME_TO_LIVE_SECONDS = 120;
    public static final long TIME_TO_IDLE_SECONDS = 120;
    public static final boolean DISK_PERSISTENT = false;
    public static final long DISK_EXPIRY_THREAD_INTERVAL_SECONDS = 120;
    public static final int MAX_ELEMENTS_ON_DISK = 0;
    public static final int DISK_POOL_BUFFER_SIZE_MB = 0;
    public static final boolean CLEAR_ON_FLUSH = true;
    public static final MemoryStoreEvictionPolicy EVICTION_POLICY = MemoryStoreEvictionPolicy.LFU;

    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    CacheManager manager;
    Cache pathCache;
    final static String VALUE = "value";

    @After
    public void afterTest(){
        manager.removalAll();
        manager.shutdown();
        manager = null;
        pathCache = null;
    }

    /**
     *
     * @return the cache under test
     */
    protected abstract Cache getTestCache();

    @Before
    public void setupPathCache(){
        ///Create a CacheManager using defaults

        manager = CacheManager.create();

        manager.addCache(getTestCache());

            pathCache = manager.getCache(CACHE_NAME);
            for ( int i = 0; i < 50000;i++){
                // put 'get' keys so get test can find them
                pathCache.put(new Element("get" + i, VALUE));
            }
    }

    @Test
    public void putElementsThatDoNotExist() {
        final int size = pathCache.getSize();
        final int max = size + 1000;
        //50000 - 51000
        for ( int i = size; i < max;i++){
            pathCache.put(new Element("put" + i, VALUE));
        }
    }

    @Test
    public void putElementsThatExist() {
        final int size = 0;
        final int max = size + 1000;
        for ( int i = size; i < max;i++){
            // put 'get' keys from @Before
            pathCache.put(new Element("get" + i, VALUE));
        }
    }

    @Test
    public void getElementsThatExist() {
        final int size = 0;
        final int max = size + 1000;
        for ( int i = size; i < max;i++){
            pathCache.get("get" + i);
        }
    }

    @Test
    public void getElementsThatDoNotExist() {
        for ( int i = 100000; i < 150000;i++){
            pathCache.get("foo" + i);
        }
    }

    @Test
    public void removeElementsThatDoNotExist() {
        for ( int i = 100000; i < 150000;i++){
            pathCache.remove("rem" + i);
        }
    }

    @Test
    public void removeElementsThatExist() {
        final int size = 0;
        final int max = size + 1000;
        for ( int i = size; i< max;i++){
            pathCache.remove("get" + i);
        }
    }

}
