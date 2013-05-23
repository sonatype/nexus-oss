/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.scheduling;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryImpl
    implements ThreadFactory
{
    private static final AtomicInteger poolNumber = new AtomicInteger( 1 );

    private final AtomicInteger threadNumber = new AtomicInteger( 1 );

    private final String namePrefix;

    private final ThreadGroup schedulerThreadGroup;

    private final int threadPriority;

    public ThreadFactoryImpl()
    {
        this( Thread.MIN_PRIORITY );
    }

    public ThreadFactoryImpl(int threadPriority)
    {
        super();

        int poolNum = poolNumber.getAndIncrement();

        this.schedulerThreadGroup = new ThreadGroup( "Sisu scheduler #" + poolNum );

        this.namePrefix = "pxpool-" + poolNum + "-thread-";

        this.threadPriority = threadPriority;
    }

    public Thread newThread( Runnable r )
    {
        Thread result = new Thread( getSchedulerThreadGroup(), r, namePrefix + threadNumber.getAndIncrement() );

        result.setDaemon( false );

        result.setPriority( this.threadPriority );

        return result;
    }

    public ThreadGroup getSchedulerThreadGroup()
    {
        return this.schedulerThreadGroup;
    }
}
