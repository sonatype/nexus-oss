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
package org.sonatype.nexus.proxy;

import java.util.concurrent.CopyOnWriteArrayList;

import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.EventMulticaster;

/**
 * The Class ProximityEventMulticasterComponent implements multicasting. Used by Repository and Registry
 * implementations.
 * 
 * @author cstamas
 */
public class EventMulticasterComponent
    extends LoggingComponent
    implements EventMulticaster
{
    /** The proximity event listeners. */
    private CopyOnWriteArrayList<EventListener> proximityEventListeners = new CopyOnWriteArrayList<EventListener>();

    public void addProximityEventListener( EventListener listener )
    {
        proximityEventListeners.add( listener );
    }

    public void removeProximityEventListener( EventListener listener )
    {
        proximityEventListeners.remove( listener );
    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        for ( EventListener l : proximityEventListeners )
        {
            try
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Notifying EventListener " + l.getClass().getName() + " about event "
                            + evt.getClass().getName() + " fired (" + evt.toString() + ")" );
                }

                l.onProximityEvent( evt );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in listener", e );
            }
        }
    }

}
