/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.log.LogConfig;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.http.HttpProxyService;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.store.DefaultEntry;
import org.sonatype.nexus.store.Entry;
import org.sonatype.nexus.store.Store;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.RemoveRepoFolderTask;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.nexus.timeline.RepositoryIdTimelineFilter;
import org.sonatype.nexus.timeline.TimelineFilter;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;

/**
 * The default Nexus implementation.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
@Component( role = Nexus.class )
public class DefaultNexus
    extends AbstractLogEnabled
    implements Nexus, Initializable, Startable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    /**
     * The nexus configuration.
     */
    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement( hint="static" )
    private SecurityConfigurationSource defaultSecurityConfigurationSource;
    
    /**
     * The NexusIndexer.
     */
    @Requirement
    private IndexerManager indexerManager;

    /**
     * The repository registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    /**
     * The store for templates.
     */
    @Requirement( hint = "file" )
    private Store templatesStore;

    /**
     * The http proxy device.
     */
    @Requirement
    private HttpProxyService httpProxyService;

    /**
     * The Scheduler.
     */
    @Requirement
    private NexusScheduler nexusScheduler;

    /**
     * The Feed recorder.
     */
    @Requirement
    private FeedRecorder feedRecorder;

    /**
     * The snapshot remover component.
     */
    @Requirement
    private SnapshotRemover snapshotRemover;

    /**
     * The Wastebasket component.
     */
    @Requirement
    private Wastebasket wastebasket;

    /**
     * The CacheManager component.
     */
    @Requirement
    private CacheManager cacheManager;

    /**
     * The SecurityConfiguration component.
     */
    @Requirement
    private RepositoryRouter rootRepositoryRouter;

    /**
     * The LogFile Manager
     */
    @Requirement
    private LogManager logManager;

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

    // ----------------------------------------------------------------------------------------------------------
    // Template names and prefixes, not allowed to go out of this class
    // ----------------------------------------------------------------------------------------------------------

    private static final String TEMPLATE_REPOSITORY_PREFIX = "repository-";

    private static final String TEMPLATE_REPOSITORY_SHADOW_PREFIX = "repositoryShadow-";

    private static final String TEMPLATE_DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String TEMPLATE_DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String TEMPLATE_DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String TEMPLATE_DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String TEMPLATE_DEFAULT_VIRTUAL = "default_virtual";

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

    public Repository createRepository( CRepository settings )
        throws ConfigurationException, IOException
    {
        Repository repository = nexusConfiguration.createRepository( settings );

        try
        {
            indexerManager.setRepositoryIndexContextSearchable( settings.getId(), settings.isIndexable() );

            // create the initial index
            if ( settings.isIndexable() )
            {
                // Create the initial index for the repository
                ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
                rt.setRepositoryId( settings.getId() );
                nexusScheduler.submit( "Create initial index.", rt );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // will not happen, just added it
        }

        return repository;
    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        // remove the storage folders for the repository
        RemoveRepoFolderTask task = nexusScheduler.createTaskInstance( RemoveRepoFolderTask.class );

        task.setRepository( repository );

        nexusScheduler.submit( "Remove repository folder", task );

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

    public Collection<NexusStreamResponse> getApplicationLogFiles()
        throws IOException
    {
        getLogger().debug( "List log files." );

        Set<File> files = logManager.getLogFiles();

        ArrayList<NexusStreamResponse> result = new ArrayList<NexusStreamResponse>( files.size() );

        for ( File file : files )
        {
            NexusStreamResponse response = new NexusStreamResponse();

            response.setName( file.getName() );

            // TODO:
            response.setMimeType( "text/plain" );

            response.setSize( file.length() );

            response.setInputStream( null );

            result.add( response );
        }

        return result;
    }

    /**
     * Retrieves a stream to the requested log file. This method ensures that the file is rooted in the log folder to
     * prevent browsing of the file system.
     * 
     * @param logFile path of the file to retrieve
     * @returns InputStream to the file or null if the file is not allowed or doesn't exist.
     */
    public NexusStreamResponse getApplicationLogAsStream( String logFile, long from, long count )
        throws IOException
    {
        if ( !logFile.contains( File.pathSeparator ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Retrieving " + logFile + " log file." );
            }

            File log = logManager.getLogFile( logFile );

            // "chroot"ing it to nexus log dir
            if ( log.exists() )
            {
                NexusStreamResponse response = new NexusStreamResponse();

                response.setName( logFile );

                // TODO:
                response.setMimeType( "text/plain" );

                response.setSize( log.length() );

                response.setFromByte( from );

                response.setBytesCount( count );

                response.setInputStream( new LimitedInputStream( new FileInputStream( log ), from, count ) );

                return response;
            }
        }

        return null;
    }

    public LogConfig getLogConfig()
        throws IOException
    {
        return logManager.getLogConfig();
    }

    public void setLogConfig( LogConfig config )
        throws IOException
    {
        logManager.setLogConfig( config );
    }

    public void expireAllCaches( ResourceStoreRequest request )
    {
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
            {
                repository.expireCaches( request );
            }
        }
    }

    public void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException
    {
        indexerManager.reindexAllRepositories( path, fullReindex );
    }

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

    public void rebuildAttributesAllRepositories( ResourceStoreRequest req )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( req, null );
        }
    }

    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException, IllegalArgumentException
    {
        return snapshotRemover.removeSnapshots( request );
    }

    public void removeRepositoryFolder( Repository repository )
    {
        getLogger().info( "Removing storage folder of repository " + repository.getId() );

        try
        {
            wastebasket.deleteRepositoryFolders( repository );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Error during deleting repository folders ", e );
        }
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

    protected Collection<Entry> filterOnPrefix( Collection<Entry> entries, String prefix )
    {
        if ( prefix == null )
        {
            return entries;
        }

        Collection<Entry> result = new ArrayList<Entry>();

        for ( Entry e : entries )
        {
            if ( e.getId().startsWith( prefix ) )
            {
                result.add( e );
            }
        }

        return result;
    }

    public Collection<CRepository> listRepositoryTemplates()
        throws IOException
    {
        Collection<Entry> entries = filterOnPrefix( templatesStore.getEntries(), TEMPLATE_REPOSITORY_PREFIX );

        ArrayList<CRepository> result = new ArrayList<CRepository>( entries.size() );

        for ( Entry entry : entries )
        {
            result.add( (CRepository) entry.getContent() );
        }

        return result;
    }

    public void createRepositoryTemplate( CRepository settings )
        throws IOException
    {
        createRepositoryTemplate( settings, true );
    }

    protected void createRepositoryTemplate( CRepository settings, boolean replace )
        throws IOException
    {
        DefaultEntry entry = new DefaultEntry( TEMPLATE_REPOSITORY_PREFIX + settings.getId(), settings );

        if ( replace || templatesStore.getEntry( entry.getId() ) == null )
        {
            templatesStore.addEntry( entry );
        }
    }

    public CRepository readRepositoryTemplate( String id )
        throws IOException
    {
        Entry entry = templatesStore.getEntry( TEMPLATE_REPOSITORY_PREFIX + id );

        if ( entry != null )
        {
            return (CRepository) entry.getContent();
        }
        else
        {
            // check for default
            if ( TEMPLATE_DEFAULT_HOSTED_RELEASE.equals( id ) || TEMPLATE_DEFAULT_HOSTED_SNAPSHOT.equals( id )
                || TEMPLATE_DEFAULT_PROXY_RELEASE.equals( id ) || TEMPLATE_DEFAULT_PROXY_SNAPSHOT.equals( id ) )
            {
                createDefaultTemplate( id, false );

                return readRepositoryTemplate( id );
            }

            return null;
        }
    }

    public void updateRepositoryTemplate( CRepository settings )
        throws IOException
    {
        deleteRepositoryTemplate( settings.getId() );

        createRepositoryTemplate( settings );
    }

    public void deleteRepositoryTemplate( String id )
        throws IOException
    {
        templatesStore.removeEntry( TEMPLATE_REPOSITORY_PREFIX + id );
    }

    // ----------------------------------------------------------------------------
    // Default Configuration
    // ----------------------------------------------------------------------------

    public boolean isDefaultSecurityEnabled()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().isEnabled();
    }

    public boolean isDefaultAnonymousAccessEnabled()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().isAnonymousAccessEnabled();
    }

    public String getDefaultAnonymousUsername()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getAnonymousUsername();
    }

    public String getDefaultAnonymousPassword()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getAnonymousPassword();
    }

    public List<String> getDefaultRealms()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getRealms();
    }

    public NexusStreamResponse getDefaultConfigurationAsStream()
        throws IOException
    {
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( "default" );

        response.setMimeType( "text/xml" );

        // TODO:
        response.setSize( 0 );

        response.setInputStream( nexusConfiguration.getConfigurationSource().getDefaultsSource()
                                                   .getConfigurationAsStream() );

        return response;
    }

    public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration()
                                 .getGlobalConnectionSettings();
    }

    public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration()
                                 .getGlobalHttpProxySettings();
    }

    public CRouting readDefaultRouting()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration().getRouting();
    }

    public CSmtpConfiguration readDefaultSmtpConfiguration()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration()
                                 .getSmtpConfiguration();
    }

    // ----------------------------------------------------------------------------
    // Feeds
    // ----------------------------------------------------------------------------

    // creating

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
        feedRecorder.addNexusArtifactEvent( nae );
    }

    public void addSystemEvent( String action, String message )
    {
        feedRecorder.addSystemEvent( action, message );
    }

    public void addAuthcAuthzEvent( AuthcAuthzEvent evt )
    {
        feedRecorder.addAuthcAuthzEvent( evt );
    }

    public SystemProcess systemProcessStarted( String action, String message )
    {
        return feedRecorder.systemProcessStarted( action, message );
    }

    public void systemProcessFinished( SystemProcess prc, String finishMessage )
    {
        feedRecorder.systemProcessFinished( prc, finishMessage );
    }

    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
        feedRecorder.systemProcessBroken( prc, e );
    }

    // reading

    public List<NexusArtifactEvent> getRecentlyStorageChanges( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents(
                                                    new HashSet<String>( Arrays.asList( new String[] {
                                                        NexusArtifactEvent.ACTION_CACHED,
                                                        NexusArtifactEvent.ACTION_DEPLOYED,
                                                        NexusArtifactEvent.ACTION_DELETED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts( Integer from, Integer count,
                                                                          Set<String> repositoryIds )
    {
        TimelineFilter filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
            NexusArtifactEvent.ACTION_CACHED, NexusArtifactEvent.ACTION_DEPLOYED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyCachedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder
                           .getNexusArtifectEvents(
                                                    new HashSet<String>(
                                                                         Arrays
                                                                               .asList( new String[] { NexusArtifactEvent.ACTION_CACHED } ) ),
                                                    from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder
                           .getNexusArtifectEvents(
                                                    new HashSet<String>(
                                                                         Arrays
                                                                               .asList( new String[] { NexusArtifactEvent.ACTION_DEPLOYED } ) ),
                                                    from, count, filter );
    }

    public List<NexusArtifactEvent> getBrokenArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter =
            ( repositoryIds == null || repositoryIds.isEmpty() ) ? null
                            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
            NexusArtifactEvent.ACTION_BROKEN, NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM } ) ), from,
                                                    count, filter );
    }

    public List<SystemEvent> getRepositoryStatusChanges( Integer from, Integer count )
    {
        return feedRecorder.getSystemEvents( new HashSet<String>( Arrays.asList( new String[] {
            FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION, FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION,
            FeedRecorder.SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION } ) ), from, count, null );
    }

    public List<SystemEvent> getSystemEvents( Integer from, Integer count )
    {
        return feedRecorder.getSystemEvents( null, from, count, null );
    }

    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Integer from, Integer count )
    {
        return feedRecorder.getAuthcAuthzEvents( null, from, count, null );
    }

    // ===========================
    // Nexus Application lifecycle

    public void initialize()
        throws InitializationException
    {

        StringBuffer sysInfoLog = new StringBuffer();

        sysInfoLog.append( "\n" );
        sysInfoLog.append( "-------------------------------------------------\n" );
        sysInfoLog.append( "\n" );
        sysInfoLog.append( "Initializing Nexus (" )
                  .append( applicationStatusSource.getSystemStatus().getEditionShort() ).append( "), Version " )
                  .append( applicationStatusSource.getSystemStatus().getVersion() ).append( "\n" );
        sysInfoLog.append( "\n" );
        sysInfoLog.append( "-------------------------------------------------" );

        getLogger().info( sysInfoLog.toString() );

        // EventInspectorHost
        applicationEventMulticaster.addEventListener( eventInspectorHost );

        applicationStatusSource.setState( SystemState.STOPPED );

        applicationStatusSource.getSystemStatus().setOperationMode( OperationMode.STANDALONE );

        applicationStatusSource.getSystemStatus().setInitializedAt( new Date() );
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
            applicationEventMulticaster.notifyEventListeners( new ConfigurationChangeEvent( nexusConfiguration, null ) );

            addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Starting Nexus (version "
                + getSystemStatus().getVersion() + " " + getSystemStatus().getEditionShort() + ")" );

            applicationStatusSource.getSystemStatus().setLastConfigChange( new Date() );

            applicationStatusSource.getSystemStatus().setFirstStart( nexusConfiguration.isConfigurationDefaulted() );

            applicationStatusSource.getSystemStatus().setInstanceUpgraded( nexusConfiguration.isInstanceUpgraded() );

            applicationStatusSource.getSystemStatus()
                                   .setConfigurationUpgraded( nexusConfiguration.isConfigurationUpgraded() );

            // creating default templates if needed
            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_RELEASE, applicationStatusSource.getSystemStatus()
                                                                                           .isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT, applicationStatusSource.getSystemStatus()
                                                                                            .isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_RELEASE, applicationStatusSource.getSystemStatus()
                                                                                          .isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_SNAPSHOT, applicationStatusSource.getSystemStatus()
                                                                                           .isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_VIRTUAL, applicationStatusSource.getSystemStatus()
                                                                                    .isInstanceUpgraded() );

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

            getLogger().info(
                              "Nexus Work Directory : "
                                  + nexusConfiguration.getWorkingDirectory().getAbsolutePath().toString() );

            getLogger().info(
                              "Started Nexus (version " + getSystemStatus().getVersion() + " "
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

        addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Stopping Nexus (version " + getSystemStatus().getVersion()
            + " " + getSystemStatus().getEditionShort() + ")" );

        applicationEventMulticaster.notifyEventListeners( new NexusStoppedEvent( this ) );

        nexusConfiguration.dropInternals();

        securitySystem.stop();

        try
        {
            indexerManager.shutdown( false );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error while stopping IndexerManager:", e );
        }

        applicationStatusSource.getSystemStatus().setState( SystemState.STOPPED );

        getLogger().info(
                          "Stopped Nexus (version " + getSystemStatus().getVersion() + " "
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

    private void createDefaultTemplate( String id, boolean shouldRecreate )
        throws IOException
    {
        if ( TEMPLATE_DEFAULT_HOSTED_RELEASE.equals( id ) )
        {
            getLogger().info( "Creating default hosted release repository template..." );

            CRepository hostedTemplate = new DefaultCRepository();

            hostedTemplate.setProviderRole( Repository.class.getName() );

            hostedTemplate.setProviderHint( "maven2" );

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_RELEASE );

            hostedTemplate.setName( "Default Release Hosted Repository Template" );

            hostedTemplate.setAllowWrite( true );

            Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );

            hostedTemplate.setExternalConfiguration( ex );

            M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

            exConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );

            exConf.setItemMaxAge( 1440 );

            exConf.setArtifactMaxAge( -1 );

            exConf.setMetadataMaxAge( 1440 );

            exConf.applyChanges();

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default hosted snapshot repository template..." );

            CRepository hostedTemplate = new DefaultCRepository();

            hostedTemplate.setProviderRole( Repository.class.getName() );

            hostedTemplate.setProviderHint( "maven2" );

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT );

            hostedTemplate.setName( "Default Snapshot Hosted Repository Template" );

            hostedTemplate.setAllowWrite( true );

            Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );

            hostedTemplate.setExternalConfiguration( ex );

            M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

            exConf.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

            exConf.setItemMaxAge( 1440 );

            exConf.setArtifactMaxAge( 1440 );

            exConf.setMetadataMaxAge( 1440 );

            exConf.applyChanges();

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_RELEASE.equals( id ) )
        {
            getLogger().info( "Creating default proxied release repository template..." );

            CRepository proxiedTemplate = new DefaultCRepository();

            proxiedTemplate.setProviderRole( Repository.class.getName() );

            proxiedTemplate.setProviderHint( "maven2" );

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_RELEASE );

            proxiedTemplate.setName( "Default Release Proxy Repository Template" );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );

            proxiedTemplate.setExternalConfiguration( ex );

            M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

            exConf.setItemMaxAge( 1440 );

            exConf.setArtifactMaxAge( -1 );

            exConf.setMetadataMaxAge( 1440 );

            exConf.applyChanges();

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default proxied snapshot repository template..." );

            CRepository proxiedTemplate = new DefaultCRepository();

            proxiedTemplate.setProviderRole( Repository.class.getName() );

            proxiedTemplate.setProviderHint( "maven2" );

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_SNAPSHOT );

            proxiedTemplate.setName( "Default Snapshot Proxy Repository Template" );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );

            proxiedTemplate.setExternalConfiguration( ex );

            M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

            exConf.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

            exConf.setItemMaxAge( 1440 );

            exConf.setArtifactMaxAge( 1440 );

            exConf.setMetadataMaxAge( 1440 );

            exConf.applyChanges();

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_VIRTUAL.equals( id ) )
        {
            getLogger().info( "Creating default virtual repository template..." );

            CRepository shadowTemplate = new DefaultCRepository();

            shadowTemplate.setProviderRole( ShadowRepository.class.getName() );

            shadowTemplate.setProviderHint( "m1-m2-shadow" );

            shadowTemplate.setId( TEMPLATE_DEFAULT_VIRTUAL );

            shadowTemplate.setName( "Default Virtual Repository Template" );

            createRepositoryTemplate( shadowTemplate, shouldRecreate );
        }
    }
}
