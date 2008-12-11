/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.cache;

import java.util.Collection;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

/**
 * The Class EhCacheCache is a thin wrapper around EHCache just to make things going.
 * 
 * @author cstamas
 */
public class EhCachePathCache
    extends AbstractPathCache
{

    /** The ec. */
    private Ehcache ec;

    /**
     * Instantiates a new eh cache cache.
     * 
     * @param cache the cache
     */
    public EhCachePathCache( Ehcache cache )
    {
        super();
        this.ec = cache;
    }

    public boolean doContains( String key )
    {
        return ec.get( key ) != null;
    }

    public boolean doIsExpired( String key )
    {
        if ( ec.isKeyInCache( key ) )
        {
            Element el = ec.get( key );
            if ( el != null )
            {
                return el.isExpired();
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public void doPut( String key, Object element, int expiration )
    {
        Element el = new Element( key, element );
        if ( expiration != -1 )
        {
            el.setTimeToLive( expiration );
        }
        ec.put( el );
    }

    public boolean doRemove( String key )
    {
        return ec.remove( key );
    }

    public boolean removeWithChildren( String path )
    {
        List<String> keys = ec.getKeys();

        String keyToRemove = makeKeyFromPath( path );

        for ( String key : keys )
        {
            if ( key.startsWith( keyToRemove ) )
            {
                ec.remove( key );
            }
        }
        return true;
    }

    public void doPurge()
    {
        ec.removeAll();
        ec.flush();
    }

    public CacheStatistics getStatistics()
    {
        Statistics stats = ec.getStatistics();

        return new CacheStatistics( stats.getObjectCount(), stats.getCacheMisses(), stats.getCacheHits() );
    }

    public Collection<String> listKeysInCache()
    {
        ec.evictExpiredElements();

        List<String> keys = (List<String>) ec.getKeys();

        return keys;
    }

}
