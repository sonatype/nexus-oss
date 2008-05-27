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
package org.sonatype.nexus.scheduling;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.PlexusContainer;

public class PlexusThreadFactory
    implements ThreadFactory
{
    private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

    private final AtomicInteger threadNumber = new AtomicInteger( 1 );

    private final String namePrefix;

    private final PlexusContainer plexusContainer;

    private final ThreadGroup schedulerThreadGroup;

    public PlexusThreadFactory( PlexusContainer plexusContainer )
    {
        super();

        this.plexusContainer = plexusContainer;

        int poolNum = poolNumber.getAndIncrement();

        this.schedulerThreadGroup = new ThreadGroup( "Plexus scheduler #" + poolNum );

        this.namePrefix = "pxpool-" + poolNum + "-thread-";
    }

    public Thread newThread( Runnable r )
    {
        Thread result = new Thread( schedulerThreadGroup, r, namePrefix + threadNumber.getAndIncrement() );

        result.setContextClassLoader( plexusContainer.getLookupRealm() );

        result.setDaemon( false );

        return result;
    }

    public ThreadGroup getSchedulerThreadGroup()
    {
        return this.schedulerThreadGroup;
    }

}
