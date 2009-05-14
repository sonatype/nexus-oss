package org.sonatype.nexus.proxy.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public abstract class AbstractRepositoryConfigurator
    implements Configurator
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement( role = RepositoryCustomizer.class )
    private Map<String, RepositoryCustomizer> pluginRepositoryConfigurators;

    public final void validate( ApplicationConfiguration configuration, Object repoConfig )
        throws ConfigurationException
    {
        if ( repoConfig instanceof CRepository )
        {
            prepareExternalConfiguration( (CRepository) repoConfig );

            doValidate( configuration, (CRepository) repoConfig,
                        ( (CRepository) repoConfig ).externalConfigurationImple );
        }
    }

    public final void applyConfiguration( Object target, ApplicationConfiguration configuration,
                                          CoreConfiguration config )
        throws ConfigurationException
    {
        prepareExternalConfiguration( (CRepository) config.getConfiguration( false ) );

        doApplyConfiguration( (Repository) target, configuration, (CRepository) config.getConfiguration( false ),
                              config.getExternalConfiguration() );
    }

    public final void prepareForSave( Object target, ApplicationConfiguration configuration, CoreConfiguration config )
    {
        prepareExternalConfiguration( (CRepository) config.getConfiguration( true ) );

        // in 1st round, i intentionally choosed to make our lives bitter, and handle plexus config manually
        // later we will see about it
        doPrepareForSave( (Repository) target, configuration, (CRepository) config.getConfiguration( true ),
                          config.getExternalConfiguration() );

        // commit, since doPrepareForSave() potentially modifies coreConfig or externalConfig
        config.applyChanges();
    }

    protected void prepareExternalConfiguration( CRepository repoConfig )
    {
        if ( repoConfig.getExternalConfiguration() == null )
        {
            // just put an elephant in South Africa to find it for sure ;)
            repoConfig.setExternalConfiguration( new Xpp3Dom( "externalConfiguration" ) );
        }

        if ( repoConfig.externalConfigurationImple == null )
        {
            // in 1st round, i intentionally choosed to make our lives bitter, and handle config manually
            // later we will see about it
            repoConfig.externalConfigurationImple =
                createExternalConfiguration( (Xpp3Dom) repoConfig.getExternalConfiguration() );
        }
    }

    protected abstract ExternalConfiguration createExternalConfiguration( Xpp3Dom dom );

    public ExternalConfiguration getExternalConfiguration( Repository repository )
    {
        return repository.getCurrentCoreConfiguration().getExternalConfiguration();
    }

    protected void doValidate( ApplicationConfiguration configuration, CRepository repo,
                               ExternalConfiguration externalConfiguration )
        throws ConfigurationException
    {
        // TODO:

    }

    @SuppressWarnings( "unchecked" )
    protected void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
                                         CRepository repo, ExternalConfiguration externalConfiguration )
        throws ConfigurationException
    {
        List<CMirror> mirrors = (List<CMirror>) repo.getMirrors();

        if ( mirrors != null && mirrors.size() > 0 )
        {
            List<Mirror> runtimeMirrors = new ArrayList<Mirror>();

            for ( CMirror mirror : mirrors )
            {
                runtimeMirrors.add( new Mirror( mirror.getId(), mirror.getUrl() ) );
            }

            repository.getPublishedMirrors().setMirrors( runtimeMirrors );
        }
        else
        {
            repository.getPublishedMirrors().setMirrors( null );
        }

        // Setting common things on a repository

        // NX-198: filling up the default variable to store the "default" local URL
        File defaultStorageFile = new File( new File( configuration.getWorkingDirectory(), "storage" ), repo.getId() );

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

        if ( repo.getLocalStorage() != null && !StringUtils.isEmpty( repo.getLocalStorage().getUrl() ) )
        {
            localUrl = repo.getLocalStorage().getUrl();
        }
        else
        {
            localUrl = repo.defaultLocalStorageUrl;

            // Default dir is going to be valid
            defaultStorageFile.mkdirs();
        }

        if ( repo.getLocalStorage() == null )
        {
            repo.setLocalStorage( new CLocalStorage() );

            repo.getLocalStorage().setProvider( "file" );
        }

        LocalRepositoryStorage ls = getLocalRepositoryStorage( repo.getId(), repo.getLocalStorage().getProvider() );

        try
        {
            ls.validateStorageUrl( localUrl );

            repo.getLocalStorage().setUrl( localUrl );

            repository.setLocalStorage( ls );
        }
        catch ( StorageException e )
        {
            ValidationResponse response = new ApplicationValidationResponse();

            ValidationMessage error =
                new ValidationMessage( "overrideLocalStorageUrl", "Repository has an invalid local storage URL '"
                    + localUrl, "Invalid file location" );

            response.addValidationError( error );

            throw new InvalidConfigurationException( response );
        }

        for ( RepositoryCustomizer configurator : pluginRepositoryConfigurators.values() )
        {
            if ( configurator.isHandledRepository( repository ) )
            {
                configurator.configureRepository( repository );
            }
        }

        // clear the NotFoundCache
        if ( repository.getNotFoundCache() != null )
        {
            repository.getNotFoundCache().purge();
        }
    }

    protected void doPrepareForSave( Repository repository, ApplicationConfiguration configuration,
                                     CRepository repoConfig, ExternalConfiguration externalConfiguration )
    {
        List<Mirror> mirrors = (List<Mirror>) repository.getPublishedMirrors().getMirrors();

        if ( mirrors != null && mirrors.size() > 0 )
        {
            List<CMirror> runtimeMirrors = new ArrayList<CMirror>();

            for ( Mirror mirror : mirrors )
            {
                CMirror cmirror = new CMirror();

                cmirror.setId( mirror.getId() );
                cmirror.setUrl( mirror.getUrl() );
                runtimeMirrors.add( cmirror );
            }

            repoConfig.setMirrors( runtimeMirrors );
        }
        else
        {
            repoConfig.getMirrors().clear();
        }

        // Setting common things on a repository

        repoConfig.getLocalStorage().setUrl( repository.getLocalUrl() );
    }

    // ==

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    protected RepositoryTypeRegistry getRepositoryTypeRegistry()
    {
        return repositoryTypeRegistry;
    }

    protected boolean existsRepositoryType( Class<?> role, String hint )
        throws InvalidConfigurationException
    {
        return componentExists( role, hint );
    }

    protected boolean existsLocalRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        return componentExists( LocalRepositoryStorage.class, provider );
    }

    protected boolean existsRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        return componentExists( RemoteRepositoryStorage.class, provider );
    }

    protected boolean componentExists( Class<?> role, String hint )
    {
        return getPlexusContainer().hasComponent( role, hint );
    }

    protected LocalRepositoryStorage getLocalRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return getPlexusContainer().lookup( LocalRepositoryStorage.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have local storage with unsupported provider: " + provider, e );
        }
    }

}
