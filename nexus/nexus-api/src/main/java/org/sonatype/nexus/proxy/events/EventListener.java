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
package org.sonatype.nexus.proxy.events;

/**
 * The listener interface for receiving proximityEvent events.
 * The class that is interested in processing a proximityEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addProximityEventListener<code> method. When
 * the proximityEvent event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AbstractEvent
 */
public interface EventListener
{

    /**
     * On proximity event.
     * 
     * @param evt the evt
     */
    void onProximityEvent( AbstractEvent evt );

}
