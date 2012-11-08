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

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractRepositoryTester;
import org.sonatype.nexus.repository.yum.internal.RpmRepositoryEventsHandler;
import org.sonatype.nexus.repository.yum.internal.config.YumPluginConfiguration;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.YumRegistry;

public class RpmRepositoryEventsHandlerTest
    extends AbstractRepositoryTester
{

    @Inject
    private RpmRepositoryEventsHandler handler;

    @Inject
    private YumRegistry repositoryRegistry;

    @Inject
    private YumPluginConfiguration yumConfig;

    @Before
    public void activateRepo()
    {
        yumConfig.setActive( true );
    }

    @After
    public void reactivateRepo()
    {
        yumConfig.setActive( true );
    }

    @Test
    public void shouldRegisterRepository()
        throws Exception
    {
        Repository repo = createRepository( true );
        handler.on( new RepositoryRegistryEventAdd( null, repo ) );
        Assert.assertTrue( repositoryRegistry.isRegistered( repo.getId() ) );
    }

    @Test
    public void shouldNotRegisterRepository()
        throws Exception
    {
        Repository repo = createRepository( false );
        repositoryRegistry.unregister( repo.getId() );
        handler.on( new RepositoryRegistryEventAdd( null, repo ) );
        Assert.assertFalse( repositoryRegistry.isRegistered( repo.getId() ) );
    }

    @Test
    public void shouldNotCreateRepo()
    {
        Repository repo = createRepository( true );
        repositoryRegistry.unregister( repo.getId() );
        handler.on( new RepositoryItemEventStoreCreate( repo, createItem( "VERSION", "test-source.jar" ) ) );
    }

    @Test
    public void shouldNotCreateRepoForPom()
    {
        yumConfig.setActive( false );

        MavenRepository repo = createRepository( true );
        repositoryRegistry.register( repo );
        handler.on( new RepositoryItemEventStoreCreate( repo, createItem( "VERSION", "test.pom" ) ) );
    }

    @Test
    public void shouldCreateRepoForPom()
    {
        yumConfig.setActive( false );

        MavenRepository repo = createRepository( true );
        repositoryRegistry.register( repo );
        handler.on( new RepositoryItemEventStoreCreate( repo, createItem( "VERSION", "test.rpm" ) ) );
    }

}
