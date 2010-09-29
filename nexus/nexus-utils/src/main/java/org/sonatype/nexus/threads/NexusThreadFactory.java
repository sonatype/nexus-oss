/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
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
