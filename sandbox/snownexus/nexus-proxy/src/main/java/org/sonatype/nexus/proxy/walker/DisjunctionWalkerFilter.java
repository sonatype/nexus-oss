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
 * A logical OR between two or more walker filters.
 *
 * @author Alin Dreghiciu
 */
public class DisjunctionWalkerFilter
    implements WalkerFilter
{

    /**
     * OR-ed filters (can be null or empty).
     */
    private final WalkerFilter[] m_filters;

    /**
     * Constructor.
     *
     * @param filters OR-ed filters (can be null or empty)
     */
    public DisjunctionWalkerFilter( final WalkerFilter... filters )
    {
        m_filters = filters;
    }

    /**
     * Performs a logical OR between results of calling {@link #shouldProcess(WalkerContext, StorageItem)} on all
     * filters. It will exit at first filter that returns true. <br/>
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
            if ( filter.shouldProcess( context, item ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a logical OR between results of calling
     * {@link #shouldProcessRecursively(WalkerContext, StorageCollectionItem)}  on all filters. It will exit at first
     * filter that returns true.<br/>
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
                return true;
            }
        }
        return false;
    }

    /**
     * Builder method.
     *
     * @param filters OR-ed filters (can be null or empty)
     *
     * @return disjunction between filters
     */
    public static DisjunctionWalkerFilter statisfiesOneOf( final WalkerFilter... filters )
    {
        return new DisjunctionWalkerFilter( filters );
    }

}
