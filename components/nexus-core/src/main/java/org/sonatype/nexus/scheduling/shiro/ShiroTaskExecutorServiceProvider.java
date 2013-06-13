/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.scheduling.shiro;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.scheduling.TaskScheduledExecutorService;
import org.sonatype.scheduling.ThreadFactoryImpl;

/**
 * {@link TaskExecutorServiceProvider} implementation that provides Shiro specific
 * {@link ShiroFixedSubjectScheduledExecutorService} implementation task executors, to make scheduled task share a valid
 * subject.
 * 
 * @author cstamas
 * @since 2.6
 */
@Singleton
@Named
@TaskScheduledExecutorService
public class ShiroTaskExecutorServiceProvider
    implements Provider<ScheduledExecutorService>
{
    private final ShiroFixedSubjectScheduledExecutorService shiroFixedSubjectScheduledExecutorService;

    public ShiroTaskExecutorServiceProvider()
    {
        final ScheduledThreadPoolExecutor target =
            (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool( 20, new ThreadFactoryImpl(
                Thread.MIN_PRIORITY ) );
        target.setExecuteExistingDelayedTasksAfterShutdownPolicy( false );
        target.setContinueExistingPeriodicTasksAfterShutdownPolicy( false );
        shiroFixedSubjectScheduledExecutorService =
            new ShiroFixedSubjectScheduledExecutorService( target, FakeAlmightySubject.TASK_SUBJECT );
    }

    @Override
    public ScheduledExecutorService get()
    {
        return shiroFixedSubjectScheduledExecutorService;
    }
}
