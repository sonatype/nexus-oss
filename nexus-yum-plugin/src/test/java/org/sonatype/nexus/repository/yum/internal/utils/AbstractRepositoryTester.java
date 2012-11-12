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
package org.sonatype.nexus.repository.yum.internal.utils;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils.BASE_CACHE_DIR;
import static org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils.BASE_TMP_FILE;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import com.google.code.tempusfugit.temporal.Condition;

public abstract class AbstractRepositoryTester
    extends AbstractYumNexusTestCase
{

    private static final String SNAPSHOTS = "snapshots";

    @Inject
    private NexusScheduler nexusScheduler;

    @After
    public void waitForThreadPool()
        throws Exception
    {
        waitFor( new Condition()
        {
            @Override
            public boolean isSatisfied()
            {
                int running = 0;
                for ( Entry<String, List<ScheduledTask<?>>> entry : nexusScheduler.getActiveTasks().entrySet() )
                {
                    for ( ScheduledTask<?> task : entry.getValue() )
                    {
                        if ( RUNNING.equals( task.getTaskState() ) )
                        {
                            running++;
                        }
                    }
                }
                return running == 0;
            }
        } );
    }

    @Before
    public void cleanUpCacheDirectory()
        throws Exception
    {
        deleteDirectory( BASE_TMP_FILE );
        BASE_CACHE_DIR.mkdirs();
    }

    protected MavenRepository createRepository( final boolean isMavenHostedRepository )
    {
        return createRepository( isMavenHostedRepository, SNAPSHOTS );
    }

    protected MavenRepository createRepository( final boolean isMavenHostedRepository, final String repoId )
    {
        final RepositoryKind kind = mock( RepositoryKind.class );
        when( kind.isFacetAvailable( MavenHostedRepository.class ) ).thenReturn( isMavenHostedRepository );

        final MavenHostedRepository repository = mock( MavenHostedRepository.class );
        when( repository.getRepositoryKind() ).thenReturn( kind );
        when( repository.getId() ).thenReturn( repoId );
        when( repository.getProviderRole() ).thenReturn( Repository.class.getName() );
        when( repository.getProviderHint() ).thenReturn( "maven2" );

        if ( isMavenHostedRepository )
        {
            when( repository.adaptToFacet( MavenHostedRepository.class ) ).thenReturn( repository );
        }
        else
        {
            when( repository.adaptToFacet( MavenHostedRepository.class ) ).thenThrow( new ClassCastException() );
        }

        final File repoDir = new File( BASE_TMP_FILE, "tmp-repos/" + repoId );
        repoDir.mkdirs();
        when( repository.getLocalUrl() ).thenReturn( repoDir.toURI().toString() );

        return repository;
    }

    protected StorageItem createItem( String version, String filename )
    {
        final StorageItem item = mock( StorageItem.class );

        when( item.getPath() ).thenReturn( "blalu/" + version + "/" + filename );
        when( item.getParentPath() ).thenReturn( "blalu/" + version );
        when( item.getItemContext() ).thenReturn( new RequestContext() );

        return item;
    }
}
