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
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "ConfigurationChangeEvent" )
public class ConfigurationChangeEventInspector
    extends AbstractFeedRecorderEventInspector
{

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof ConfigurationChangeEvent );
    }

    public void inspect( Event<?> evt )
    {
        inspectForNexus( evt );
    }

    private void inspectForNexus( Event<?> evt )
    {
        // TODO: This causes cycle!
        // getNexus().getSystemStatus().setLastConfigChange( new Date() );

        ConfigurationChangeEvent event = (ConfigurationChangeEvent) evt;

        if ( event.getChanges().isEmpty() )
        {
            return;
        }

        StringBuffer msg = new StringBuffer();

        msg.append( "Nexus server configuration was changed" );

        //TODO: refine _what_ is changed
/*        for ( Configurable change : event.getChanges() )
        {
            msg.append( " '" ).append( change.getName() ).append( "', " );
        }*/

        if ( event.getSubject() != null && event.getSubject().getPrincipal() != null )
        {
            msg.append( ", change was made by [" + event.getSubject().getPrincipal().toString() + "]" );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, msg.toString() );
    }
}
