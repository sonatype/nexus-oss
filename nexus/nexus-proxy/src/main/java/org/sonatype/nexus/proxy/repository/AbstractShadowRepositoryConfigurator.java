package org.sonatype.nexus.proxy.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.RepositoryStatusConverter;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.plugins.PluginShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public abstract class AbstractShadowRepositoryConfigurator
    implements ShadowRepositoryConfigurator
{
    @Requirement
    private RepositoryStatusConverter repositoryStatusConverter;

    @Requirement( role = PluginShadowRepositoryConfigurator.class )
    private Map<String, PluginShadowRepositoryConfigurator> pluginShadowRepositoryConfigurators;

    public ShadowRepository updateRepositoryFromModel( ShadowRepository old, ApplicationConfiguration configuration,
        CRepositoryShadow repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, Repository masterRepository )
        throws InvalidConfigurationException
    {
        ShadowRepository shadowRepository = old;

        try
        {
            shadowRepository.setMasterRepository( masterRepository );
        }
        catch ( IncompatibleMasterRepositoryException e )
        {
            ValidationMessage message = new ValidationMessage(
                "shadowOf",
                e.getMessage(),
                "The source nexus repository is of an invalid Format." );

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }

        shadowRepository.setId( repo.getId() );
        shadowRepository.setName( repo.getName() );

        shadowRepository.setLocalStatus( repositoryStatusConverter.localStatusFromModel( repo.getLocalStatus() ) );
        shadowRepository.setAllowWrite( false );
        shadowRepository.setBrowseable( true );
        shadowRepository.setProxyMode( null );
        shadowRepository.setIndexable( false );
        shadowRepository.setNotFoundCacheTimeToLive( masterRepository.getNotFoundCacheTimeToLive() );
        shadowRepository.setUserManaged( repo.isUserManaged() );
        shadowRepository.setItemMaxAge( masterRepository.getItemMaxAge() );

        // NX-198: filling up the default variable to store the "default" local URL
        File defaultStorageFile = new File(
            new File( configuration.getWorkingDirectory(), "storage" ),
            shadowRepository.getId() );

        defaultStorageFile.mkdirs();

        try
        {
            repo.defaultLocalStorageUrl = defaultStorageFile.toURL().toString();
        }
        catch ( MalformedURLException e )
        {
            // will not happen, not user settable
            throw new InvalidConfigurationException( "Malformed URL for LocalRepositoryStorage!", e );
        }

        shadowRepository.setLocalUrl( repo.defaultLocalStorageUrl );

        shadowRepository.setLocalStorage( ls );

        for ( PluginShadowRepositoryConfigurator configurator : pluginShadowRepositoryConfigurators.values() )
        {
            if ( configurator.isHandledRepository( shadowRepository ) )
            {
                configurator.configureRepository( shadowRepository );
            }
        }

        return shadowRepository;
    }

}
