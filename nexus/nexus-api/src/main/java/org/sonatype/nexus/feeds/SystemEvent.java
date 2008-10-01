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
package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that encapsulates a Nexus System event, like boot, reconfiguration, etc.
 * 
 * @author cstamas
 */
public class SystemEvent
{
    /**
     * The date of event.
     */
    private Date eventDate;

    /**
     * The action.
     */
    private final String action;

    /**
     * Human message/descritpion.
     */
    private final String message;

    /**
     * The context of event.
     */
    private final Map<String, Object> eventContext;

    public SystemEvent( String action, String message )
    {
        super();

        this.eventDate = new Date();

        this.action = action;

        this.message = message;

        this.eventContext = new HashMap<String, Object>();
    }

    public Date getEventDate()
    {
        return eventDate;
    }
    
    public void setEventDate(Date date)
    {
        this.eventDate = date;
    }

    public Map<String, Object> getEventContext()
    {
        return eventContext;
    }

    public String getAction()
    {
        return action;
    }

    public String getMessage()
    {
        return message;
    }
    
    public String toString()
    {
        return getMessage();
    }
}
