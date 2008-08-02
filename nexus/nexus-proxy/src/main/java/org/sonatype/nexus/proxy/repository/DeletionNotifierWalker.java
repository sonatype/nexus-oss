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
package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

public class DeletionNotifierWalker
    extends StoreFileWalker
{
    private Repository repository;

    private Map<String, Object> context;

    public DeletionNotifierWalker( Repository repository, Logger logger, Map<String, Object> context )
    {
        super( repository, logger );

        this.repository = repository;

        this.context = context;
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        if ( context != null )
        {
            item.getItemContext().putAll( context );
        }

        // just fire it, and someone will eventually catch it
        repository.notifyProximityEventListeners( new RepositoryItemEventDelete( item ) );
    }

}
