/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventRemove;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryRegistryGroupEvent" )
public class RepositoryRegistryGroupEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private IndexerManager indexerManager;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryRegistryGroupEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        try
        {
            RepositoryRegistryGroupEvent gevt = (RepositoryRegistryGroupEvent) evt;

            // we are handling repo events, like addition and removal
            if ( RepositoryRegistryGroupEventAdd.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().addRepositoryGroupIndexContext( gevt.getGroupId() );
            }
            else if ( RepositoryRegistryGroupEventRemove.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().removeRepositoryGroupIndexContext( gevt.getGroupId(), false );
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Could not maintain group (merged) indexing contexts!", e );
        }

    }

}
