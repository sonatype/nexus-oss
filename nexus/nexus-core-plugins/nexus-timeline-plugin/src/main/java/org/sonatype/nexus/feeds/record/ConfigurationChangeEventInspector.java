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
package org.sonatype.nexus.feeds.record;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.record.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "ConfigurationChangeEvent" )
public class ConfigurationChangeEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
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

        // TODO: refine _what_ is changed
        /*
         * for ( Configurable change : event.getChanges() ) { msg.append( " '" ).append( change.getName() ).append(
         * "', " ); }
         */

        if ( event.getUserId() != null )
        {
            msg.append( ", change was made by [" + event.getUserId() + "]" );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, msg.toString() );
    }
}
