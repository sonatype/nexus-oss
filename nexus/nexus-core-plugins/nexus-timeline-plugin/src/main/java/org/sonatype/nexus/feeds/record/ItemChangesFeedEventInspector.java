/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.feeds.record;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.record.AbstractFeedRecorderEventInspector;
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
                nae.addItemAttributes( ievt.getItem().getRepositoryItemAttributes().asMap() );

                getFeedRecorder().addNexusArtifactEvent( nae );
            }
        }
    }
}
