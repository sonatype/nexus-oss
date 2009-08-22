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
 * A logical NOT on a filter.
 *
 * @author Alin Dreghiciu
 */
public class NegationWalkerFilter
    implements WalkerFilter
{

    /**
     * Negated filter (can be null).
     */
    private final WalkerFilter m_filter;

    /**
     * Constructor.
     *
     * @param filter negated filter (can be null)
     */
    public NegationWalkerFilter( final WalkerFilter filter )
    {
        m_filter = filter;
    }

    /**
     * Performs a logical NOT on result of calling {@link #shouldProcess(WalkerContext, StorageItem)} on negated filter.
     * <br/>
     * If no filter was provided returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcess( final WalkerContext context,
                                  final StorageItem item )
    {
        return m_filter == null || !m_filter.shouldProcess( context, item );
    }

    /**
     * Performs a logical NOT on result of calling
     * {@link #shouldProcessRecursively(WalkerContext, StorageCollectionItem)} on negated filter.<br/>
     * If no filter was provided returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcessRecursively( final WalkerContext context,
                                             final StorageCollectionItem coll )
    {
        return m_filter == null || !m_filter.shouldProcessRecursively( context, coll );
    }

    /**
     * Builder method.
     *
     * @param filter negated filter (can be null or empty)
     *
     * @return disjunction between filters
     */
    public static NegationWalkerFilter not( final WalkerFilter filter )
    {
        return new NegationWalkerFilter( filter );
    }

}