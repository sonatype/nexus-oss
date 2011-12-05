/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.index.events.ReindexRepositoriesEvent;
import org.sonatype.nexus.index.events.ReindexRepositoriesRequest;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginManagerResponse;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.SecuritySystem;

/**
 * The default Nexus implementation.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
@Component( role = Nexus.class )
public class DefaultNexus
    extends AbstractLoggingComponent
    implements Nexus, Initializable, Startable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private NexusPluginManager nexusPluginManager;

    /**
     * The nexus configuration.
     */
    @Requirement
    private NexusConfiguration nexusConfiguration;

    /**
     * The repository registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    /**
     * The Scheduler.
     */
    @Requirement
    private NexusScheduler nexusScheduler;

    /**
     * The snapshot remover component.
     */
    @Requirement
    private SnapshotRemover snapshotRemover;

    /**
     * The SecurityConfiguration component.
     */
    @Requirement
    private RepositoryRouter rootRepositoryRouter;

    /**
     * Template manager.
     */
    @Requirement
    private TemplateManager templateManager;

    /**
     * The event inspector host.
     */
    @Requirement
    private EventInspectorHost eventInspectorHost;

    /**
     * The status holding component.
     */
    @Requirement
    private ApplicationStatusSource applicationStatusSource;

    /**
     * Security component
     */
    @Requirement
    private SecuritySystem securitySystem;

    @Requirement
    private PlexusEhCacheWrapper cacheWrapper;

    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    private static final String MAPPING_PROPERTIES_FILE = "packaging2extension-mapping.properties";

    // ----------------------------------------------------------------------------------------------------------
    // SystemStatus
    // ----------------------------------------------------------------------------------------------------------

    public SystemStatus getSystemStatus()
    {
        return applicationStatusSource.getSystemStatus();
    }

    public boolean setState( SystemState state )
    {
        SystemState currentState = getSystemStatus().getState();

        // only Stopped or BrokenConfig Nexus may be started
        if ( SystemState.STARTED.equals( state )
            && ( SystemState.STOPPED.equals( currentState ) || SystemState.BROKEN_CONFIGURATION.equals( currentState ) ) )
        {
            try
            {
                start();

                return true;
            }
            catch ( StartingException e )
            {
                getLogger().error( "Could not start Nexus! (currentState=" + currentState.toString() + ")", e );
            }

            return false;
        }
        // only Started Nexus may be stopped
        else if ( SystemState.STOPPED.equals( state ) && SystemState.STARTED.equals( currentState ) )
        {
            try
            {
                stop();

                return true;
            }
            catch ( StoppingException e )
            {
                getLogger().error( "Could not stop STARTED Nexus! (currentState=" + currentState.toString() + ")", e );
            }

            return false;
        }
        else
        {
            throw new IllegalArgumentException( "Illegal STATE: '" + state.toString() + "', currentState='"
                + currentState.toString() + "'" );
        }
    }

    // ----------------------------------------------------------------------------------------------------------
    // Config
    // ----------------------------------------------------------------------------------------------------------

    public NexusConfiguration getNexusConfiguration()
    {
        return nexusConfiguration;
    }

    // ----------------------------------------------------------------------------------------------------------
    // Repositories
    // ----------------------------------------------------------------------------------------------------------

    public StorageItem dereferenceLinkItem( StorageLinkItem item )
        throws NoSuchResourceStoreException, ItemNotFoundException, AccessDeniedException, IllegalOperationException,
        StorageException

    {
        return getRootRouter().dereferenceLink( item );
    }

    public RepositoryRouter getRootRouter()
    {
        return rootRepositoryRouter;
    }

    // ----------------------------------------------------------------------------
    // Repo maintenance
    // ----------------------------------------------------------------------------

    public void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
    {
        deleteRepository( id, false );
    }

    public void deleteRepository( String id, boolean force )
        throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        if ( !force && !repository.isUserManaged() )
        {
            throw new AccessDeniedException( "Not allowed to delete non-user-managed repository '" + id + "'." );
        }

        // delete the configuration
        nexusConfiguration.deleteRepository( id );
    }

    // Maintenance
    // ----------------------------------------------------------------------------

    public NexusStreamResponse getConfigurationAsStream()
        throws IOException
    {
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( "current" );

        response.setMimeType( "text/xml" );

        // TODO:
        response.setSize( 0 );

        response.setInputStream( nexusConfiguration.getConfigurationSource().getConfigurationAsStream() );

        return response;
    }

    @Deprecated
    public void expireAllCaches( ResourceStoreRequest request )
    {
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( repository.getLocalStatus().shouldServiceRequest() )
            {
                repository.expireCaches( request );
            }
        }
    }

    @Deprecated
    public void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException
    {
        this.applicationEventMulticaster.notifyEventListeners( new ReindexRepositoriesEvent(
                                                                                             this,
                                                                                             new ReindexRepositoriesRequest(
                                                                                                                             path,
                                                                                                                             fullReindex ) ) );
    }

    @Deprecated
    public Collection<String> evictAllUnusedProxiedItems( ResourceStoreRequest req, long timestamp )
        throws IOException
    {
        ArrayList<String> result = new ArrayList<String>();

        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
            {
                result.addAll( repository.evictUnusedItems( req, timestamp ) );
            }
        }

        return result;
    }

    @Deprecated
    public void rebuildMavenMetadataAllRepositories( ResourceStoreRequest req )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            if ( repo instanceof MavenRepository )
            {
                ( (MavenRepository) repo ).recreateMavenMetadata( req );
            }
        }
    }

    @Deprecated
    public void rebuildAttributesAllRepositories( ResourceStoreRequest req )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( req, null );
        }
    }

    @Deprecated
    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException, IllegalArgumentException
    {
        return snapshotRemover.removeSnapshots( request );
    }

    public Map<String, String> getConfigurationFiles()
    {
        return nexusConfiguration.getConfigurationFiles();
    }

    public NexusStreamResponse getConfigurationAsStreamByKey( String key )
        throws IOException
    {
        return nexusConfiguration.getConfigurationAsStreamByKey( key );
    }

    // ----------------------------------------------------------------------------
    // Repo templates, CRUD
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // Feeds
    // ----------------------------------------------------------------------------

    // creating
/*
    @Deprecated
    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
        feedRecorder.addNexusArtifactEvent( nae );
    }

    @Deprecated
    public void addSystemEvent( String action, String message )
    {
        feedRecorder.addSystemEvent( action, message );
    }

    @Deprecated
    public void addAuthcAuthzEvent( AuthcAuthzEvent evt )
    {
        feedRecorder.addAuthcAuthzEvent( evt );
    }

    @Deprecated
    public SystemProcess systemProcessStarted( String action, String message )
    {
        return feedRecorder.systemProcessStarted( action, message );
    }

    @Deprecated
    public void systemProcessFinished( SystemProcess prc, String finishMessage )
    {
        feedRecorder.systemProcessFinished( prc, finishMessage );
    }

    @Deprecated
    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
        feedRecorder.systemProcessBroken( prc, e );
    }

    // reading

    public List<NexusArtifactEvent> getRecentlyStorageChanges( Integer from, Integer count, Set<String> repositoryIds )
    {
        Predicate<Entry> filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
                                                        NexusArtifactEvent.ACTION_CACHED,
                                                        NexusArtifactEvent.ACTION_DEPLOYED,
                                                        NexusArtifactEvent.ACTION_DELETED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts( Integer from, Integer count,
                                                                          Set<String> repositoryIds )
    {
        Predicate<Entry> filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
                                                        NexusArtifactEvent.ACTION_CACHED,
                                                        NexusArtifactEvent.ACTION_DEPLOYED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyCachedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        Predicate<Entry> filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>(
                                                                         Arrays.asList( new String[] { NexusArtifactEvent.ACTION_CACHED } ) ),
                                                    from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        Predicate<Entry> filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>(
                                                                         Arrays.asList( new String[] { NexusArtifactEvent.ACTION_DEPLOYED } ) ),
                                                    from, count, filter );
    }

    public List<NexusArtifactEvent> getBrokenArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        Predicate<Entry> filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
                                                        NexusArtifactEvent.ACTION_BROKEN,
                                                        NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM,
                                                        NexusArtifactEvent.ACTION_BROKEN_INVALID_CONTENT } ) ),
                                                    from, count, filter );
    }

    public List<SystemEvent> getRepositoryStatusChanges( Integer from, Integer count )
    {
        return feedRecorder.getSystemEvents( new HashSet<String>( Arrays.asList( new String[] {
                                                 FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION,
                                                 FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION,
                                                 FeedRecorder.SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION } ) ), from,
                                             count, null );
    }

    public List<SystemEvent> getSystemEvents( Integer from, Integer count )
    {
        return feedRecorder.getSystemEvents( null, from, count, null );
    }

    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Integer from, Integer count )
    {
        return feedRecorder.getAuthcAuthzEvents( null, from, count, null );
    }

    public List<ErrorWarningEvent> getErrorWarningEvents( Integer from, Integer count )
    {
        return feedRecorder.getErrorWarningEvents( null, from, count, null );
    }
*/
    // ===========================
    // Nexus Application lifecycle

    public void initialize()
        throws InitializationException
    {
        StringBuffer sysInfoLog = new StringBuffer();

        sysInfoLog.append( "\n" );
        sysInfoLog.append( "-------------------------------------------------\n" );
        sysInfoLog.append( "\n" );
        sysInfoLog.append( "Initializing Nexus (" ).append( applicationStatusSource.getSystemStatus().getEditionShort() ).append( "), Version " ).append( applicationStatusSource.getSystemStatus().getVersion() ).append( "\n" );
        sysInfoLog.append( "\n" );
        sysInfoLog.append( "-------------------------------------------------" );

        getLogger().info( sysInfoLog.toString() );

        artifactPackagingMapper.setPropertiesFile( new File( nexusConfiguration.getConfigurationDirectory(),
                                                             MAPPING_PROPERTIES_FILE ) );

        // EventInspectorHost
        applicationEventMulticaster.addEventListener( eventInspectorHost );

        // load locally present plugins
        getLogger().info( "Activating locally installed plugins..." );

        Collection<PluginManagerResponse> activationResponse = nexusPluginManager.activateInstalledPlugins();

        for ( PluginManagerResponse response : activationResponse )
        {
            if ( response.isSuccessful() )
            {
                getLogger().info( response.formatAsString( getLogger().isDebugEnabled() ) );
            }
            else
            {
                getLogger().warn( response.formatAsString( getLogger().isDebugEnabled() ) );
            }
        }

        applicationStatusSource.setState( SystemState.STOPPED );

        applicationStatusSource.getSystemStatus().setOperationMode( OperationMode.STANDALONE );

        applicationStatusSource.getSystemStatus().setInitializedAt( new Date() );

        applicationEventMulticaster.notifyEventListeners( new NexusInitializedEvent( this ) );
    }

    public void start()
        throws StartingException
    {
        try
        {
            startService();
        }
        catch ( Exception e )
        {
            throw new StartingException( "Could not start Nexus!", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        try
        {
            stopService();
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Could not stop Nexus!", e );
        }
    }

    protected void startService()
        throws Exception
    {
        applicationStatusSource.getSystemStatus().setState( SystemState.STARTING );

        try
        {
            cacheWrapper.start();

            // force config load and validation
            // applies configuration and notifies listeners
            nexusConfiguration.loadConfiguration( true );

            // essential service
            securitySystem.start();

            // create internals
            nexusConfiguration.createInternals();

            // init tasks
            nexusScheduler.initializeTasks();

            // notify about start
            applicationEventMulticaster.notifyEventListeners( new ConfigurationChangeEvent( nexusConfiguration, null,
                                                                                            null ) );

            applicationStatusSource.getSystemStatus().setLastConfigChange( new Date() );

            applicationStatusSource.getSystemStatus().setFirstStart( nexusConfiguration.isConfigurationDefaulted() );

            applicationStatusSource.getSystemStatus().setInstanceUpgraded( nexusConfiguration.isInstanceUpgraded() );

            applicationStatusSource.getSystemStatus().setConfigurationUpgraded( nexusConfiguration.isConfigurationUpgraded() );

            if ( applicationStatusSource.getSystemStatus().isFirstStart() )
            {
                getLogger().info( "This is 1st start of new Nexus instance." );

                // TODO: a virgin instance, inital config created
            }

            if ( applicationStatusSource.getSystemStatus().isInstanceUpgraded() )
            {
                getLogger().info( "This is an upgraded instance of Nexus." );

                // TODO: perform upgrade or something
            }

            // sync shadows now, those needed
            synchronizeShadowsAtStartup();

            applicationStatusSource.getSystemStatus().setState( SystemState.STARTED );

            applicationStatusSource.getSystemStatus().setStartedAt( new Date() );

            getLogger().info( "Nexus Work Directory : "
                                  + nexusConfiguration.getWorkingDirectory().getAbsolutePath().toString() );

            getLogger().info( "Started Nexus (version " + getSystemStatus().getVersion() + " "
                                  + getSystemStatus().getEditionShort() + ")" );

            applicationEventMulticaster.notifyEventListeners( new NexusStartedEvent( this ) );
        }
        catch ( IOException e )
        {
            applicationStatusSource.getSystemStatus().setState( SystemState.BROKEN_IO );

            applicationStatusSource.getSystemStatus().setErrorCause( e );

            getLogger().error( "Could not start Nexus, bad IO exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }
        catch ( ConfigurationException e )
        {
            applicationStatusSource.getSystemStatus().setState( SystemState.BROKEN_CONFIGURATION );

            applicationStatusSource.getSystemStatus().setErrorCause( e );

            getLogger().error( "Could not start Nexus, user configuration exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }
    }

    protected void stopService()
        throws Exception
    {
        applicationStatusSource.getSystemStatus().setState( SystemState.STOPPING );

        applicationEventMulticaster.notifyEventListeners( new NexusStoppedEvent( this ) );

        nexusConfiguration.dropInternals();

        securitySystem.stop();

        cacheWrapper.stop();

        applicationStatusSource.getSystemStatus().setState( SystemState.STOPPED );
        
        // Now a cleanup, to kill dangling thread of HttpClients
        CustomMultiThreadedHttpConnectionManager.shutdownAll();

        getLogger().info( "Stopped Nexus (version " + getSystemStatus().getVersion() + " "
                              + getSystemStatus().getEditionShort() + ")" );
    }

    private void synchronizeShadowsAtStartup()
    {
        Collection<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet( ShadowRepository.class );

        for ( ShadowRepository shadow : shadows )
        {
            // spawn tasks to do it
            if ( shadow.isSynchronizeAtStartup() )
            {
                SynchronizeShadowsTask task = nexusScheduler.createTaskInstance( SynchronizeShadowsTask.class );

                task.setShadowRepositoryId( shadow.getId() );

                nexusScheduler.submit( "Shadow Sync (" + shadow.getId() + ")", task );
            }
        }
    }

    // ----------------------------------------------------------------------------
    // Repo templates
    // ----------------------------------------------------------------------------

    public TemplateSet getRepositoryTemplates()
    {
        return templateManager.getTemplates().getTemplates( RepositoryTemplate.class );
    }

    public RepositoryTemplate getRepositoryTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        return (RepositoryTemplate) templateManager.getTemplate( RepositoryTemplate.class, id );
    }
}
