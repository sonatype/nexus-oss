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

import java.io.IOException;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.util.task.CancelableRunnableSupport;
import org.sonatype.nexus.util.task.ProgressListener;

/**
 * Job that performs WL update and publishing of one single {@link MavenRepository}.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WLUpdateRepositoryRunnable
    extends CancelableRunnableSupport
{
    private final ApplicationStatusSource applicationStatusSource;

    private final WLManagerImpl wlManager;

    private final MavenRepository mavenRepository;

    /**
     * Constructor.
     * 
     * @param progressListener
     * @param applicationStatusSource
     * @param wlManager
     * @param mavenRepository
     */
    public WLUpdateRepositoryRunnable( final ProgressListener progressListener,
                                       final ApplicationStatusSource applicationStatusSource,
                                       final WLManagerImpl wlManager, final MavenRepository mavenRepository )
    {
        super( progressListener, mavenRepository.getId() + " WL-updater" );
        this.applicationStatusSource = checkNotNull( applicationStatusSource );
        this.wlManager = checkNotNull( wlManager );
        this.mavenRepository = checkNotNull( mavenRepository );
    }

    @Override
    protected void doRun()
    {
        if ( !applicationStatusSource.getSystemStatus().isNexusStarted() )
        {
            getLogger().warn( "Nexus stopped during background WL updates, bailing out." );
            return;
        }
        try
        {
            wlManager.updateAndPublishWhitelist( mavenRepository, true );
        }
        catch ( Exception e )
        {
            getLogger().warn( "Problem during WL update of {}",
                RepositoryStringUtils.getHumanizedNameString( mavenRepository ), e );
            try
            {
                wlManager.unpublish( mavenRepository );
            }
            catch ( IOException ioe )
            {
                // silently
            }
        }
    }
}