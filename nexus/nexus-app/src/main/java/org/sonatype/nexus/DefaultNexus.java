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
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.log.SimpleLog4jConfig;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.http.HttpProxyService;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.store.DefaultEntry;
import org.sonatype.nexus.store.Entry;
import org.sonatype.nexus.store.Store;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.RemoveRepoFolderTask;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.timeline.RepositoryIdTimelineFilter;
import org.sonatype.nexus.timeline.TimelineFilter;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.Schedule;

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
    /**
     * The nexus configuration.
     */
    @Requirement
    private NexusConfiguration nexusConfiguration;

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
     * The NexusSecurity.
     */
    @Requirement
    private NexusSecurity security;

    /**
     * The SecurityConfiguration component.
     */
    @Requirement
    private RepositoryRouter rootRepositoryRouter;

    /**
     * The LogFile Manager
     */
    @Requirement
    private LogFileManager logFileManager;

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

    public Repository getRepository( String repoId )
        throws NoSuchRepositoryException
    {
        return repositoryRegistry.getRepository( repoId );
    }

    public <T> T getRepositoryWithFacet( String repoId, Class<T> f )
        throws NoSuchRepositoryException
    {
        return repositoryRegistry.getRepositoryWithFacet( repoId, f );
    }

    public Collection<Repository> getRepositories()
    {
        return repositoryRegistry.getRepositories();
    }

    public <T> Collection<T> getRepositoriesWithFacet( Class<T> f )
    {
        return repositoryRegistry.getRepositoriesWithFacet( f );
    }

    public StorageItem dereferenceLinkItem( StorageLinkItem item )
        throws NoSuchResourceStoreException,
            ItemNotFoundException,
            AccessDeniedException,
            IllegalOperationException,
            StorageException

    {
        return getRootRouter().dereferenceLink( item );
    }

    public RepositoryRouter getRootRouter()
    {
        return rootRepositoryRouter;
    }

    // ----------------------------------------------------------------------------------------------------------
    // Wastebasket
    // ----------------------------------------------------------------------------------------------------------

    public long getWastebasketItemCount()
        throws IOException
    {
        return wastebasket.getItemCount();
    }

    public long getWastebasketSize()
        throws IOException
    {
        return wastebasket.getSize();
    }

    public void wastebasketPurge()
        throws IOException
    {
        wastebasket.purge();
    }

    // ------------------------------------------------------------------
    // Security

    public boolean isSecurityEnabled()
    {
        return nexusConfiguration.isSecurityEnabled();
    }

    public void setSecurityEnabled( boolean enabled )
        throws IOException
    {
        nexusConfiguration.setSecurityEnabled( enabled );
    }

    public void setRealms( List<String> realms )
        throws IOException
    {
        nexusConfiguration.getRealms().clear();
        nexusConfiguration.setRealms( realms );
    }

    public boolean isAnonymousAccessEnabled()
    {
        return nexusConfiguration.isAnonymousAccessEnabled();
    }

    public void setAnonymousAccessEnabled( boolean enabled )
        throws IOException
    {
        nexusConfiguration.setAnonymousAccessEnabled( enabled );
    }

    public String getAnonymousUsername()
    {
        return nexusConfiguration.getAnonymousUsername();
    }

    public void setAnonymousUsername( String val )
        throws IOException
    {
        nexusConfiguration.setAnonymousUsername( val );
    }

    public String getAnonymousPassword()
    {
        return nexusConfiguration.getAnonymousPassword();
    }

    public void setAnonymousPassword( String val )
        throws IOException
    {
        nexusConfiguration.setAnonymousPassword( val );
    }

    public List<String> getRealms()
    {
        return nexusConfiguration.getRealms();
    }

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections

    public String getBaseUrl()
    {
        return nexusConfiguration.getBaseUrl();
    }

    public void setBaseUrl( String baseUrl )
        throws IOException
    {
        nexusConfiguration.setBaseUrl( baseUrl );
    }

    public boolean isForceBaseUrl()
    {
        return nexusConfiguration.isForceBaseUrl();
    }

    public void setForceBaseUrl( boolean force )
        throws IOException
    {
        nexusConfiguration.setForceBaseUrl( force );
    }

    // Globals are mandatory: RU

    // CRemoteConnectionSettings are mandatory: RU

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        return nexusConfiguration.readGlobalRemoteConnectionSettings();
    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGlobalRemoteConnectionSettings( settings );
    }

    // CRemoteHttpProxySettings are optional: CRUD

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createGlobalRemoteHttpProxySettings( settings );
    }

    public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
    {
        return nexusConfiguration.readGlobalRemoteHttpProxySettings();
    }

    public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGlobalRemoteHttpProxySettings( settings );
    }

    public void deleteGlobalRemoteHttpProxySettings()
        throws IOException
    {
        nexusConfiguration.deleteGlobalRemoteHttpProxySettings();
    }

    // CRouting are mandatory: RU

    public CRouting readRouting()
    {
        return nexusConfiguration.readRouting();
    }

    public void updateRouting( CRouting settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRouting( settings );
    }

    // CRepository: CRUD

    public Collection<CRepository> listRepositories()
    {
        return nexusConfiguration.listRepositories();
    }

    public void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createRepository( settings );

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
    }

    public CRepository readRepository( String id )
        throws NoSuchRepositoryException
    {
        return nexusConfiguration.readRepository( id );
    }

    public void updateRepository( CRepository settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        // check current settings for download Index
        boolean previousDownloadRemoteIndexes = this.readRepository( settings.getId() ).isDownloadRemoteIndexes();

        nexusConfiguration.updateRepository( settings );

        indexerManager.setRepositoryIndexContextSearchable( settings.getId(), settings.isIndexable() );

        // create the initial index
        if ( !previousDownloadRemoteIndexes && settings.isDownloadRemoteIndexes() )
        {
            // Create the initial index for the repository
            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
            rt.setRepositoryId( settings.getId() );
            nexusScheduler.submit( "Download remote index enabled.", rt );
        }
    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException,
            IOException,
            ConfigurationException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        File defaultStorageFile = new File( new File( nexusConfiguration.getWorkingDirectory(), "storage" ), repository
            .getId() );

        // only remove the storage folder when in default storage case
        if ( defaultStorageFile.toURL().toString().equals( repository.getLocalUrl() + "/" ) )
        {
            // remove the storage folders for the repository
            RemoveRepoFolderTask task = nexusScheduler.createTaskInstance( RemoveRepoFolderTask.class );

            task.setRepository( repository );

            nexusScheduler.submit( "Remove repository folder", task );
        }

        // delete the configuration
        nexusConfiguration.deleteRepository( id );
    }

    // CRepositoryShadow: CRUD

    public Collection<CRepositoryShadow> listRepositoryShadows()
    {
        return nexusConfiguration.listRepositoryShadows();
    }

    public void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createRepositoryShadow( settings );
    }

    public CRepositoryShadow readRepositoryShadow( String id )
        throws NoSuchRepositoryException
    {
        return nexusConfiguration.readRepositoryShadow( id );
    }

    public void updateRepositoryShadow( CRepositoryShadow settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRepositoryShadow( settings );
    }

    public void deleteRepositoryShadow( String id )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.deleteRepositoryShadow( id );
    }

    // CGroupsSettingPathMapping: CRUD

    public Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping()
    {
        return nexusConfiguration.listGroupsSettingPathMapping();
    }

    public void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.createGroupsSettingPathMapping( settings );
    }

    public CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException
    {
        return nexusConfiguration.readGroupsSettingPathMapping( id );
    }

    public void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGroupsSettingPathMapping( settings );
    }

    public void deleteGroupsSettingPathMapping( String id )
        throws IOException
    {
        nexusConfiguration.deleteGroupsSettingPathMapping( id );
    }

    // CRepositoryGroup: CRUD

    public Collection<CRepositoryGroup> listRepositoryGroups()
    {
        return nexusConfiguration.listRepositoryGroups();
    }

    public void createRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException,
            ConfigurationException
    {
        nexusConfiguration.createRepositoryGroup( settings );
    }

    public CRepositoryGroup readRepositoryGroup( String id )
        throws NoSuchRepositoryException
    {
        return nexusConfiguration.readRepositoryGroup( id );
    }

    public void updateRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException,
            ConfigurationException
    {
        nexusConfiguration.updateRepositoryGroup( settings );
    }

    public void deleteRepositoryGroup( String id )
        throws NoSuchRepositoryException,
            IOException
    {
        nexusConfiguration.deleteRepositoryGroup( id );
    }

    public Collection<CRepositoryTarget> listRepositoryTargets()
    {
        return nexusConfiguration.listRepositoryTargets();
    }

    public void createRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createRepositoryTarget( settings );
    }

    public CRepositoryTarget readRepositoryTarget( String id )
    {
        return nexusConfiguration.readRepositoryTarget( id );
    }

    public void updateRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRepositoryTarget( settings );
    }

    public void deleteRepositoryTarget( String id )
        throws IOException
    {
        nexusConfiguration.deleteRepositoryTarget( id );
    }

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        return nexusConfiguration.listRemoteNexusInstances();
    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        return nexusConfiguration.readRemoteNexusInstance( alias );
    }

    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        nexusConfiguration.createRemoteNexusInstance( settings );
    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        nexusConfiguration.deleteRemoteNexusInstance( alias );
    }

    public CSmtpConfiguration readSmtpConfiguration()
    {
        return nexusConfiguration.readSmtpConfiguration();
    }

    public void updateSmtpConfiguration( CSmtpConfiguration settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateSmtpConfiguration( settings );
    }

    // =============
    // Maintenance

    public NexusStreamResponse getConfigurationAsStream()
        throws IOException
    {
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( "current" );

        response.setMimeType( "text/xml" );

        // TODO:
        response.setSize( 0 );

        response.setInputStream( nexusConfiguration.getConfigurationAsStream() );

        return response;
    }

    public Collection<NexusStreamResponse> getApplicationLogFiles()
        throws IOException
    {
        getLogger().debug( "List log files." );

        Set<File> files = logFileManager.getLogFiles();

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
            getLogger().debug( "Retrieving " + logFile + " log file." );

            File log = logFileManager.getLogFile( logFile );

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

    public void clearAllCaches( String path )
    {
        getLogger().info( "Clearing caches in all repositories from path " + path );

        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            repository.clearCaches( path );
        }
    }

    public void clearRepositoryCaches( String path, String repositoryId )
        throws NoSuchRepositoryException
    {
        getLogger().info( "Clearing caches in repository " + repositoryId + " from path " + path );

        repositoryRegistry.getRepository( repositoryId ).clearCaches( path );
    }

    public void clearRepositoryGroupCaches( String path, String repositoryGroupId )
        throws NoSuchRepositoryException
    {
        getLogger().info( "Clearing caches in repository group " + repositoryGroupId + " from path " + path );

        for ( Repository repository : repositoryRegistry.getRepositoryWithFacet(
            repositoryGroupId,
            GroupRepository.class ).getMemberRepositories() )
        {
            repository.clearCaches( path );
        }
    }

    protected Collection<String> evictUnusedItems( long timestamp, Repository repository, boolean proxyOnly )
        throws IOException
    {
        if ( proxyOnly && repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return repository.evictUnusedItems( timestamp );
        }
        else
        {
            return repository.evictUnusedItems( timestamp );
        }
    }

    public Collection<String> evictAllUnusedProxiedItems( long timestamp )
        throws IOException
    {
        getLogger().info( "Evicting unused items in all repositories." );

        ArrayList<String> result = new ArrayList<String>();

        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            result.addAll( evictUnusedItems( timestamp, repository, true ) );
        }

        return result;
    }

    public Collection<String> evictRepositoryUnusedProxiedItems( long timestamp, String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        getLogger().info( "Evicting unused items from repository " + repositoryId + "." );

        return evictUnusedItems( timestamp, repositoryRegistry.getRepository( repositoryId ), true );
    }

    public Collection<String> evictRepositoryGroupUnusedProxiedItems( long timestamp, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException
    {
        getLogger().info( "Evicting unused items from repositories in group " + repositoryGroupId + "." );

        ArrayList<String> result = new ArrayList<String>();

        for ( Repository repository : repositoryRegistry.getRepositoryWithFacet(
            repositoryGroupId,
            GroupRepository.class ).getMemberRepositories() )
        {
            result.addAll( evictUnusedItems( timestamp, repository, true ) );
        }

        return result;
    }

    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            IllegalArgumentException
    {
        return snapshotRemover.removeSnapshots( request );
    }

    public void synchronizeShadow( String shadowRepositoryId )
        throws NoSuchRepositoryException
    {
        try
        {
            ShadowRepository shadowRepo = repositoryRegistry.getRepositoryWithFacet(
                shadowRepositoryId,
                ShadowRepository.class );

            shadowRepo.synchronizeWithMaster();
        }
        catch ( ClassCastException e )
        {
            // the repo exists but is not shadow???
            throw new NoSuchRepositoryException( shadowRepositoryId );
        }
    }

    // ------------------------------------------------------------------
    // Repo templates, CRUD

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

    public void createRepositoryTemplate( CRepository settings, boolean replace )
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

    public Collection<CRepositoryShadow> listRepositoryShadowTemplates()
        throws IOException
    {
        Collection<Entry> entries = filterOnPrefix( templatesStore.getEntries(), TEMPLATE_REPOSITORY_SHADOW_PREFIX );

        ArrayList<CRepositoryShadow> result = new ArrayList<CRepositoryShadow>( entries.size() );

        for ( Entry entry : entries )
        {
            result.add( (CRepositoryShadow) entry.getContent() );
        }

        return result;
    }

    public void createRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        createRepositoryShadowTemplate( settings, true );
    }

    public void createRepositoryShadowTemplate( CRepositoryShadow settings, boolean replace )
        throws IOException
    {
        DefaultEntry entry = new DefaultEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + settings.getId(), settings );

        if ( replace || templatesStore.getEntry( entry.getId() ) == null )
        {
            templatesStore.addEntry( entry );
        }
    }

    public CRepositoryShadow readRepositoryShadowTemplate( String id )
        throws IOException
    {
        Entry entry = templatesStore.getEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + id );

        if ( entry != null )
        {
            return (CRepositoryShadow) entry.getContent();
        }
        else
        {
            // check for default
            if ( TEMPLATE_DEFAULT_VIRTUAL.equals( id ) )
            {
                createDefaultTemplate( id, false );

                return readRepositoryShadowTemplate( id );
            }
            return null;
        }
    }

    public void updateRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        deleteRepositoryShadowTemplate( settings.getId() );

        createRepositoryShadowTemplate( settings );
    }

    public void deleteRepositoryShadowTemplate( String id )
        throws IOException
    {
        templatesStore.removeEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + id );
    }

    // ------------------------------------------------------------------
    // ContentClasses

    public Collection<ContentClass> listRepositoryContentClasses()
    {
        return nexusConfiguration.listRepositoryContentClasses();
    }

    // ------------------------------------------------------------------
    // Scheduled Tasks
    public List<ScheduledTaskDescriptor> listScheduledTaskDescriptors()
    {
        return nexusConfiguration.listScheduledTaskDescriptors();
    }

    public ScheduledTaskDescriptor getScheduledTaskDescriptor( String id )
    {
        return nexusConfiguration.getScheduledTaskDescriptor( id );
    }

    // ------------------------------------------------------------------
    // Configuration defaults

    public boolean isDefaultSecurityEnabled()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().isEnabled();
    }

    public boolean isDefaultAnonymousAccessEnabled()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().isAnonymousAccessEnabled();
    }

    public String getDefaultAnonymousUsername()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().getAnonymousUsername();
    }

    public String getDefaultAnonymousPassword()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().getAnonymousPassword();
    }

    public List<String> getDefaultRealms()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().getRealms();
    }

    public NexusStreamResponse getDefaultConfigurationAsStream()
        throws IOException
    {
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( "default" );

        response.setMimeType( "text/xml" );

        // TODO:
        response.setSize( 0 );

        response.setInputStream( nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );

        return response;
    }

    public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getGlobalConnectionSettings();
    }

    public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getGlobalHttpProxySettings();
    }

    public CRouting readDefaultRouting()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration().getRouting();
    }

    public CSmtpConfiguration readDefaultSmtpConfiguration()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSmtpConfiguration();
    }

    // =============
    // Feeds

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
        TimelineFilter filter = ( repositoryIds == null || repositoryIds.isEmpty() )
            ? null
            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
            NexusArtifactEvent.ACTION_CACHED,
            NexusArtifactEvent.ACTION_DEPLOYED,
            NexusArtifactEvent.ACTION_DELETED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts( Integer from, Integer count,
        Set<String> repositoryIds )
    {
        TimelineFilter filter = ( repositoryIds == null || repositoryIds.isEmpty() )
            ? null
            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
            NexusArtifactEvent.ACTION_CACHED,
            NexusArtifactEvent.ACTION_DEPLOYED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyCachedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter = ( repositoryIds == null || repositoryIds.isEmpty() )
            ? null
            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays
            .asList( new String[] { NexusArtifactEvent.ACTION_CACHED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getRecentlyDeployedArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter = ( repositoryIds == null || repositoryIds.isEmpty() )
            ? null
            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays
            .asList( new String[] { NexusArtifactEvent.ACTION_DEPLOYED } ) ), from, count, filter );
    }

    public List<NexusArtifactEvent> getBrokenArtifacts( Integer from, Integer count, Set<String> repositoryIds )
    {
        TimelineFilter filter = ( repositoryIds == null || repositoryIds.isEmpty() )
            ? null
            : new RepositoryIdTimelineFilter( repositoryIds );

        return feedRecorder.getNexusArtifectEvents( new HashSet<String>( Arrays.asList( new String[] {
            NexusArtifactEvent.ACTION_BROKEN,
            NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM } ) ), from, count, filter );
    }

    public List<SystemEvent> getRepositoryStatusChanges( Integer from, Integer count )
    {
        return feedRecorder.getSystemEvents( new HashSet<String>( Arrays.asList( new String[] {
            FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION,
            FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION,
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

    // =============
    // Schedules

    public <T> ScheduledTask<T> submit( String name, NexusTask<T> task )
        throws RejectedExecutionException,
            NullPointerException
    {
        return nexusScheduler.submit( name, task );
    }

    public <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException
    {
        return nexusScheduler.schedule( name, nexusTask, schedule );
    }

    public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException
    {
        return nexusScheduler.updateSchedule( task );
    }

    public Map<String, List<ScheduledTask<?>>> getAllTasks()
    {
        return nexusScheduler.getAllTasks();
    }

    public Map<String, List<ScheduledTask<?>>> getActiveTasks()
    {
        return nexusScheduler.getActiveTasks();
    }

    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        return nexusScheduler.getTaskById( id );
    }

    public NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return nexusScheduler.createTaskInstance( taskType );
    }

    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        return nexusScheduler.createTaskInstance( taskType );
    }

    // =============
    // Search and indexing related

    public void reindexAllRepositories( String path )
        throws IOException
    {
        indexerManager.reindexAllRepositories( path );
    }

    public void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        indexerManager.reindexRepository( path, repositoryId );
    }

    public void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException
    {
        indexerManager.reindexRepositoryGroup( path, repositoryGroupId );
    }

    public void publishAllIndex()
        throws IOException
    {
        indexerManager.publishAllIndex();
    }

    public void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        indexerManager.publishRepositoryIndex( repositoryId );
    }

    public void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryException
    {
        indexerManager.publishRepositoryGroupIndex( repositoryGroupId );
    }

    public void rebuildMavenMetadataAllRepositories( String path )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            if ( repo instanceof MavenRepository )
            {
                ( (MavenRepository) repo ).recreateMavenMetadata( path );
            }
        }
    }

    public void rebuildMavenMetadataRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        Repository repo = repositoryRegistry.getRepository( repositoryId );

        if ( repo instanceof MavenRepository )
        {
            ( (MavenRepository) repo ).recreateMavenMetadata( path );
        }
    }

    public void rebuildMavenMetadataRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException
    {
        List<Repository> reposes = repositoryRegistry
            .getRepositoryWithFacet( repositoryGroupId, GroupRepository.class ).getMemberRepositories();

        for ( Repository repo : reposes )
        {
            if ( repo instanceof MavenRepository )
            {
                ( (MavenRepository) repo ).recreateMavenMetadata( path );
            }
        }
    }

    public void rebuildAttributesAllRepositories( String path )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( path, null );
        }
    }

    public void rebuildAttributesRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        repositoryRegistry.getRepository( repositoryId ).recreateAttributes( path, null );
    }

    public void rebuildAttributesRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException
    {
        List<Repository> reposes = repositoryRegistry
            .getRepositoryWithFacet( repositoryGroupId, GroupRepository.class ).getMemberRepositories();

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( path, null );
        }
    }

    //
    // Indexing
    //

    public IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        return indexerManager.getRepositoryLocalIndexContext( repositoryId );
    }

    public IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        return indexerManager.getRepositoryRemoteIndexContext( repositoryId );
    }

    public IndexingContext getRepositoryBestIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        return indexerManager.getRepositoryBestIndexContext( repositoryId );
    }

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        return indexerManager.identifyArtifact( type, checksum );
    }

    public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, String groupId, Integer from,
        Integer count )
    {
        return indexerManager.searchArtifactFlat( term, repositoryId, groupId, from, count );
    }

    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, String groupId, Integer from,
        Integer count )
    {
        return indexerManager.searchArtifactClassFlat( term, repositoryId, groupId, from, count );
    }

    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, String groupId, Integer from, Integer count )
    {
        return indexerManager
            .searchArtifactFlat( gTerm, aTerm, vTerm, pTerm, cTerm, repositoryId, groupId, from, count );
    }

    // ===========================
    // Nexus Application lifecycle

    public void initialize()
        throws InitializationException
    {
        // EventInspectorHost -- BEGIN
        // tying in eventInspectorHost to all event producers
        repositoryRegistry.addProximityEventListener( eventInspectorHost );

        nexusConfiguration.addProximityEventListener( eventInspectorHost );

        security.addProximityEventListener( eventInspectorHost );
        // EventInspectorHost -- END

        applicationStatusSource.setState( SystemState.STOPPED );

        applicationStatusSource.getSystemStatus().setOperationMode( OperationMode.STANDALONE );

        applicationStatusSource.getSystemStatus().setInitializedAt( new Date() );

        getLogger().info( "Initialized Nexus (version " + applicationStatusSource.getSystemStatus().getVersion() + ")" );
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

    public void startService()
        throws Exception
    {
        applicationStatusSource.getSystemStatus().setState( SystemState.STARTING );

        try
        {
            // force config load and validation
            // applies configuration and notifies listeners
            nexusConfiguration.loadConfiguration( true );

            // essential service
            security.startService();

            // essential service
            cacheManager.startService();

            // create internals
            nexusConfiguration.createInternals();

            // notify about start
            nexusConfiguration.notifyProximityEventListeners( new ConfigurationChangeEvent( nexusConfiguration ) );

            feedRecorder.startService();

            nexusScheduler.startService();

            addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Starting Nexus (version "
                + getSystemStatus().getVersion() + " " + getSystemStatus().getEditionShort() + ")" );

            applicationStatusSource.getSystemStatus().setLastConfigChange( new Date() );

            applicationStatusSource.getSystemStatus().setConfigurationValidationResponse(
                nexusConfiguration.getConfigurationSource().getValidationResponse() );

            applicationStatusSource.getSystemStatus().setFirstStart( nexusConfiguration.isConfigurationDefaulted() );

            applicationStatusSource.getSystemStatus().setInstanceUpgraded( nexusConfiguration.isInstanceUpgraded() );

            applicationStatusSource.getSystemStatus().setConfigurationUpgraded(
                nexusConfiguration.isConfigurationUpgraded() );

            // creating default templates if needed
            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_RELEASE, applicationStatusSource
                .getSystemStatus().isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT, applicationStatusSource
                .getSystemStatus().isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_RELEASE, applicationStatusSource
                .getSystemStatus().isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_SNAPSHOT, applicationStatusSource
                .getSystemStatus().isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_VIRTUAL, applicationStatusSource
                .getSystemStatus().isInstanceUpgraded() );

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
                "Nexus Work Directory : " + nexusConfiguration.getWorkingDirectory().getAbsolutePath().toString() );

            getLogger().info(
                "Started Nexus (version " + getSystemStatus().getVersion() + " " + getSystemStatus().getEditionShort()
                    + ")" );

            nexusConfiguration.notifyProximityEventListeners( new NexusStartedEvent() );
        }
        catch ( IOException e )
        {
            applicationStatusSource.getSystemStatus().setState( SystemState.BROKEN_IO );

            applicationStatusSource.getSystemStatus().setConfigurationValidationResponse(
                nexusConfiguration.getConfigurationSource().getValidationResponse() );

            applicationStatusSource.getSystemStatus().setErrorCause( e );

            getLogger().error( "Could not start Nexus, bad IO exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }
        catch ( ConfigurationException e )
        {
            applicationStatusSource.getSystemStatus().setState( SystemState.BROKEN_CONFIGURATION );

            applicationStatusSource.getSystemStatus().setConfigurationValidationResponse(
                nexusConfiguration.getConfigurationSource().getValidationResponse() );

            applicationStatusSource.getSystemStatus().setErrorCause( e );

            getLogger().error( "Could not start Nexus, user configuration exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }
    }

    public void stopService()
        throws Exception
    {
        applicationStatusSource.getSystemStatus().setState( SystemState.STOPPING );

        addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Stopping Nexus (version " + getSystemStatus().getVersion()
            + " " + getSystemStatus().getEditionShort() + ")" );

        nexusConfiguration.notifyProximityEventListeners( new NexusStoppedEvent() );

        httpProxyService.stopService();

        nexusScheduler.stopService();

        security.stopService();

        try
        {
            indexerManager.shutdown( false );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error while stopping IndexerManager:", e );
        }

        feedRecorder.stopService();

        try
        {
            cacheManager.stopService();
        }
        catch ( IllegalStateException e )
        {
            getLogger().error( "Error while stopping CacheManager:", e );
        }

        nexusConfiguration.dropInternals();

        applicationStatusSource.getSystemStatus().setState( SystemState.STOPPED );

        getLogger().info(
            "Stopped Nexus (version " + getSystemStatus().getVersion() + " " + getSystemStatus().getEditionShort()
                + ")" );
    }

    private void synchronizeShadowsAtStartup()
    {
        Collection<CRepositoryShadow> shadows = listRepositoryShadows();

        if ( shadows == null )
        {
            return;
        }

        for ( CRepositoryShadow shadow : shadows )
        {
            // spawn tasks to do it
            if ( shadow.isSyncAtStartup() )
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

            CRepository hostedTemplate = new CRepository();

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_RELEASE );

            hostedTemplate.setName( "Default Release Hosted Repository Template" );

            hostedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );

            hostedTemplate.setArtifactMaxAge( -1 );

            hostedTemplate.setMetadataMaxAge( 1440 );

            hostedTemplate.setAllowWrite( true );

            hostedTemplate.setDownloadRemoteIndexes( false );

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default hosted snapshot repository template..." );

            CRepository hostedTemplate = new CRepository();

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT );

            hostedTemplate.setName( "Default Snapshot Hosted Repository Template" );

            hostedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );

            hostedTemplate.setArtifactMaxAge( 1440 );

            hostedTemplate.setMetadataMaxAge( 1440 );

            hostedTemplate.setAllowWrite( true );

            hostedTemplate.setDownloadRemoteIndexes( false );

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_RELEASE.equals( id ) )
        {
            getLogger().info( "Creating default proxied release repository template..." );

            CRepository proxiedTemplate = new CRepository();

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_RELEASE );

            proxiedTemplate.setName( "Default Release Proxy Repository Template" );

            proxiedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );

            proxiedTemplate.setArtifactMaxAge( -1 );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setDownloadRemoteIndexes( true );

            proxiedTemplate.setChecksumPolicy( CRepository.CHECKSUM_POLICY_WARN );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default proxied snapshot repository template..." );

            CRepository proxiedTemplate = new CRepository();

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_SNAPSHOT );

            proxiedTemplate.setName( "Default Snapshot Proxy Repository Template" );

            proxiedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setDownloadRemoteIndexes( true );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_VIRTUAL.equals( id ) )
        {
            getLogger().info( "Creating default virtual repository template..." );

            CRepositoryShadow shadowTemplate = new CRepositoryShadow();

            shadowTemplate.setId( TEMPLATE_DEFAULT_VIRTUAL );

            shadowTemplate.setName( "Default Virtual Repository Template" );

            createRepositoryShadowTemplate( shadowTemplate, shouldRecreate );
        }
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
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( key );

        response.setMimeType( "text/xml" );

        // TODO:
        response.setSize( 0 );

        response.setInputStream( nexusConfiguration.getConfigurationAsStreamByKey( key ) );

        return response;
    }

    public SimpleLog4jConfig getLogConfig()
        throws IOException
    {
        return logFileManager.getLogConfig();
    }

    public void setLogConfig( SimpleLog4jConfig config )
        throws IOException
    {
        logFileManager.setLogConfig( config );
    }

    // Mirrors
    public void setMirrors( String repositoryId, List<CMirror> mirrors )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        this.nexusConfiguration.setMirrors( repositoryId, mirrors );
    }

    public Collection<CMirror> listMirrors( String repositoryId )
        throws NoSuchRepositoryException
    {
        return this.nexusConfiguration.listMirrors( repositoryId );
    }
}
