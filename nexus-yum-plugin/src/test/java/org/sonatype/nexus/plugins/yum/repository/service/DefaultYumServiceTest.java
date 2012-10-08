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
package org.sonatype.nexus.plugins.yum.repository.service;

import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.yum.AbstractYumNexusTestCase;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultYumServiceTest
    extends AbstractYumNexusTestCase
{
    private static final String REPO_BASE_URL = "http://localhost:8081/nexus/service/local/snapshots/1.0";

    private static final String VERSION_1_0 = "1.0";

    private static final String SNAPSHOTS = "snapshots";

    // private static final String FILE_PATH1 = "path1.rpm";
    // private static final String FILE_PATH2 = "path2.rpm";

    @Inject
    private YumService yumService;

    @Inject
    private RepositoryRegistry repositoryRegistry;

    @Inject
    private YumConfiguration yumConfig;

    @Before
    public void activateService()
    {
        yumConfig.setActive( true );
    }

    @Test
    public void shouldCacheRepository()
        throws Exception
    {
        YumRepository repo1 =
            yumService.getRepository( createRepository( SNAPSHOTS ), VERSION_1_0, new URL( REPO_BASE_URL ) );
        YumRepository repo2 =
            yumService.getRepository( createRepository( SNAPSHOTS ), VERSION_1_0, new URL( REPO_BASE_URL ) );
        Assert.assertEquals( repo1, repo2 );
    }

    @Test
    public void shouldRecreateRepository()
        throws Exception
    {
        YumRepository repo1 =
            yumService.getRepository( createRepository( SNAPSHOTS ), VERSION_1_0, new URL( REPO_BASE_URL ) );

        yumService.markDirty( createRepository( SNAPSHOTS ), VERSION_1_0 );

        YumRepository repo2 =
            yumService.getRepository( createRepository( SNAPSHOTS ), VERSION_1_0, new URL( REPO_BASE_URL ) );

        assertNotSame( repo1, repo2 );
    }

    @Test
    public void shouldNotCreateYumRepo()
        throws Exception
    {
        yumConfig.setActive( false );
        Assert.assertNull( yumService.createYumRepository( createRepository( SNAPSHOTS ) ) );
    }

    @Test
    public void shouldNotFindRepository()
        throws Exception
    {
        Assert.assertNull( repositoryRegistry.findRepositoryForId( "blablup" ) );
    }

    @Test
    public void shouldFindRepository()
        throws Exception
    {
        repositoryRegistry.registerRepository( createRepository( SNAPSHOTS ) );
        Assert.assertNotNull( repositoryRegistry.findRepositoryForId( SNAPSHOTS ) );
    }

    public static MavenRepository createRepository( String id )
    {
        final MavenRepository repo = mock( MavenRepository.class );
        when( repo.getId() ).thenReturn( id );
        when( repo.getLocalUrl() ).thenReturn( getTempUrl() );
        when( repo.getProviderRole() ).thenReturn( Repository.class.getName() );
        when( repo.getProviderHint() ).thenReturn( "maven2" );
        return repo;
    }

    private static String getTempUrl()
    {
        return new File( System.getProperty( "java.io.tmpdir" ) ).toURI().toString();
    }
}
