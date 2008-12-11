/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.events;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it.
 * 
 * @author cstamas
 */
@Component( role = EventInspectorHost.class )
public class DefaultEventInspectorHost
    extends AbstractLogEnabled
    implements EventInspectorHost
{
    @Requirement( role = EventInspector.class )
    private Map<String, EventInspector> eventInspectors;

    public void processEvent( AbstractEvent evt )
    {
        for ( Map.Entry<String, EventInspector> entry : eventInspectors.entrySet() )
        {
            EventInspector ei = entry.getValue();

            try
            {
                if ( ei.accepts( evt ) )
                {
                    ei.inspect( evt );
                }
            }
            catch ( Throwable e )
            {
                getLogger().warn(
                    "EventInspector hint='" + entry.getKey() + "' class='" + ei.getClass().getName()
                        + "' had problem inspecting an event='" + evt.getClass() + "'",
                    e );
            }
        }
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        processEvent( evt );
    }

}
