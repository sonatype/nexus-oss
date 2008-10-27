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

import java.util.Date;

/**
 * The superclass for all Nexus events.
 * 
 * @author cstamas
 */
public abstract class AbstractEvent
{
    /** The event date. */
    private final Date eventDate;

    /**
     * Instantiates a new abstract event.
     */
    public AbstractEvent()
    {
        super();

        this.eventDate = new Date();
    }

    /**
     * Gets the event date.
     * 
     * @return the event date
     */
    public Date getEventDate()
    {
        return eventDate;
    }
}
