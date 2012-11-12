/**
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
package org.sonatype.nexus.repository.yum.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.repository.yum.YumRepository;
import org.sonatype.nexus.repository.yum.internal.task.RepositoryScanningTask;
import org.sonatype.nexus.repository.yum.internal.task.YumGroupRepositoryGenerationTask;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.YumRegistry;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

@Named
@Singleton
public class YumRegistryImpl
    implements YumRegistry
{

    private static final Logger LOG = LoggerFactory.getLogger( YumRegistryImpl.class );

    private final Map<String, Yum> yums = new ConcurrentHashMap<String, Yum>();

    private final NexusScheduler nexusScheduler;

    private final YumFactory yumFactory;

    @Inject
    public YumRegistryImpl( final NexusScheduler nexusScheduler,
                            final YumFactory yumFactory )
    {
        this.nexusScheduler = checkNotNull( nexusScheduler );
        this.yumFactory = checkNotNull( yumFactory );
    }

    @Override
    public Yum register( final MavenRepository repository )
    {
        if ( !yums.containsKey( repository.getId() ) )
        {
            final Yum yum = yumFactory.create( repository );
            yums.put( repository.getId(), yum );

            LOG.info( "Marked repository as RPM-repository : {}", repository.getId() );

            runScanningTask( yum );

            return yum;
        }
        return yums.get( repository.getId() );
    }

    @Override
    public Yum unregister( final String repositoryId )
    {
        return yums.remove( repositoryId );
    }

    @Override
    public Yum get( final String repositoryId )
    {
        return yums.get( repositoryId );
    }

    private void runScanningTask( final Yum yum )
    {
        RepositoryScanningTask task = nexusScheduler.createTaskInstance( RepositoryScanningTask.class );
        task.setYum( yum );
        nexusScheduler.submit( RepositoryScanningTask.ID, task );
    }

    @Override
    public boolean isRegistered( String repositoryId )
    {
        return yums.containsKey( repositoryId );
    }

    @Override
    public ScheduledTask<YumRepository> createGroupRepository( GroupRepository groupRepository )
    {
        YumGroupRepositoryGenerationTask task =
            nexusScheduler.createTaskInstance( YumGroupRepositoryGenerationTask.class );
        task.setGroupRepository( groupRepository );
        return nexusScheduler.submit( YumGroupRepositoryGenerationTask.ID, task );
    }

}
