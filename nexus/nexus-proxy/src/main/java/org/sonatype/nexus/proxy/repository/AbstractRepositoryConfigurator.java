package org.sonatype.nexus.proxy.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.plugins.PluginRepositoryConfigurator;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public class AbstractRepositoryConfigurator
    implements RepositoryConfigurator
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement( role = PluginRepositoryConfigurator.class )
    private Map<String, PluginRepositoryConfigurator> pluginRepositoryConfigurators;

    public final void applyConfiguration( Repository repository, ApplicationConfiguration configuration,
        CRepository repoConfig )
        throws ConfigurationException
    {
        if ( repoConfig.getExternalConfiguration() == null )
        {
            // just put an elephant in South Africa to find it for sure ;)
            repoConfig.setExternalConfiguration( new Xpp3Dom( "externalConfiguration" ) );
        }

        // in 1st round, i intentionally choosed to make our lives bitter, and handle plexus config manually
        // later we will see about it
        PlexusConfiguration externalConfiguration = new XmlPlexusConfiguration( (Xpp3Dom) repoConfig
            .getExternalConfiguration() );

        preConfigure( repository, configuration, repoConfig, externalConfiguration );

        doConfigure( repository, configuration, repoConfig, externalConfiguration );

        postConfigure( repository, configuration, repoConfig, externalConfiguration );
    }

    protected void preConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        repository.setId( repo.getId() );
        repository.setName( repo.getName() );
        repository.setPathPrefix( repo.getPathPrefix() );
        repository.setLocalStatus( LocalStatus.valueOf( repo.getLocalStatus() ) );
        repository.setAllowWrite( repo.isAllowWrite() );
        repository.setBrowseable( repo.isBrowseable() );
        repository.setIndexable( repo.isIndexable() );
        repository.setNotFoundCacheTimeToLive( repo.getNotFoundCacheTTL() );
        repository.setUserManaged( repo.isUserManaged() );
        repository.setExposed( repo.isExposed() );
        repository.setNotFoundCacheActive( repo.isNotFoundCacheActive() );

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

        LocalRepositoryStorage ls = getLocalRepositoryStorage( repo.getId(), repo.getLocalStorage().getProvider() );

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
    }

    protected void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        // nothing
    }

    protected void postConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        for ( PluginRepositoryConfigurator configurator : pluginRepositoryConfigurators.values() )
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

    public final void prepareForSave( Repository repository, ApplicationConfiguration configuration,
        CRepository repoConfig )
        throws ConfigurationException
    {
        if ( repoConfig.getExternalConfiguration() == null )
        {
            // just put an elephant in South Africa to find it for sure ;)
            repoConfig.setExternalConfiguration( new Xpp3Dom( "externalConfiguration" ) );
        }

        // in 1st round, i intentionally choosed to make our lives bitter, and handle plexus config manually
        // later we will see about it
        PlexusConfiguration externalConfiguration = new XmlPlexusConfiguration( (Xpp3Dom) repoConfig
            .getExternalConfiguration() );

        prePrepare( repository, configuration, repoConfig, externalConfiguration );

        doPrepare( repository, configuration, repoConfig, externalConfiguration );

        postPrepare( repository, configuration, repoConfig, externalConfiguration );
    }

    protected void prePrepare( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        repo.setId( repository.getId() );
        repo.setName( repository.getName() );
        repo.setPathPrefix( repository.getPathPrefix() );
        repo.setLocalStatus( repository.getLocalStatus().toString() );
        repo.setAllowWrite( repository.isAllowWrite() );
        repo.setBrowseable( repository.isBrowseable() );
        repo.setIndexable( repository.isIndexable() );
        repo.setNotFoundCacheTTL( repository.getNotFoundCacheTimeToLive() );
        repo.setUserManaged( repository.isUserManaged() );
        repo.setExposed( repository.isExposed() );
        repo.setNotFoundCacheActive( repository.isNotFoundCacheActive() );

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

        LocalRepositoryStorage ls = getLocalRepositoryStorage( repo.getId(), repo.getLocalStorage().getProvider() );

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
    }

    protected void doPrepare( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        // nothing
    }

    protected void postPrepare( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        // nothing
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

    protected <T extends Repository> T createRepository( Class<T> role, String hint )
        throws InvalidConfigurationException
    {
        try
        {
            return getPlexusContainer().lookup( role, hint );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Could not lookup a new instance of Repository!", e );
        }
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

    protected RemoteRepositoryStorage getRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return getPlexusContainer().lookup( RemoteRepositoryStorage.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have remote storage with unsupported provider: " + provider, e );
        }
    }

}
