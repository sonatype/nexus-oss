package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class AbstractMavenRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    @Override
    public Repository updateRepositoryFromModel( Repository old, ApplicationConfiguration configuration,
        CRepository repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, RemoteRepositoryStorage rs )
        throws InvalidConfigurationException
    {
        MavenRepository repository =  (MavenRepository) super.updateRepositoryFromModel( old, configuration, repo, rsc, ls, rs );

        repository.setReleaseMaxAge( repo.getArtifactMaxAge() );
        repository.setSnapshotMaxAge( repo.getArtifactMaxAge() );
        repository.setMetadataMaxAge( repo.getMetadataMaxAge() );
        repository.setCleanseRepositoryMetadata( repo.isMaintainProxiedRepositoryMetadata() );
        repository.setChecksumPolicy( ChecksumPolicy.fromModel( repo.getChecksumPolicy() ) );

        if ( CRepository.REPOSITORY_POLICY_RELEASE.equals( repo.getRepositoryPolicy() ) )
        {
            repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        }
        else
        {
            repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        }
        
        return repository;
    }
}
