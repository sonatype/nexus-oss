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
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.repository.LocalStatus;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryEventLocalStatusChanged" )
public class RepositoryEventLocalStatusChangedInspector
    extends AbstractFeedRecorderEventInspector
{

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryEventLocalStatusChanged )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        RepositoryEventLocalStatusChanged revt = (RepositoryEventLocalStatusChanged) evt;

        StringBuffer sb = new StringBuffer( "The repository '" );

        sb.append( revt.getRepository().getName() );

        sb.append( "' (ID='" ).append( revt.getRepository().getId() ).append( "') was put " );

        if ( LocalStatus.IN_SERVICE.equals( revt.getRepository().getLocalStatus() ) )
        {
            sb.append( "IN SERVICE." );
        }
        else if ( LocalStatus.OUT_OF_SERVICE.equals( revt.getRepository().getLocalStatus() ) )
        {
            sb.append( "OUT OF SERVICE." );
        }
        else
        {
            sb.append( revt.getRepository().getLocalStatus().toString() ).append( "." );
        }

        sb.append( " The previous state was " );

        if ( LocalStatus.IN_SERVICE.equals( revt.getOldLocalStatus() ) )
        {
            sb.append( "IN SERVICE." );
        }
        else if ( LocalStatus.OUT_OF_SERVICE.equals( revt.getOldLocalStatus() ) )
        {
            sb.append( "OUT OF SERVICE." );
        }
        else
        {
            sb.append( revt.getOldLocalStatus().toString() ).append( "." );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION, sb.toString() );
    }

}
