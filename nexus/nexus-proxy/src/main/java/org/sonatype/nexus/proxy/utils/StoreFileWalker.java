/**
 * Sonatype NexusTM [Open Source Version].
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
package org.sonatype.nexus.proxy.utils;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class StoreFileWalker.
 * 
 * @author cstamas
 * @deprecated use Walker service in org.sonatype.nexus.proxy.walker package
 */
public abstract class StoreFileWalker
    extends StoreWalker
{

    public StoreFileWalker( ResourceStore store, Logger logger )
    {
        super( store, logger );
    }

    public StoreFileWalker( ResourceStore store, Logger logger, StoreWalkerFilter filter )
    {
        super( store, logger, filter );
    }

    protected final void processItem( StorageItem item )
    {
        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                processFileItem( (StorageFileItem) item );
            }
            catch ( Exception ex )
            {
                if ( getLogger() != null )
                {
                    getLogger().warn( "Got exception during file item walking!", ex );
                }
            }
        }
    }

    /**
     * Process file item.
     * 
     * @param store the store
     * @param fItem the f item
     * @param logger the logger
     */
    protected abstract void processFileItem( StorageFileItem fItem );

}
