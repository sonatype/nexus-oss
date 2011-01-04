/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NexusThreadFactory
    implements ThreadFactory
{
    private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

    private final AtomicInteger threadNumber = new AtomicInteger( 1 );

    private final String namePrefix;

    private final ThreadGroup schedulerThreadGroup;

    private final boolean deamonThread;

    private int threadPriority;

    public NexusThreadFactory( String poolId, String name )
    {
        this( poolId, name, Thread.NORM_PRIORITY );
    }

    public NexusThreadFactory( final String poolId, final String threadGroupName, final int threadPriority )
    {
        this( poolId, threadGroupName, threadPriority, false );
    }

    public NexusThreadFactory( final String poolId, final String threadGroupName, final int threadPriority,
                               final boolean daemonThread )
    {
        super();

        int poolNum = poolNumber.getAndIncrement();

        this.schedulerThreadGroup = new ThreadGroup( threadGroupName + " #" + poolNum );

        this.namePrefix = poolId + "-" + poolNum + "-thread-";

        this.deamonThread = daemonThread;

        this.threadPriority = threadPriority;
    }

    public Thread newThread( Runnable r )
    {
        Thread result = new Thread( schedulerThreadGroup, r, namePrefix + threadNumber.getAndIncrement() );

        result.setDaemon( this.deamonThread );

        result.setPriority( this.threadPriority );

        return result;
    }

    public ThreadGroup getSchedulerThreadGroup()
    {
        return this.schedulerThreadGroup;
    }

    public int getThreadPriority()
    {
        return threadPriority;
    }

    public void setThreadPriority( int threadPriority )
    {
        this.threadPriority = threadPriority;
    }
}
