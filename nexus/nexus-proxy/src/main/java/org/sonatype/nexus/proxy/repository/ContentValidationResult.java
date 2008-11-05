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

package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.feeds.NexusArtifactEvent;

public class ContentValidationResult
{

    private final List<NexusArtifactEvent> events = new ArrayList<NexusArtifactEvent>();
    private boolean contentValid;

    public boolean isContentValid()
    {
        return contentValid;
    }

    public List<NexusArtifactEvent> getEvents()
    {
        return events;
    }

    public void addEvent( NexusArtifactEvent event )
    {
        events.add( event );
    }

    public void setContentValid( boolean contentValid )
    {
        this.contentValid = contentValid;
    }

}
