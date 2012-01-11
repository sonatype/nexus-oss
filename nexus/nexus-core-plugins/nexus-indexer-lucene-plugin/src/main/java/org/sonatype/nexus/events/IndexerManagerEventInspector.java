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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector that maintains indexes.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "LuceneIndexerManagerEventInspector" )
public class IndexerManagerEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
    private final boolean enabled = SystemPropertiesHelper.getBoolean(
        "org.sonatype.nexus.events.IndexerManagerEventInspector.enabled", true );

    @Requirement
    private IndexerManager indexerManager;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( Event<?> evt )
    {
        // listen for STORE, CACHE, DELETE only
        return enabled
            && ( evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache || evt instanceof RepositoryItemEventDelete );
    }

    public void inspect( Event<?> evt )
    {
        if ( enabled )
        {
            inspectForIndexerManager( evt );
        }
    }

    private void inspectForIndexerManager( Event<?> evt )
    {
        try
        {
            RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

            // should we sync at all
            if ( ievt.getRepository().isIndexable() )
            {
                if ( ievt instanceof RepositoryItemEventCache || ievt instanceof RepositoryItemEventStore )
                {
                    getIndexerManager().addItemToIndex( ievt.getRepository(), ievt.getItem() );
                }
                else if ( ievt instanceof RepositoryItemEventDelete )
                {
                    getIndexerManager().removeItemFromIndex( ievt.getRepository(), ievt.getItem() );
                }
            }
        }
        catch ( Exception e ) // TODO be more specific
        {
            getLogger().error( "Could not maintain index!", e );
        }
    }

}
