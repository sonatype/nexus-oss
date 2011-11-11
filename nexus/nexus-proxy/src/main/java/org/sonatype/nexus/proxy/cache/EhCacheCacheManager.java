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
package org.sonatype.nexus.proxy.cache;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 * 
 * @author cstamas
 */
@Component( role = CacheManager.class )
public class EhCacheCacheManager
    extends AbstractLoggingComponent
    implements CacheManager
{
    @Requirement
    private PlexusEhCacheWrapper cacheManager;
    
    public static final String SINGLE_PATH_CACHE_NAME = "path-cache";

    public PathCache getPathCache( String cache )
    {
        net.sf.ehcache.CacheManager ehCacheManager = this.cacheManager.getEhCacheManager();

        if ( !ehCacheManager.cacheExists( SINGLE_PATH_CACHE_NAME ) )
        {
            ehCacheManager.addCache( SINGLE_PATH_CACHE_NAME );
        }

        return new EhCachePathCache( cache, ehCacheManager.getEhcache( SINGLE_PATH_CACHE_NAME ) );
    }
}
