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
package org.sonatype.nexus.schedule;

import java.util.concurrent.ThreadFactory;

import org.codehaus.plexus.PlexusContainer;

public class PlexusThreadFactory
    implements ThreadFactory
{
    private PlexusContainer plexusContainer;

    private ThreadGroup schedulerThreadGroup;

    public PlexusThreadFactory( PlexusContainer plexusContainer )
    {
        super();

        this.plexusContainer = plexusContainer;

        this.schedulerThreadGroup = new ThreadGroup( "Plexus scheduler" );
    }

    public Thread newThread( Runnable r )
    {
        Thread result = new Thread( schedulerThreadGroup, r );

        result.setContextClassLoader( plexusContainer.getLookupRealm() );

        return result;
    }

    public ThreadGroup getSchedulerThreadGroup()
    {
        return this.schedulerThreadGroup;
    }

}
