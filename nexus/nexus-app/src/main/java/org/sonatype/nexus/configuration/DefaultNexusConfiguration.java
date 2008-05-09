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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.configuration.model.CAuthSource;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.runtime.RuntimeConfigurationBuilder;
import org.sonatype.nexus.configuration.source.ConfigurationSource;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.maven.M2Repository;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.security.AuthenticationSource;

/**
 * The class DefaultNexusConfiguration is responsible for config management. It actually keeps in sync Nexus internal
 * state with persisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus current
 * state and Nexus user config.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexusConfiguration
    extends AbstractLogEnabled
    implements NexusConfiguration
{

    /**
     * The configuration source.
     * 
     * @plexus.requirement role-hint="file"
     */
    private ConfigurationSource configurationSource;

    /**
     * The config validator.
     * 
     * @plexus.requirement
     */
    private ConfigurationValidator configurationValidator;

    /**
     * The runtime configuration builder.
     * 
     * @plexus.requirement
     */
    private RuntimeConfigurationBuilder runtimeConfigurationBuilder;

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /** The global remote storage context. */
    private RemoteStorageContext remoteStorageContext;

    /** The working directory. */
    private File workingDirectory;

    /** The app log dir */
    private File applicationLogDirectory;

    /** The config dir */
    private File configurationDirectory;

    /** The temp dir */
    private File temporaryDirectory;

    /** The config event listeners. */
    private Vector<ConfigurationChangeListener> configurationChangeListeners = new Vector<ConfigurationChangeListener>();

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

            workingDirectory = null;

            configurationDirectory = null;

            applicationLogDirectory = null;

            // create shared remote ctx
            remoteStorageContext = new DefaultRemoteStorageContext();

            if ( getConfiguration().getGlobalConnectionSettings() != null )
            {
                remoteStorageContext.setRemoteConnectionSettings( getConfiguration().getGlobalConnectionSettings() );
            }

            if ( getConfiguration().getGlobalHttpProxySettings() != null )
            {
                remoteStorageContext.setRemoteHttpProxySettings( getConfiguration().getGlobalHttpProxySettings() );
            }

            // and register things
            runtimeConfigurationBuilder.initialize( this );

            createRepositories();

            notifyConfigurationChangeListeners();
        }
    }

    public void applyConfiguration()
        throws IOException
    {
        getLogger().info( "Applying Nexus Configuration..." );

        workingDirectory = null;

        configurationDirectory = null;

        applicationLogDirectory = null;

        notifyConfigurationChangeListeners();
    }

    protected void saveConfiguration()
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

    public void addConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        configurationChangeListeners.add( listener );
    }

    public void removeConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        configurationChangeListeners.remove( listener );
    }

    public void notifyConfigurationChangeListeners()
    {
        notifyConfigurationChangeListeners( new ConfigurationChangeEvent( this ) );
    }

    public void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt )
    {
        synchronized ( configurationChangeListeners )
        {
            for ( ConfigurationChangeListener l : configurationChangeListeners )
            {
                try
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Notifying component about config change: " + l.getClass().getName() );
                    }

                    l.onConfigurationChange( evt );
                }
                catch ( Exception e )
                {
                    getLogger().info( "Unexpected exception in listener", e );
                }
            }
        }
    }

    public Configuration getConfiguration()
    {
        return configurationSource.getConfiguration();
    }

    public ConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return configurationSource.getConfigurationAsStream();
    }

    public boolean isInstanceUpgradeNeeded()
    {
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationDefaulted()
    {
        return configurationSource.isConfigurationDefaulted();
    }

    public File getWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            workingDirectory = new File( getConfiguration().getWorkingDirectory() );

            if ( !workingDirectory.exists() )
            {
                workingDirectory.mkdirs();
            }
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

    public File getApplicationLogDirectory()
    {
        if ( applicationLogDirectory == null )
        {
            applicationLogDirectory = new File( getConfiguration().getApplicationLogDirectory() );
        }
        return applicationLogDirectory;
    }

    public AuthenticationSource getAuthenticationSource()
        throws ConfigurationException
    {
        return runtimeConfigurationBuilder.getAuthenticationSource( getConfiguration() );
    }

    public AccessManager getAccessManagerForRealm( String realmId )
        throws ConfigurationException
    {
        return runtimeConfigurationBuilder.getAccessManagerForRealm( getConfiguration(), realmId );
    }

    // ----------------------------------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------------------------------

    protected void removeRepositories()
    {
        try
        {
            // get all IDs
            List<String> repoIds = repositoryRegistry.getRepositoryIds();

            for ( String repoId : repoIds )
            {
                repositoryRegistry.removeRepository( repoId );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // will not happen
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void createRepositories()
        throws ConfigurationException,
            IOException
    {
        removeRepositories();

        List<CRepository> reposes = getConfiguration().getRepositories();

        for ( CRepository repo : reposes )
        {
            Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel( getConfiguration(), repo );

            repositoryRegistry.addRepository( repository );
        }

        if ( getConfiguration().getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> shadows = getConfiguration().getRepositoryShadows();
            for ( CRepositoryShadow shadow : shadows )
            {
                Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel(
                    getConfiguration(),
                    shadow );
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

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections

    public boolean isSecurityEnabled()
    {
        return getConfiguration().getSecurity() != null && getConfiguration().getSecurity().isEnabled();
    }

    public boolean isAnonymousAccessEnabled()
    {
        return getConfiguration().getSecurity() != null && getConfiguration().getSecurity().isAnonymousAccessEnabled();
    }
    
    public boolean isSimpleSecurityModel()
    {
        return "simple".equals( getAuthenticationSourceType() );
    }

    public String getAuthenticationSourceType()
    {
        if ( isSecurityEnabled() )
        {
            return getConfiguration().getSecurity().getAuthenticationSource().getType();
        }
        else
        {
            return null;
        }
    }

    public void setSecurity( boolean enabled, String authenticationSourceType )
        throws IOException
    {
        if ( !enabled )
        {
            getConfiguration().getSecurity().setEnabled( false );

            getConfiguration().getSecurity().setAnonymousAccessEnabled( false );

            getConfiguration().getSecurity().setAuthenticationSource( null );

            getConfiguration().getSecurity().setRealms( null );
        }
        else
        {
            getConfiguration().getSecurity().setEnabled( true );

            if ( "simple".equals( authenticationSourceType ) )
            {
                getConfiguration().getSecurity().setAnonymousAccessEnabled( true );
            }
            else
            {
                getConfiguration().getSecurity().setAnonymousAccessEnabled( false );
            }

            getConfiguration().getSecurity().setAuthenticationSource( new CAuthSource() );

            getConfiguration().getSecurity().getAuthenticationSource().setType( authenticationSourceType );
        }

        applyAndSaveConfiguration();
    }

    // Globals are mandatory: RU

    public String readWorkingDirectory()
    {
        return getConfiguration().getWorkingDirectory();
    }

    public void updateWorkingDirectory( String settings )
        throws IOException
    {
        getConfiguration().setWorkingDirectory( settings );

        applyAndSaveConfiguration();
    }

    public String readApplicationLogDirectory()
    {
        return getConfiguration().getApplicationLogDirectory();
    }

    public void updateApplicationLogDirectory( String settings )
        throws IOException
    {
        getConfiguration().setApplicationLogDirectory( settings );

        applyAndSaveConfiguration();
    }

    // CRemoteConnectionSettings are mandatory: RU

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        return getConfiguration().getGlobalConnectionSettings();
    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException
    {
        remoteStorageContext.setRemoteConnectionSettings( settings );

        getConfiguration().setGlobalConnectionSettings( settings );

        applyAndSaveConfiguration();
    }

    // CRemoteHttpProxySettings are optional: CRUD

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        remoteStorageContext.setRemoteHttpProxySettings( settings );

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
        remoteStorageContext.setRemoteHttpProxySettings( null );

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

    // CRepository: CRUD

    public Collection<CRepository> listRepositories()
    {
        return new ArrayList<CRepository>( getConfiguration().getRepositories() );
    }

    public void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateRepository( settings );

        if ( vr.isValid() )
        {

            Repository repository = runtimeConfigurationBuilder
                .createRepositoryFromModel( getConfiguration(), settings );

            repositoryRegistry.addRepository( repository );

            getConfiguration().getRepositories().add( settings );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
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
        ValidationResponse vr = configurationValidator.validateRepository( settings );

        if ( vr.isValid() )
        {
            Repository repository = repositoryRegistry.getRepository( settings.getId() );

            if ( M2Repository.class.isAssignableFrom( repository.getClass() ) )
            {
                Repository newRepository = runtimeConfigurationBuilder.createRepositoryFromModel(
                    getConfiguration(),
                    settings );

                // this is ok, registry maintains a map of reposes, this will simply replace the old one
                repositoryRegistry.addRepository( newRepository );

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
        else
        {
            throw new InvalidConfigurationException( vr );
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

    public Collection<CRepositoryShadow> listRepositoryShadows()
    {
        return new ArrayList<CRepositoryShadow>( getConfiguration().getRepositoryShadows() );
    }

    public void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException
    {
        ValidationResponse vr = configurationValidator.validateRepository( settings );

        if ( vr.isValid() )
        {

            Repository repository = runtimeConfigurationBuilder
                .createRepositoryFromModel( getConfiguration(), settings );

            repositoryRegistry.addRepository( repository );

            getConfiguration().getRepositoryShadows().add( settings );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
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
        ValidationResponse vr = configurationValidator.validateRepository( settings );

        if ( vr.isValid() )
        {
            Repository repository = repositoryRegistry.getRepository( settings.getId() );

            if ( ShadowRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                Repository newRepository = runtimeConfigurationBuilder.createRepositoryFromModel(
                    getConfiguration(),
                    settings );

                // this is ok, registry maintains a map of reposes, this will simply replace the old one
                repositoryRegistry.addRepository( newRepository );

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
        else
        {
            throw new InvalidConfigurationException( vr );
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

}
