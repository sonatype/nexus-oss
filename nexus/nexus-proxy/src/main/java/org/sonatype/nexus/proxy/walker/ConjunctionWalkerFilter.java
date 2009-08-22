/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A logical AND between two or more walker filters.
 *
 * @author Alin Dreghiciu
 */
public class ConjunctionWalkerFilter
    implements WalkerFilter
{

    /**
     * AND-ed filters (can be null or empty).
     */
    private final WalkerFilter[] m_filters;

    /**
     * Constructor.
     *
     * @param filters AND-ed filters (can be null or empty)
     */
    public ConjunctionWalkerFilter( final WalkerFilter... filters )
    {
        m_filters = filters;
    }

    /**
     * Performs a logical AND between results of calling {@link #shouldProcess(org.sonatype.nexus.proxy.walker.WalkerContext, org.sonatype.nexus.proxy.item.StorageItem)} on all
     * filters. It will exit at first filter that returns false. <br/>
     * If no filters were provided returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcess( final WalkerContext context,
                                  final StorageItem item )
    {
        if ( m_filters == null || m_filters.length == 0 )
        {
            return true;
        }
        for ( WalkerFilter filter : m_filters )
        {
            if ( !filter.shouldProcess( context, item ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a logical AND between results of calling
     * {@link #shouldProcessRecursively(org.sonatype.nexus.proxy.walker.WalkerContext, org.sonatype.nexus.proxy.item.StorageCollectionItem)}  on all filters. It will exit at first
     * filter that returns false.<br/>
     * If no filters were provided returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcessRecursively( final WalkerContext context,
                                             final StorageCollectionItem coll )
    {
        if ( m_filters == null || m_filters.length == 0 )
        {
            return true;
        }
        for ( WalkerFilter filter : m_filters )
        {
            if ( !filter.shouldProcessRecursively( context, coll ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Builder method.
     *
     * @param filters AND-ed filters (can be null or empty)
     *
     * @return conjunction between filters
     */
    public static ConjunctionWalkerFilter satisfiesAllOf( final WalkerFilter... filters )
    {
        return new ConjunctionWalkerFilter( filters );
    }

}