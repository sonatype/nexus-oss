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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils.createRepository;

import java.util.concurrent.TimeoutException;
import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.repository.yum.YumRegistry;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractRepositoryTester;
import org.sonatype.nexus.scheduling.NexusScheduler;
import com.google.code.tempusfugit.temporal.Condition;

public class YumRegistryImplTest
    extends AbstractRepositoryTester
{

    private static final String REPO_ID = "rpm-snapshots";

    @Inject
    private YumRegistry yumRegistry;

    @Inject
    private NexusScheduler nexusScheduler;

    @Test
    public void shouldScanRepository()
        throws Exception
    {
        final MavenRepository repository = mock( MavenRepository.class );
        when( repository.getId() ).thenReturn( REPO_ID );
        when( repository.getLocalUrl() ).thenReturn(
            UTIL.resolveFile( "target/test-classes/repo" ).toURI().toString()
        );

        yumRegistry.register( repository );

        waitForAllTasksToBeDone();

        Assert.assertNotNull( yumRegistry.get( REPO_ID ) );
    }

    @Test
    public void shouldUnregisterRepository()
        throws Exception
    {
        MavenRepository repository = createRepository( true );

        yumRegistry.register( repository );
        Assert.assertTrue( yumRegistry.isRegistered( repository.getId() ) );

        yumRegistry.unregister( repository.getId() );
        Assert.assertFalse( yumRegistry.isRegistered( repository.getId() ) );
    }

    private void waitForAllTasksToBeDone()
        throws TimeoutException, InterruptedException
    {
        waitFor( new Condition()
        {
            @Override
            public boolean isSatisfied()
            {
                return nexusScheduler.getActiveTasks().isEmpty();
            }
        } );
    }
}
