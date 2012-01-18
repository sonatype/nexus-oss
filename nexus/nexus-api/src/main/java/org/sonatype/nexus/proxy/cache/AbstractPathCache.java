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

    public final boolean purge()
    {
        return doPurge();
    }

    // ==

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

    protected abstract boolean doContains( String key );

    protected abstract boolean doIsExpired( String key );

    protected abstract void doPut( String key, Object element, int expiration );

    protected abstract boolean doRemove( String key );

    protected abstract boolean doPurge();
}
