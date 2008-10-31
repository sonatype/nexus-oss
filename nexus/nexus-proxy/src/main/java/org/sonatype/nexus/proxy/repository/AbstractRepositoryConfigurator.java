package org.sonatype.nexus.proxy.repository;

import java.io.File;
import java.net.MalformedURLException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.RepositoryStatusConverter;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public abstract class AbstractRepositoryConfigurator
    implements RepositoryConfigurator
{
    @Requirement
    private RepositoryStatusConverter repositoryStatusConverter;

    public Repository updateRepositoryFromModel( Repository old, ApplicationConfiguration configuration,
        CRepository repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, RemoteRepositoryStorage rs )
        throws InvalidConfigurationException
    {
        MavenRepository repository = (MavenRepository) old;

        repository.setId( repo.getId() );
        repository.setName( repo.getName() );
        repository.setLocalStatus( repositoryStatusConverter.localStatusFromModel( repo.getLocalStatus() ) );
        repository.setAllowWrite( repo.isAllowWrite() );
        repository.setBrowseable( repo.isBrowseable() );
        repository.setProxyMode( repositoryStatusConverter.proxyModeFromModel( repo.getProxyMode() ) );
        repository.setIndexable( repo.isIndexable() );
        repository.setNotFoundCacheTimeToLive( repo.getNotFoundCacheTTL() );
        repository.setUserManaged( repo.isUserManaged() );

        repository.setItemMaxAge( repo.getArtifactMaxAge() );
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

        // Setting common things on a repository

        // NX-198: filling up the default variable to store the "default" local URL
        File defaultStorageFile = new File( new File( configuration.getWorkingDirectory(), "storage" ), repository
            .getId() );

        try
        {
            repo.defaultLocalStorageUrl = defaultStorageFile.toURL().toString();
        }
        catch ( MalformedURLException e )
        {
            // will not happen, not user settable
            throw new InvalidConfigurationException( "Malformed URL for LocalRepositoryStorage!", e );
        }

        String localUrl = null;

        if ( repo.getLocalStorage() != null )
        {
            localUrl = repo.getLocalStorage().getUrl();
        }
        else
        {
            localUrl = repo.defaultLocalStorageUrl;

            // Default dir is going to be valid
            defaultStorageFile.mkdirs();
        }

        try
        {
            ls.validateStorageUrl( localUrl );

            repository.setLocalUrl( localUrl );
            repository.setLocalStorage( ls );
        }
        catch ( StorageException e )
        {
            ValidationResponse response = new ApplicationValidationResponse();
            ValidationMessage error = new ValidationMessage(
                "overrideLocalStorageUrl",
                "Repository has an invalid local storage URL '" + localUrl,
                "Invalid file location" );
            response.addValidationError( error );
            throw new InvalidConfigurationException( response );
        }

        if ( repo.getRemoteStorage() != null )
        {
            repository.setRemoteUrl( repo.getRemoteStorage().getUrl() );

            repository.setRemoteStorage( rs );

            if ( repo.getRemoteStorage().getAuthentication() != null
                || repo.getRemoteStorage().getConnectionSettings() != null
                || repo.getRemoteStorage().getHttpProxySettings() != null )
            {
                DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( rsc );

                ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS, repo
                    .getRemoteStorage().getConnectionSettings() );

                ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS, repo
                    .getRemoteStorage().getHttpProxySettings() );

                ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS, repo
                    .getRemoteStorage().getAuthentication() );

                repository.setRemoteStorageContext( ctx );
            }
            else
            {
                repository.setRemoteStorageContext( rsc );
            }
        }

        return repository;
    }

}
