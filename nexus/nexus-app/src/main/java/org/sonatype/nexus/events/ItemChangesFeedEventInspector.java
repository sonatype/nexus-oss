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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.events.AbstractFeedRecorderEventInspector;
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

                NexusArtifactEvent nae = new NexusArtifactEvent();
                NexusItemInfo ai = new NexusItemInfo();
                ai.setRepositoryId( pomItem.getRepositoryId() );
                ai.setPath( pomItem.getPath() );
                ai.setRemoteUrl( pomItem.getRemoteUrl() );
                nae.setNexusItemInfo( ai );
                nae.setEventDate( ievt.getEventDate() );

                // Make sure to add the item attributes as well
                // that is where remote ip is contained (among other things)
                nae.setEventContext( ievt.getContext() );
                nae.getEventContext().putAll( ievt.getItem().getAttributes() );

                if ( ievt instanceof RepositoryItemEventCache )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_CACHED );
                }
                else if ( ievt instanceof RepositoryItemEventStore )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_DEPLOYED );
                }
                else if ( ievt instanceof RepositoryItemEventDelete )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_DELETED );
                }
                else
                {
                    return;
                }

                getFeedRecorder().addNexusArtifactEvent( nae );
            }

        }
    }
}
