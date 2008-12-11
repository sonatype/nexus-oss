/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

/**
 * The Class AbstractPathCache.
 */
public abstract class AbstractPathCache
    implements PathCache
{

    public final boolean contains( String path )
    {
        return doContains( makeKeyFromPath( path ) );
    }

    public final boolean isExpired( String path )
    {
        return doIsExpired( makeKeyFromPath( path ) );
    }

    public final void put( String path, Object element )
    {
        doPut( makeKeyFromPath( path ), element, -1 );
    }

    public final void put( String path, Object element, int expiration )
    {
        doPut( makeKeyFromPath( path ), element, expiration );
    }

    public final boolean remove( String path )
    {
        if ( contains( path ) )
        {
            return doRemove( makeKeyFromPath( path ) );
        }
        else
        {
            return false;
        }
    }

    public final boolean removeWithParents( String path )
    {
        boolean result = remove( path );
        int lastSlash = path.lastIndexOf( "/" );
        while ( lastSlash > -1 )
        {
            path = path.substring( 0, lastSlash );
            boolean r = remove( path );
            result = result || r;
            lastSlash = path.lastIndexOf( "/" );
        }
        return result;
    }

    public abstract boolean removeWithChildren( String path );

    public final void purge()
    {
        doPurge();
    }

    protected String makeKeyFromPath( String path )
    {
        while ( path.startsWith( "/" ) )
        {
            path = path.substring( 1 );
        }

        while ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        return path;
    }

    public abstract boolean doContains( String key );

    public abstract boolean doIsExpired( String key );

    public abstract void doPut( String key, Object element, int expiration );

    public abstract boolean doRemove( String key );

    public abstract void doPurge();

}
