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
import org.sonatype.nexus.proxy.walker.Walker;

/**
 * @since 2.5
 */
public class DefaultReleaseRemoverTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldFailOnProxyRepositories()
        throws Exception
    {
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final Repository proxyRepository = mock( Repository.class );
        final RepositoryKind proxyRepositoryKind = mock( RepositoryKind.class );

        when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( proxyRepository );
        when( proxyRepository.getRepositoryContentClass() ).thenReturn( maven2ContentClass );
        when( proxyRepository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( proxyRepository.getRepositoryKind() ).thenReturn( proxyRepositoryKind );
        when( proxyRepositoryKind.isFacetAvailable( ProxyRepository.class ) ).thenReturn( true );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( "foo-proxy", 1)
        );
    }

    @Test
         public void shouldFailOnNonMaven2Repositories()
    throws Exception
{
    final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
    final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
    final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
    final Repository repository = mock( Repository.class );

    when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( repository );
    when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );

    thrown.expect( IllegalArgumentException.class );
    new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
    {
        @Override
        public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                       final ReleaseRemovalRequest request )
        {
            return new ReleaseRemovalResult( repository.getId() );
        }
    }.removeReleases(
        new ReleaseRemovalRequest( "foo-proxy", 1)
    );
}

    @Test
    public void shouldFailOnOutOfServiceRepositories()
        throws Exception
    {
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final Repository repository = mock( Repository.class );

        when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven2ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.OUT_OF_SERVICE );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( "foo-proxy", 1)
        );
    }

    @Test
    public void shouldFailOnGroupRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );


        when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( true );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( "foo-proxy", 1)
        );
    }

    @Test
    public void shouldFailOnSnapshotRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );
        final MavenRepository mavenRepository = mock( MavenRepository.class);

        when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( false );
        when( repository.adaptToFacet( MavenRepository.class )).thenReturn( mavenRepository );
        when( mavenRepository.getRepositoryPolicy()).thenReturn( RepositoryPolicy.SNAPSHOT );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( "foo-proxy", 1)
        );
    }

    @Test
    public void shouldFailOnMixedRepositories()
        throws Exception
    {
        final Maven1ContentClass maven1ContentClass = new Maven1ContentClass();
        final Maven2ContentClass maven2ContentClass = new Maven2ContentClass();
        final RepositoryRegistry repositoryRegistry = mock( RepositoryRegistry.class );
        final Repository repository = mock( Repository.class );
        final RepositoryKind repositoryKind = mock( RepositoryKind.class );
        final MavenRepository mavenRepository = mock( MavenRepository.class);

        when( repositoryRegistry.getRepository( "foo-proxy" ) ).thenReturn( repository );
        when( repository.getRepositoryContentClass() ).thenReturn( maven1ContentClass );
        when( repository.getLocalStatus() ).thenReturn( LocalStatus.IN_SERVICE );
        when( repository.getRepositoryKind() ).thenReturn( repositoryKind );
        when( repositoryKind.isFacetAvailable( GroupRepository.class ) ).thenReturn( false );
        when( repository.adaptToFacet( MavenRepository.class )).thenReturn( mavenRepository );
        when( mavenRepository.getRepositoryPolicy()).thenReturn( RepositoryPolicy.MIXED );

        thrown.expect( IllegalArgumentException.class );
        new DefaultReleaseRemover( repositoryRegistry, mock( Walker.class ), maven2ContentClass )
        {
            @Override
            public ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                           final ReleaseRemovalRequest request )
            {
                return new ReleaseRemovalResult( repository.getId() );
            }
        }.removeReleases(
            new ReleaseRemovalRequest( "foo-proxy", 1)
        );
    }
}
