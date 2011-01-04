/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * The Class EhCacheCache is a thin wrapper around EHCache just to make things going.
 * 
 * @author cstamas
 */
public class EhCachePathCache
    extends AbstractPathCache
{
    private final String _repositoryId;

    /** The ec. */
    private final Ehcache _ec;

    /**
     * Instantiates a new eh cache cache.
     * 
     * @param cache the cache
     */
    public EhCachePathCache( final String repositoryId, final Ehcache cache )
    {
        this._repositoryId = repositoryId;
        this._ec = cache;
    }

    protected String getRepositoryId()
    {
        return _repositoryId;
    }

    protected Ehcache getEHCache()
    {
        return _ec;
    }

    public boolean doContains( String key )
    {
        return getEHCache().get( key ) != null;
    }

    public boolean doIsExpired( String key )
    {
        if ( getEHCache().isKeyInCache( key ) )
        {
            Element el = getEHCache().get( key );
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
        getEHCache().put( el );
    }

    public boolean doRemove( String key )
    {
        return getEHCache().remove( key );
    }

    public boolean removeWithChildren( String path )
    {
        @SuppressWarnings( "unchecked" )
        List<String> keys = getEHCache().getKeys();

        String keyToRemove = makeKeyFromPath( path );

        for ( String key : keys )
        {
            if ( key.startsWith( keyToRemove ) )
            {
                getEHCache().remove( key );
            }
        }
        return true;
    }

    public void doPurge()
    {
        // getEHCache().removeAll();
        // getEHCache().flush();

        // this above is not true anymore, since the "shared-cache" implementor forgot about the fact that using purge()
        // will purge _all_ caches (it purges the one shared!), not just this repo's cache 
        removeWithChildren( RepositoryItemUid.PATH_ROOT );
    }

    public CacheStatistics getStatistics()
    {
        Statistics stats = getEHCache().getStatistics();

        return new CacheStatistics( stats.getObjectCount(), stats.getCacheMisses(), stats.getCacheHits() );
    }

    @SuppressWarnings( "unchecked" )
    public Collection<String> listKeysInCache()
    {
        getEHCache().evictExpiredElements();

        List<String> keys = new ArrayList<String>();

        // this is going to be slow (if we have lots of items) but if you are concerned about speed you shouldn't call
        // this method anyway, this should only be used for information purposes

        String startsWithString = getKeyPrefix();

        for ( String key : (List<String>) getEHCache().getKeys() )
        {
            if ( key.startsWith( startsWithString ) )
            {
                keys.add( key.substring( startsWithString.length() ) );
            }
        }

        return keys;
    }

    @Override
    protected String makeKeyFromPath( String path )
    {
        path = super.makeKeyFromPath( path );

        return getKeyPrefix() + path;
    }

    protected String getKeyPrefix()
    {
        return getRepositoryId() + ":";
    }

}
