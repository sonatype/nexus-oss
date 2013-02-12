/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.util.task.ProgressListener;
import org.sonatype.nexus.util.task.RunnableSupport;

/**
 * Job that aggregates multiple {@link WLUpdateRepositoryRunnable} jobs into one job and executes them sequentially in
 * the {@link Thread} that executes this job.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WLUpdateRunnable
    extends RunnableSupport
{
    private final ApplicationStatusSource applicationStatusSource;

    private final List<WLUpdateRepositoryRunnable> mavenRepositoryJobs;

    /**
     * Constructor.
     * 
     * @param progressListener
     * @param applicationStatusSource
     * @param mavenRepositoryJobs
     */
    public WLUpdateRunnable( final ProgressListener progressListener,
                             final ApplicationStatusSource applicationStatusSource,
                             final List<WLUpdateRepositoryRunnable> mavenRepositoryJobs )
    {
        super( progressListener );
        this.applicationStatusSource = applicationStatusSource;
        this.mavenRepositoryJobs = checkNotNull( mavenRepositoryJobs );
    }

    @Override
    protected void doRun()
    {
        for ( WLUpdateRepositoryRunnable mavenRepositoryJob : mavenRepositoryJobs )
        {
            if ( !applicationStatusSource.getSystemStatus().isNexusStarted() )
            {
                getLogger().warn( "Nexus stopped during background WL updates, bailing out." );
                break;
            }
            if ( isCanceled() )
            {
                getLogger().warn( "Update job canceled, bailing out." );
                break;
            }
            mavenRepositoryJob.run();
        }
    }
}