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
package org.sonatype.nexus.plugins.yum.plugin.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.yum.AbstractRepositoryTester;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

import com.google.code.tempusfugit.temporal.Condition;

public class DefaultRepositoryRegistryTest
    extends AbstractRepositoryTester
{
    private static final String REPO_ID = "rpm-snapshots";

    @Inject
    private RepositoryRegistry repositoryRegistry;

    @Inject
    private NexusScheduler nexusScheduler;

    @Test
    public void shouldScanRepository()
        throws Exception
    {
        MavenRepository repository = createMock( MavenRepository.class );
        expect( repository.getId() ).andReturn( REPO_ID ).anyTimes();
        expect( repository.getLocalUrl() ).andReturn( new File( ".", "target/test-classes/repo" ).toURI().toString() ).anyTimes();
        replay( repository );

        repositoryRegistry.registerRepository( repository );
        waitForAllTasksToBeDone();
        Assert.assertNotNull( repositoryRegistry.findRepositoryForId( REPO_ID ) );
    }

    @Test
    public void shouldUnregisterRepository()
        throws Exception
    {
        MavenRepository repository = createRepository( true );
        repositoryRegistry.registerRepository( repository );
        Assert.assertTrue( repositoryRegistry.isRegistered( repository ) );
        repositoryRegistry.unregisterRepository( repository );
        Assert.assertFalse( repositoryRegistry.isRegistered( repository ) );
    }

    @SuppressWarnings( "serial" )
    public static class QueueingEventListener
        extends ArrayList<Event<?>>
        implements EventListener
    {
        @Override
        public void onEvent( Event<?> evt )
        {
            add( evt );
        }
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
