/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCache;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.hamcrest.MatcherAssert;

import java.io.Serializable;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Repeats the tests of StatelessAndStatefulWebSessionManager, using ehCache as the session store.
 * This test will check the cache instance directly (using the ehcache API to verify the cached contents)
 */
public class StatelessAndStatefulWebSessionManagerEhCacheTest extends StatelessAndStatefulWebSessionManagerTest
{
    private CacheManager cacheManager = null;

    protected void setupCacheManager( NexusWebRealmSecurityManager securityManager )
    {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.init();

        cacheManager = ehCacheManager.getCacheManager();

        // by default use the default implementation
        securityManager.setCacheManager( ehCacheManager );
    }

    protected void initCache( CachingSessionDAO sessionDAO )
    {
        super.initCache( sessionDAO );

        // verify ehCache was set for this test
        MatcherAssert.assertThat( sessionDAO.getActiveSessionsCache(), is( instanceOf( EhCache.class ) ) );
    }


    protected void verifyNoSessionStored()
    {
        super.verifyNoSessionStored();

        // use the EhCache API to verify no sessions are stored
        MatcherAssert.assertThat( cacheManager.getCache( CachingSessionDAO.ACTIVE_SESSION_CACHE_NAME ).getSize(), is( 0 ) );

    }

    protected void verifySingleSessionStored( Serializable sessionId )
    {
        super.verifySingleSessionStored( sessionId );

        // use the EhCache API to verify 1 sessions are stored

        Cache cache = cacheManager.getCache( CachingSessionDAO.ACTIVE_SESSION_CACHE_NAME );

        MatcherAssert.assertThat( cache.getSize(), is( 1 ) );
       // again using the sessionId
        MatcherAssert.assertThat( cache.get( sessionId ), notNullValue() );

    }

}
