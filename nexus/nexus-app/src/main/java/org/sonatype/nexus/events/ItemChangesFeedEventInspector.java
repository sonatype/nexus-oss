/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.events.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector that creates feeds about item changes.
 * 
 * @author Juven Xu
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ItemChangesFeedEventInspector" )
public class ItemChangesFeedEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof RepositoryItemEvent )
        {
            return true;
        }

        return false;
    }

    public void inspect( Event<?> evt )
    {
        inspectForNexus( evt );
    }

    private void inspectForNexus( Event<?> evt )
    {
        RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

        if ( ievt instanceof RepositoryItemEventRetrieve )
        {
            // RETRIEVE event creates a lot of noise in events,
            // so we are not processing those
            return;
        }

        if ( ievt.getItemUid().getPath().endsWith( ".pom" ) || ievt.getItemUid().getPath().endsWith( ".jar" ) )
        {
            // filter out links and dirs/collections
            if ( StorageFileItem.class.isAssignableFrom( ievt.getItem().getClass() ) )
            {
                StorageFileItem pomItem = (StorageFileItem) ievt.getItem();

                NexusItemInfo ai = new NexusItemInfo();
                ai.setRepositoryId( pomItem.getRepositoryId() );
                ai.setPath( pomItem.getPath() );
                ai.setRemoteUrl( pomItem.getRemoteUrl() );

                String action;

                if ( ievt instanceof RepositoryItemEventCache )
                {
                    action = NexusArtifactEvent.ACTION_CACHED;
                }
                else if ( ievt instanceof RepositoryItemEventStore )
                {
                    action = NexusArtifactEvent.ACTION_DEPLOYED;
                }
                else if ( ievt instanceof RepositoryItemEventDelete )
                {
                    action = NexusArtifactEvent.ACTION_DELETED;
                }
                else
                {
                    return;
                }

                NexusArtifactEvent nae = new NexusArtifactEvent( ievt.getEventDate(), action, "", ai );
                // set context
                nae.addEventContext( ievt.getItemContext() );
                // set attributes
                nae.addItemAttributes( ievt.getItem().getAttributes() );

                getFeedRecorder().addNexusArtifactEvent( nae );
            }
        }
    }
}
