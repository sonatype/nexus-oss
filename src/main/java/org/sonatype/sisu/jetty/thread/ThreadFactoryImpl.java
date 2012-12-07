/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * Simple thread factory implementation that puts Jetty pooled threads into it's own group and installs
 * {@link UncaughtExceptionHandler} to created threads.
 * 
 * @author cstamas
 * @since 1.3
 */
public class ThreadFactoryImpl
    implements ThreadFactory
{
    private final ThreadGroup group;

    private final UncaughtExceptionHandler uncaughtExceptionHandler;

    public ThreadFactoryImpl()
    {
        this( new ThreadGroup( "Jetty8" ), new LoggingUncaughtExceptionHandler() );
    }

    public ThreadFactoryImpl( final ThreadGroup group, final UncaughtExceptionHandler uncaughtExceptionHandler )
    {
        this.group = group;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public Thread newThread( final Runnable runnable )
    {
        final Thread t = new Thread( group, runnable );
        t.setUncaughtExceptionHandler( uncaughtExceptionHandler );
        return t;
    }
}
