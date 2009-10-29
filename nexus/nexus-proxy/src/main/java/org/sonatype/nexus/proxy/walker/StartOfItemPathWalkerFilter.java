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
 * A walker filter that accepts items with a path that starts with a specified path.
 *
 * @author Alin Dreghiciu
 */
public class StartOfItemPathWalkerFilter
    implements WalkerFilter
{

    /**
     * The path that item path should start with.
     */
    private final String path;

    /**
     * Constructor.
     *
     * @param path the path that item path should start with
     */
    public StartOfItemPathWalkerFilter( final String path )
    {
        assert path != null : "Path must be specified (cannot be null)";

        this.path = path;
    }

    /**
     * Return "true" if the item path starts with specified path.
     *
     * {@inheritDoc}
     */
    public boolean shouldProcess( final WalkerContext ctx,
                                  final StorageItem item )
    {
        return item.getPath().matches( path );
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldProcessRecursively( final WalkerContext ctx,
                                             final StorageCollectionItem coll )
    {
        return coll.getPath().matches( path );
    }

    /**
     * Builder method.
     *
     * @return new ItemNameWalkerFilter
     */
    public static StartOfItemPathWalkerFilter pathStartsWith( final String path )
    {
        return new StartOfItemPathWalkerFilter( path );
    }

}
