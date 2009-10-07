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
 * A walker filter that can filters items based on item type (instanceof). If more types are provided it will allow
 * sorage items that matches at least one of them.
 *
 * @author Alin Dreghiciu
 */
public class InstanceOfWalkerFilter
    implements WalkerFilter
{

    /**
     * Allowed types (can be null or empty).
     */
    private final Class<? extends StorageItem>[] m_allowed;

    /**
     * Constructor.
     *
     * @param allowedClasses allowed types (can be null or empty)
     */
    public InstanceOfWalkerFilter( final Class<? extends StorageItem>... allowedClasses )
    {
        m_allowed = allowedClasses;
    }

    /**
     * Checks if item is instance of any of allowed classes.<br/>
     * If no classes were provided returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcess( final WalkerContext context,
                                  final StorageItem item )
    {
        if ( m_allowed == null || m_allowed.length == 0 )
        {
            return true;
        }
        for ( Class<? extends StorageItem> clazz : m_allowed )
        {
            if ( clazz.isAssignableFrom( item.getClass() ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Always returns true.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcessRecursively( final WalkerContext context,
                                             final StorageCollectionItem coll )
    {
        return true;
    }

    /**
     * Builder method.
     *
     * @param allowedClasses allowed types (can be null or empty)
     *
     * @return conjunction between filters
     */
    public static InstanceOfWalkerFilter anyInstanceOf( final Class<? extends StorageItem>... allowedClasses )
    {
        return new InstanceOfWalkerFilter( allowedClasses );
    }

}