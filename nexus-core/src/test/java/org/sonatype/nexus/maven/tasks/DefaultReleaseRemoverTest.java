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
package org.sonatype.nexus.maven.tasks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.proxy.walker.Walker;

/**
 * @since 2.5
 */
public class DefaultReleaseRemoverTest
{

    public static final String REPO_ID = "foo-proxy";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldFailOnProxyRepositories()
        throws Exception
    {
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final TargetRegistry targetRegistry = mock( TargetRegistry.class );
        final Repository proxyRepository = mock( Repository.class );
        final RepositoryKind proxyRepositoryKind = mock( RepositoryKind.class );

        when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( proxyRepository );
        when( proxyRepository.getRepositoryContentClass() ).thenReturn( maven2ContentClass );
        when( proxyRepository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( proxyRepository.getRepositoryKind() ).thenReturn( proxyRepositoryKind );
        when( proxyRepositoryKind.isFacetAvailable( ProxyRepository.class ) ).thenReturn( true );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request,
                                                                           final ReleaseRemovalResult result,
                                                                           final Target repositoryTarget )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( REPO_ID, 1, null)
        );
    }

    @Test
         public void shouldFailOnNonMaven2Repositories()
    throws Exception
{
    final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
    final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
    final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
    final TargetRegistry targetRegistry = mock( TargetRegistry.class );
    final Repository repository = mock( Repository.class );

    when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( repository );
    when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );

    thrown.expect( IllegalArgumentException.class );
    new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
    {
        @Override
        public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                       final ReleaseRemovalRequest request,
                                                                       final ReleaseRemovalResult result,
                                                                       final Target repositoryTarget )
        {
            return new ReleaseRemovalResult( repository.getId() );
        }
    }.removeReleases(
        new ReleaseRemovalRequest( REPO_ID, 1, null)
    );
}

    @Test
    public void shouldFailOnOutOfServiceRepositories()
        throws Exception
    {
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final TargetRegistry targetRegistry = mock( TargetRegistry.class );

        final Repository repository = mock( Repository.class );

        when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven2ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request,
                                                                           final ReleaseRemovalResult result,
                                                                           final Target repositoryTarget )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( REPO_ID, 1, null)
        );
    }

    @Test
    public void shouldFailOnGroupRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final TargetRegistry targetRegistry = mock( TargetRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );


        when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( true );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request,
                                                                           final ReleaseRemovalResult result,
                                                                           final Target repositoryTarget )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( REPO_ID, 1, null)
        );
    }

    @Test
    public void shouldFailOnSnapshotRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final TargetRegistry targetRegistry = mock( TargetRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );
        final MavenRepository mavenRepository = mock( MavenRepository.class);

        when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( false );
        when( repository.adaptToFacet( MavenRepository.class )).thenReturn( mavenRepository );
        when( mavenRepository.getRepositoryPolicy()).thenReturn( RepositoryPolicy.SNAPSHOT );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request,
                                                                           final ReleaseRemovalResult result,
                                                                           final Target repositoryTarget )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( REPO_ID, 1, null)
        );
    }

    @Test
    public void shouldFailOnMixedRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final TargetRegistry targetRegistry = mock( TargetRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );
        final MavenRepository mavenRepository = mock( MavenRepository.class);

        when( repositoryRegistry.getRepository( REPO_ID ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( false );
        when( repository.adaptToFacet( MavenRepository.class )).thenReturn( mavenRepository );
        when( mavenRepository.getRepositoryPolicy()).thenReturn( RepositoryPolicy.MIXED );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, targetRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request,
                                                                           final ReleaseRemovalResult result,
                                                                           final Target repositoryTarget )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( REPO_ID, 1, null)
        );
    }
}
