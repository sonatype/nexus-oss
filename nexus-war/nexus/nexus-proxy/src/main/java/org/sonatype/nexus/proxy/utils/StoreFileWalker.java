/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
 */
public abstract class StoreFileWalker
    extends StoreWalker
{

    public StoreFileWalker( ResourceStore store, Logger logger )
    {
        super( store, logger );
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
