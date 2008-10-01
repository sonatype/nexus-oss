/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.runtime.ApplicationRuntimeConfigurationBuilder;
import org.sonatype.nexus.configuration.application.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.application.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationContext;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.EventMulticasterComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;

/**
 * The class DefaultNexusConfiguration is responsible for config management. It actually keeps in sync Nexus internal
 * state with p ersisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus current
 * state and Nexus user config.
 * 
 * @author cstamas
 */
@Component( role = NexusConfiguration.class )
public class DefaultNexusConfiguration
    extends EventMulticasterComponent
    implements NexusConfiguration
{

    /**
     * The configuration source.
     */
    @Requirement( hint = "file" )
    private ApplicationConfigurationSource configurationSource;

    /**
     * The config validator.
     */
    @Requirement
    private ApplicationConfigurationValidator configurationValidator;

    /**
     * The runtime configuration builder.
     */
    @Requirement
    private ApplicationRuntimeConfigurationBuilder runtimeConfigurationBuilder;

    /**
     * The repository registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    /**
     * The target registry.
     */
    @Requirement
    private TargetRegistry targetRegistry;

    /**
     * The available content classes.
     */
    @Requirement( role = ContentClass.class )
    private List<ContentClass> contentClasses;

    /** The global remote storage context. */
    private RemoteStorageContext remoteStorageContext;

    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}" )
    private File workingDirectory;

    /** The config dir */
    private File configurationDirectory;

    /** The temp dir */
    private File temporaryDirectory;

    /** The trash */
    private File wastebasketDirectory;

    public RemoteStorageContext getRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public void setRemoteStorageContext( RemoteStorageContext remoteStorageContext )
    {
        this.remoteStorageContext = remoteStorageContext;
    }

    public void loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        loadConfiguration( false );
    }

    public void loadConfiguration( boolean force )
        throws ConfigurationException,
            IOException
    {
        if ( force || configurationSource.getConfiguration() == null )
        {
            getLogger().info( "Loading Nexus Configuration..." );

            configurationSource.loadConfiguration();

            configurationDirectory = null;

            temporaryDirectory = null;

            wastebasketDirectory = null;

            // create shared remote ctx
            // this one has no parent
            remoteStorageContext = new DefaultRemoteStorageContext( null );

            if ( getConfiguration().getGlobalConnectionSettings() != null )
            {
                remoteStorageContext.putRemoteConnectionContextObject(
                    RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS,
                    getConfiguration().getGlobalConnectionSettings() );
            }

            if ( getConfiguration().getGlobalHttpProxySettings() != null )
            {
                remoteStorageContext.putRemoteConnectionContextObject(
                    RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS,
                    getConfiguration().getGlobalHttpProxySettings() );
            }

            // and register things
            runtimeConfigurationBuilder.initialize( this );

            notifyProximityEventListeners( new ConfigurationChangeEvent( this ) );
        }
    }

    public void applyConfiguration()
        throws IOException
    {
        getLogger().info( "Applying Nexus Configuration..." );

        configurationDirectory = null;

        temporaryDirectory = null;

        wastebasketDirectory = null;

        notifyProximityEventListeners( new ConfigurationChangeEvent( this ) );
    }

    public void saveConfiguration()
        throws IOException
    {
        configurationSource.storeConfiguration();
    }

    protected void applyAndSaveConfiguration()
        throws IOException
    {
        applyConfiguration();

        saveConfiguration();
    }

    public Configuration getConfiguration()
    {
        return configurationSource.getConfiguration();
    }

    public ApplicationConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return configurationSource.getConfigurationAsStream();
    }

    public boolean isInstanceUpgraded()
    {
        // TODO: this is not quite true: we might keep model ver but upgrade JARs of Nexus only in a release
        // we should store the nexus version somewhere in working storage and trigger some household stuff
        // if version changes.
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationUpgraded()
    {
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationDefaulted()
    {
        return configurationSource.isConfigurationDefaulted();
    }

    public File getWorkingDirectory()
    {
        // Create the dir if doesn't exist, throw runtime exception on failure
        // bad bad bad
        if ( !workingDirectory.exists() && !workingDirectory.mkdirs() )
        {
            String message = "\r\n******************************************************************************\r\n"
                + "* Could not create work directory [ " + workingDirectory.toString() + "]!!!! *\r\n"
                + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
                + "******************************************************************************";

            getLogger().fatalError( message );
        }

        return workingDirectory;
    }

    public File getWorkingDirectory( String key )
    {
        return new File( getWorkingDirectory(), key );
    }

    public File getTemporaryDirectory()
    {
        if ( temporaryDirectory == null )
        {
            temporaryDirectory = new File( System.getProperty( "java.io.tmpdir" ) );

            if ( !temporaryDirectory.exists() )
            {
                temporaryDirectory.mkdirs();
            }
        }
        return temporaryDirectory;
    }

    public File getConfigurationDirectory()
    {
        if ( configurationDirectory == null )
        {
            configurationDirectory = new File( getWorkingDirectory(), "conf" );

            if ( !configurationDirectory.exists() )
            {
                configurationDirectory.mkdirs();
            }

        }
        return configurationDirectory;
    }

    public File getWastebasketDirectory()
    {
        if ( wastebasketDirectory == null )
        {
            wastebasketDirectory = getWorkingDirectory( "trash" );
        }
        return wastebasketDirectory;
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepository repository )
        throws InvalidConfigurationException
    {
        return runtimeConfigurationBuilder.createRepositoryFromModel( configuration, repository );
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepositoryShadow repositoryShadow )
        throws InvalidConfigurationException
    {
        return runtimeConfigurationBuilder.createRepositoryFromModel( configuration, repositoryShadow );
    }

    public Collection<ContentClass> listRepositoryContentClasses()
    {
        return Collections.unmodifiableList( contentClasses );
    }

    // ------------------------------------------------------------------
    // Security

    public boolean isSecurityEnabled()
    {
        return getConfiguration().getSecurity() != null && getConfiguration().getSecurity().isEnabled();
    }

    public void setSecurityEnabled( boolean enabled )
        throws IOException
    {
        getConfiguration().getSecurity().setEnabled( enabled );

        applyAndSaveConfiguration();
    }

    public boolean isAnonymousAccessEnabled()
    {
        return getConfiguration().getSecurity() != null && getConfiguration().getSecurity().isAnonymousAccessEnabled();
    }

    public void setAnonymousAccessEnabled( boolean enabled )
        throws IOException
    {
        getConfiguration().getSecurity().setAnonymousAccessEnabled( enabled );

        applyAndSaveConfiguration();
    }

    public String getAnonymousUsername()
    {
        return getConfiguration().getSecurity().getAnonymousUsername();
    }

    public void setAnonymousUsername( String val )
        throws IOException
    {
        getConfiguration().getSecurity().setAnonymousUsername( val );

        applyAndSaveConfiguration();
    }

    public String getAnonymousPassword()
    {
        return getConfiguration().getSecurity().getAnonymousPassword();
    }

    public void setAnonymousPassword( String val )
        throws IOException
    {
        getConfiguration().getSecurity().setAnonymousPassword( val );

        applyAndSaveConfiguration();
    }

    public List<String> getRealms()
    {
        return getConfiguration().getSecurity().getRealms();
    }

    // ------------------------------------------------------------------
    // Booting

    public void createInternals()
        throws ConfigurationException
    {
        createRepositories();

        createRepositoryTargets();
    }

    public void dropInternals()
    {
        dropRepositories();

        dropRepositoryTargets();
    }

    @SuppressWarnings( "unchecked" )
    protected void createRepositories()
        throws ConfigurationException
    {
        List<CRepository> reposes = getConfiguration().getRepositories();

        for ( CRepository repo : reposes )
        {
            Repository repository = createRepositoryFromModel( getConfiguration(), repo );

            repositoryRegistry.addRepository( repository );
        }

        if ( getConfiguration().getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> shadows = getConfiguration().getRepositoryShadows();
            for ( CRepositoryShadow shadow : shadows )
            {
                Repository repository = createRepositoryFromModel( getConfiguration(), shadow );

                // shadows has no index
                repositoryRegistry.addRepository( repository );
            }
        }

        if ( getConfiguration().getRepositoryGrouping() != null
            && getConfiguration().getRepositoryGrouping().getRepositoryGroups() != null )
        {
            List<CRepositoryGroup> groups = getConfiguration().getRepositoryGrouping().getRepositoryGroups();

            for ( CRepositoryGroup group : groups )
            {
                if ( group.getName() == null )
                {
                    group.setName( group.getGroupId() );
                }
                try
                {
                    repositoryRegistry.addRepositoryGroup( group.getGroupId(), group.getRepositories() );
                }
                catch ( NoSuchRepositoryException e )
                {
                    throw new ConfigurationException( "Cannot register repository groups!", e );
                }
                catch ( InvalidGroupingException e )
                {
                    throw new ConfigurationException( "Configuration contains invalid grouping!", e );
                }
            }
        }
    }

    protected void dropRepositories()
    {
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            try
            {
                repositoryRegistry.removeRepositorySilently( repository.getId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                // will not happen
            }

            // unregister it as config listener if needed
            removeProximityEventListener( repository );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void createRepositoryTargets()
        throws ConfigurationException
    {
        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        if ( targets == null )
        {
            return;
        }

        for ( CRepositoryTarget settings : targets )
        {
            ContentClass contentClass = null;

            for ( ContentClass cl : contentClasses )
            {
                if ( settings.getContentClass().equals( cl.getId() ) )
                {
                    contentClass = cl;

                    break;
                }
            }

            if ( contentClass == null )
            {
                throw new ConfigurationException( "Could not find ContentClass with ID='" + settings.getContentClass()
                    + "'" );
            }

            targetRegistry.addRepositoryTarget( new Target(
                settings.getId(),
                settings.getName(),
                contentClass,
                settings.getPatterns() ) );
        }
    }

    protected void dropRepositoryTargets()
    {
        // nothing?
    }

    // ------------------------------------------------------------------
    // REST API

    public String getBaseUrl()
    {
        if ( getConfiguration().getRestApi() != null )
        {
            return getConfiguration().getRestApi().getBaseUrl();
        }
        else
        {
            return null;
        }
    }

    public void setBaseUrl( String baseUrl )
        throws IOException
    {
        getConfiguration().getRestApi().setBaseUrl( baseUrl );

        applyAndSaveConfiguration();
    }

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections
    // Globals are mandatory: RU

    // CRemoteConnectionSettings are mandatory: RU

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        return getConfiguration().getGlobalConnectionSettings();
    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException
    {
        remoteStorageContext.putRemoteConnectionContextObject(
            RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS,
            settings );

        getConfiguration().setGlobalConnectionSettings( settings );

        applyAndSaveConfiguration();
    }

    // CRemoteHttpProxySettings are optional: CRUD

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        remoteStorageContext.putRemoteConnectionContextObject(
            RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS,
            settings );

        getConfiguration().setGlobalHttpProxySettings( settings );

        applyAndSaveConfiguration();
    }

    public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
    {
        return getConfiguration().getGlobalHttpProxySettings();
    }

    public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        createGlobalRemoteHttpProxySettings( settings );
    }

    public void deleteGlobalRemoteHttpProxySettings()
        throws IOException
    {
        remoteStorageContext.removeRemoteConnectionContextObject( RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS );

        getConfiguration().setGlobalHttpProxySettings( null );

        applyAndSaveConfiguration();
    }

    // CRouting are mandatory: RU

    public CRouting readRouting()
    {
        return getConfiguration().getRouting();
    }

    public void updateRouting( CRouting settings )
        throws ConfigurationException,
            IOException
    {
        getConfiguration().setRouting( settings );

        applyAndSaveConfiguration();
    }

    // CRepository and CreposioryShadow helper

    protected ApplicationValidationContext getRepositoryValidationContext()
    {
        ApplicationValidationContext result = new ApplicationValidationContext();

        result.addExistingRepositoryIds();

        List<CRepository> repositories = getConfiguration().getRepositories();

        if ( repositories != null )
        {
            for ( CRepository repo : repositories )
            {
                result.getExistingRepositoryIds().add( repo.getId() );
            }
        }

        result.addExistingRepositoryShadowIds();

        List<CRepositoryShadow> repositoryShadows = getConfiguration().getRepositoryShadows();

        if ( repositoryShadows != null )
        {
            for ( CRepositoryShadow repo : repositoryShadows )
            {
                result.getExistingRepositoryShadowIds().add( repo.getId() );
            }
        }

        return result;
    }

    // CRepository: CRUD

    protected void validateRepository( CRepository settings, boolean create )
        throws ConfigurationException
    {
        ApplicationValidationContext ctx = getRepositoryValidationContext();

        if ( !create && !StringUtils.isEmpty( settings.getId() ) )
        {
            // remove "itself" from the list to avoid hitting "duplicate repo" problem
            ctx.getExistingRepositoryIds().remove( settings.getId() );
        }

        ValidationResponse vr = configurationValidator.validateRepository( ctx, settings );

        if ( !vr.isValid() )
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public Collection<CRepository> listRepositories()
    {
        return new ArrayList<CRepository>( getConfiguration().getRepositories() );
    }

    public void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException
    {
        validateRepository( settings, true );

        Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel( getConfiguration(), settings );

        repositoryRegistry.addRepository( repository );

        getConfiguration().getRepositories().add( settings );

        applyAndSaveConfiguration();
    }

    public CRepository readRepository( String id )
        throws NoSuchRepositoryException
    {
        repositoryRegistry.getRepository( id );

        List<CRepository> shadows = getConfiguration().getRepositories();

        for ( Iterator<CRepository> i = shadows.iterator(); i.hasNext(); )
        {
            CRepository repo = i.next();

            if ( repo.getId().equals( id ) )
            {
                return repo;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    public void updateRepository( CRepository settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        validateRepository( settings, false );

        Repository repository = repositoryRegistry.getRepository( settings.getId() );

        if ( !RepositoryType.SHADOW.equals( repository.getRepositoryType() ) )
        {
            Repository newRepository = runtimeConfigurationBuilder.updateRepositoryFromModel(
                repository,
                getConfiguration(),
                settings );

            // replace it with new one
            repositoryRegistry.updateRepository( newRepository );

            List<CRepository> reposes = getConfiguration().getRepositories();

            for ( int i = 0; i < reposes.size(); i++ )
            {
                CRepository repo = reposes.get( i );

                if ( repo.getId().equals( settings.getId() ) )
                {
                    reposes.remove( i );

                    reposes.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }
        }
        else
        {
            throw new ConfigurationException( "Repository with ID=" + settings.getId()
                + " is not a hosted/proxy repository!" );
        }
    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException,
            IOException,
            ConfigurationException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        if ( ShadowRepository.class.isAssignableFrom( repository.getClass() ) )
        {
            // this is shadow
            throw new NoSuchRepositoryException( id );
        }

        repository.setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        // remove dependants too

        // =======
        // shadows
        // (fail if any repo references the currently processing one)
        List<CRepositoryShadow> shadows = getConfiguration().getRepositoryShadows();

        for ( Iterator<CRepositoryShadow> i = shadows.iterator(); i.hasNext(); )
        {
            CRepositoryShadow shadow = i.next();

            if ( repository.getId().equals( shadow.getShadowOf() ) )
            {
                throw new ConfigurationException( "The repository with ID " + id
                    + " is not deletable, it has dependant repositories!" );
            }
        }

        // ======
        // groups
        // (correction in config only, registry DOES handle it)

        // do we have groups at all?
        if ( getConfiguration().getRepositoryGrouping() != null
            && getConfiguration().getRepositoryGrouping().getRepositoryGroups() != null )
        {
            // all existing groups
            List<CRepositoryGroup> groups = getConfiguration().getRepositoryGrouping().getRepositoryGroups();

            // the groups that references the repository
            List<String> repoGroups = repositoryRegistry.getGroupsOfRepository( id );

            for ( Iterator<CRepositoryGroup> i = groups.iterator(); i.hasNext(); )
            {
                CRepositoryGroup group = i.next();

                if ( repoGroups.contains( group.getGroupId() ) )
                {
                    group.getRepositories().remove( id );
                }
            }
        }

        // ===========
        // pahMappings
        // (correction, since registry is completely unaware of this component)

        List<CGroupsSettingPathMappingItem> pathMappings = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CGroupsSettingPathMappingItem> i = pathMappings.iterator(); i.hasNext(); )
        {
            CGroupsSettingPathMappingItem item = i.next();

            item.removeRepository( id );
        }

        // ===========
        // and finally
        // this cleans it properly from the registry (from reposes and repo groups)
        repositoryRegistry.removeRepository( id );

        List<CRepository> reposes = getConfiguration().getRepositories();

        for ( Iterator<CRepository> i = reposes.iterator(); i.hasNext(); )
        {
            CRepository repo = i.next();

            if ( repo.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    // CRepositoryShadow: CRUD

    protected void validateRepositoryShadow( CRepositoryShadow settings, boolean create )
        throws ConfigurationException
    {
        ApplicationValidationContext ctx = getRepositoryValidationContext();

        if ( !create && !StringUtils.isEmpty( settings.getId() ) )
        {
            // remove "itself" from the list to avoid hitting "duplicate repo" problem
            ctx.getExistingRepositoryShadowIds().remove( settings.getId() );
        }

        ValidationResponse vr = configurationValidator.validateRepository( ctx, settings );

        if ( !vr.isValid() )
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public Collection<CRepositoryShadow> listRepositoryShadows()
    {
        return new ArrayList<CRepositoryShadow>( getConfiguration().getRepositoryShadows() );
    }

    public void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException
    {
        validateRepositoryShadow( settings, true );

        Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel( getConfiguration(), settings );

        repositoryRegistry.addRepository( repository );

        getConfiguration().getRepositoryShadows().add( settings );

        applyAndSaveConfiguration();
    }

    public CRepositoryShadow readRepositoryShadow( String id )
        throws NoSuchRepositoryException
    {
        repositoryRegistry.getRepository( id );

        List<CRepositoryShadow> shadows = getConfiguration().getRepositoryShadows();

        for ( Iterator<CRepositoryShadow> i = shadows.iterator(); i.hasNext(); )
        {
            CRepositoryShadow shadow = i.next();
            if ( shadow.getId().equals( id ) )
            {
                return shadow;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    public void updateRepositoryShadow( CRepositoryShadow settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        validateRepositoryShadow( settings, false );

        Repository repository = repositoryRegistry.getRepository( settings.getId() );

        if ( ShadowRepository.class.isAssignableFrom( repository.getClass() ) )
        {
            Repository newRepository = runtimeConfigurationBuilder.updateRepositoryFromModel(
                (ShadowRepository) repository,
                getConfiguration(),
                settings );

            // replace it with new one
            repositoryRegistry.updateRepository( newRepository );

            List<CRepositoryShadow> reposes = getConfiguration().getRepositoryShadows();

            for ( int i = 0; i < reposes.size(); i++ )
            {
                CRepositoryShadow repo = reposes.get( i );

                if ( repo.getId().equals( settings.getId() ) )
                {
                    reposes.remove( i );

                    reposes.add( i, settings );

                    applyAndSaveConfiguration();

                    return;
                }
            }
        }
        else
        {
            throw new ConfigurationException( "Repository with ID=" + settings.getId()
                + " is not a virtual repository!" );
        }
    }

    public void deleteRepositoryShadow( String id )
        throws NoSuchRepositoryException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        if ( !ShadowRepository.class.isAssignableFrom( repository.getClass() ) )
        {
            // this is shadow
            throw new NoSuchRepositoryException( id );
        }

        repositoryRegistry.removeRepository( id );

        List<CRepositoryShadow> shadows = getConfiguration().getRepositoryShadows();

        for ( Iterator<CRepositoryShadow> i = shadows.iterator(); i.hasNext(); )
        {
            CRepositoryShadow shadow = i.next();
            if ( shadow.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    // CGroupsSettingPathMapping: CRUD
    protected void validateRoutePattern( CGroupsSettingPathMappingItem settings )
        throws ConfigurationException
    {
        ValidationResponse res = configurationValidator.validateGroupsSettingPathMappingItem( null, settings );

        if ( !res.isValid() )
        {
            throw new InvalidConfigurationException( res );
        }
    }

    public Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping()
    {
        if ( getConfiguration().getRepositoryGrouping() != null
            && getConfiguration().getRepositoryGrouping().getPathMappings() != null )
        {
            return new ArrayList<CGroupsSettingPathMappingItem>( getConfiguration()
                .getRepositoryGrouping().getPathMappings() );
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        validateRoutePattern( settings );

        if ( getConfiguration().getRepositoryGrouping() == null )
        {
            getConfiguration().setRepositoryGrouping( new CRepositoryGrouping() );
        }

        getConfiguration().getRepositoryGrouping().addPathMapping( settings );

        applyAndSaveConfiguration();
    }

    public CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException
    {
        List<CGroupsSettingPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CGroupsSettingPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CGroupsSettingPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( id ) )
            {
                return mapping;
            }
        }

        return null;
    }

    public void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        validateRoutePattern( settings );

        List<CGroupsSettingPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CGroupsSettingPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CGroupsSettingPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( settings.getId() ) )
            {
                mapping.setRouteType( settings.getRouteType() );

                mapping.setRoutePattern( settings.getRoutePattern() );

                mapping.setRepositories( settings.getRepositories() );

                break;
            }
        }

        applyAndSaveConfiguration();
    }

    public void deleteGroupsSettingPathMapping( String id )
        throws IOException
    {
        if ( getConfiguration().getRepositoryGrouping() == null
            || getConfiguration().getRepositoryGrouping().getPathMappings() == null )
        {
            return;
        }

        List<CGroupsSettingPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CGroupsSettingPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CGroupsSettingPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( id ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    // CRepositoryGroup: CRUD

    public Collection<CRepositoryGroup> listRepositoryGroups()
    {
        return new ArrayList<CRepositoryGroup>( getConfiguration().getRepositoryGrouping().getRepositoryGroups() );
    }

    public void createRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException
    {
        repositoryRegistry.addRepositoryGroup( settings.getGroupId(), settings.getRepositories() );

        getConfiguration().getRepositoryGrouping().addRepositoryGroup( settings );

        applyAndSaveConfiguration();
    }

    public CRepositoryGroup readRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException
    {
        repositoryRegistry.getRepositoryGroup( id );

        List<CRepositoryGroup> groups = getConfiguration().getRepositoryGrouping().getRepositoryGroups();

        for ( Iterator<CRepositoryGroup> i = groups.iterator(); i.hasNext(); )
        {
            CRepositoryGroup group = i.next();

            if ( group.getGroupId().equals( id ) )
            {
                return group;
            }
        }

        throw new NoSuchRepositoryGroupException( id );
    }

    public void updateRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            InvalidGroupingException,
            IOException
    {
        deleteRepositoryGroup( settings.getGroupId() );

        createRepositoryGroup( settings );
    }

    public void deleteRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        repositoryRegistry.removeRepositoryGroup( id );

        List<CRepositoryGroup> groups = getConfiguration().getRepositoryGrouping().getRepositoryGroups();

        for ( Iterator<CRepositoryGroup> i = groups.iterator(); i.hasNext(); )
        {
            CRepositoryGroup group = i.next();

            if ( group.getGroupId().equals( id ) )
            {
                i.remove();
            }
        }
        applyAndSaveConfiguration();
    }

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        List<CRemoteNexusInstance> result = null;

        if ( getConfiguration().getRemoteNexusInstances() != null )
        {
            result = Collections.unmodifiableList( getConfiguration().getRemoteNexusInstances() );
        }

        return result;
    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfiguration().getRemoteNexusInstances();

        for ( Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); )
        {
            CRemoteNexusInstance nexusInstance = i.next();

            if ( nexusInstance.getAlias().equals( alias ) )
            {
                return nexusInstance;
            }
        }

        return null;
    }

    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        getConfiguration().addRemoteNexusInstance( settings );

        applyAndSaveConfiguration();
    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfiguration().getRemoteNexusInstances();

        for ( Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); )
        {
            CRemoteNexusInstance nexusInstance = i.next();

            if ( nexusInstance.getAlias().equals( alias ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    // Repository Targets

    protected void validateCRepositoryTarget( CRepositoryTarget settings, boolean create )
        throws ConfigurationException
    {
        ApplicationValidationContext ctx = null;

        // checking for uniqueness only on CREATE event
        if ( create && getConfiguration().getRepositoryTargets() != null )
        {
            ctx = new ApplicationValidationContext();

            ctx.addExistingRepositoryTargetIds();

            List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

            for ( CRepositoryTarget target : targets )
            {
                ctx.getExistingRepositoryTargetIds().add( target.getId() );
            }
        }

        ValidationResponse res = configurationValidator.validateRepositoryTarget( ctx, settings );

        if ( res.isValid() )
        {
            boolean contentClassExists = false;

            for ( ContentClass cc : contentClasses )
            {
                if ( cc.getId().equals( settings.getContentClass() ) )
                {
                    contentClassExists = true;
                    break;
                }
            }

            if ( !contentClassExists )
            {
                throw new ConfigurationException(
                    "The Repository Target 'ContentClass' must exists: there is no class with id='"
                        + settings.getContentClass() + "'!" );
            }
        }
        else
        {
            throw new InvalidConfigurationException( res );
        }
    }

    public Collection<CRepositoryTarget> listRepositoryTargets()
    {
        List<CRepositoryTarget> result = null;

        if ( getConfiguration().getRepositoryTargets() != null )
        {
            result = Collections.unmodifiableList( getConfiguration().getRepositoryTargets() );
        }

        return result;
    }

    public void createRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException
    {
        validateCRepositoryTarget( settings, true );

        ContentClass contentClass = null;

        for ( ContentClass cl : contentClasses )
        {
            if ( settings.getContentClass().equals( cl.getId() ) )
            {
                contentClass = cl;

                break;
            }
        }

        if ( contentClass == null )
        {
            throw new ConfigurationException( "Could not find ContentClass with ID='" + settings.getContentClass()
                + "'" );
        }

        targetRegistry.addRepositoryTarget( new Target( settings.getId(), settings.getName(), contentClass, settings
            .getPatterns() ) );

        getConfiguration().addRepositoryTarget( settings );

        applyAndSaveConfiguration();
    }

    public CRepositoryTarget readRepositoryTarget( String id )
    {
        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        for ( Iterator<CRepositoryTarget> i = targets.iterator(); i.hasNext(); )
        {
            CRepositoryTarget target = i.next();

            if ( target.getId().equals( id ) )
            {
                return target;
            }
        }

        return null;
    }

    public void updateRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException
    {
        validateCRepositoryTarget( settings, false );

        CRepositoryTarget oldTarget = readRepositoryTarget( settings.getId() );

        if ( oldTarget != null )
        {
            oldTarget.setContentClass( settings.getContentClass() );

            oldTarget.setName( settings.getName() );

            oldTarget.getPatterns().clear();

            oldTarget.getPatterns().addAll( settings.getPatterns() );

            ContentClass contentClass = null;

            for ( ContentClass cl : contentClasses )
            {
                if ( oldTarget.getContentClass().equals( cl.getId() ) )
                {
                    contentClass = cl;

                    break;
                }
            }

            if ( contentClass == null )
            {
                throw new ConfigurationException( "Could not find ContentClass with ID='" + oldTarget.getContentClass()
                    + "'" );
            }

            targetRegistry.addRepositoryTarget( new Target(
                oldTarget.getId(),
                oldTarget.getName(),
                contentClass,
                oldTarget.getPatterns() ) );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new IllegalArgumentException( "Repository target with ID='" + settings.getId() + "' does not exists!" );
        }
    }

    public void deleteRepositoryTarget( String id )
        throws IOException
    {
        targetRegistry.removeRepositoryTarget( id );

        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        for ( Iterator<CRepositoryTarget> i = targets.iterator(); i.hasNext(); )
        {
            CRepositoryTarget target = i.next();

            if ( target.getId().equals( id ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    public CSmtpConfiguration readSmtpConfiguration()
    {
        return getConfiguration().getSmtpConfiguration();
    }

    public void updateSmtpConfiguration( CSmtpConfiguration settings )
        throws ConfigurationException,
            IOException
    {
        getConfiguration().setSmtpConfiguration( settings );

        applyAndSaveConfiguration();
    }
}
