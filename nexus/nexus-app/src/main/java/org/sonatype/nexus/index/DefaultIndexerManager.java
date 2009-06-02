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
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;
import org.sonatype.nexus.index.updater.ResourceFetcher;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.ResetGroupIndexTask;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

/**
 * Indexer Manager. This is a thin layer above Nexus Indexer and simply manages indexingContext additions, updates and
 * removals. Every Nexus repository (except ShadowRepository, which are completely left out of indexing) has two
 * indexing context maintained: local and remote. In case of hosted/proxy repositories, the local context contains the
 * content/cache content and the remote context contains nothing/downloaded index (if remote index downlad happened and
 * remote peer is publishing index). In case of group reposes, the things are little different: their local context
 * contains the index of GroupRepository local storage, and remote context contains the merged indexes of it's member
 * repositories.
 * 
 * @author Tamas Cservenak
 */
@Component( role = IndexerManager.class )
public class DefaultIndexerManager
    extends AbstractLogEnabled
    implements IndexerManager, EventListener, Initializable

{
    /** Context id local suffix */
    public static final String CTX_LOCAL_SUFIX = "-local";

    /** Context id remote suffix */
    public static final String CTX_REMOTE_SUFIX = "-remote";

    private static final Map<String, ReadWriteLock> locks = new LinkedHashMap<String, ReadWriteLock>();

    @Requirement
    private NexusIndexer nexusIndexer;

    @Requirement
    private IndexUpdater indexUpdater;

    @Requirement
    private IndexPacker indexPacker;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement( role = IndexCreator.class, hints = { "min", "jarContent" } )
    private List<IndexCreator> indexCreators;

    @Requirement( hint = "maven2" )
    private ContentClass maven2;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private IndexArtifactFilter indexArtifactFilter;

    private File workingDirectory;

    private File tempDirectory;

    private ReadWriteLock getLock( String repositoryId )
    {
        if ( !locks.containsKey( repositoryId ) )
        {
            locks.put( repositoryId, new ReentrantReadWriteLock() );
        }
        return locks.get( repositoryId );
    }

    protected File getWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            workingDirectory = nexusConfiguration.getWorkingDirectory( "indexer" );
        }
        return workingDirectory;
    }

    protected File getTempDirectory()
    {
        if ( tempDirectory == null )
        {
            tempDirectory = nexusConfiguration.getTemporaryDirectory();
        }
        return tempDirectory;
    }

    /**
     * Used to close all indexing context explicitly.
     */
    public void shutdown( boolean deleteFiles )
        throws IOException
    {
        getLogger().info( "Shutting down Nexus IndexerManager" );

        for ( IndexingContext ctx : nexusIndexer.getIndexingContexts().values() )
        {
            nexusIndexer.removeIndexingContext( ctx, false );
        }

        locks.clear();
    }

    public void resetConfiguration()
    {
        workingDirectory = null;

        tempDirectory = null;
    }

    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    protected boolean isIndexingSupported( Repository repository )
    {
        // we index only non-shadow maven2 reposes
        return !repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class )
            && repository.getRepositoryContentClass().isCompatible( maven2 );
    }

    public void addRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        IndexingContext ctxLocal = null;
        IndexingContext ctxRemote = null;

        if ( !isIndexingSupported( repository ) )
        {
            return;
        }

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            // group repository
            // just to throw NoSuchRepositoryGroupException if not existing
            repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

            File repoRoot = getRepositoryLocalStorageAsFile( repository );

            // add context for repository
            // context do not take part in "search all" ops, since they contain
            // the member reposes only, so it would duplicate results
            ctxLocal =
                nexusIndexer.addIndexingContextForced( getLocalContextId( repository.getId() ), repository.getId(),
                                                       repoRoot, new File( getWorkingDirectory(),
                                                                           getLocalContextId( repository.getId() ) ),
                                                       null, null, indexCreators );
            ctxLocal.setSearchable( false );

            ctxRemote =
                nexusIndexer.addIndexingContextForced( getRemoteContextId( repository.getId() ), repository.getId(),
                                                       repoRoot, new File( getWorkingDirectory(),
                                                                           getRemoteContextId( repository.getId() ) ),
                                                       null, null, indexCreators );
            ctxRemote.setSearchable( false );
        }
        else
        {
            repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );

            File repoRoot = getRepositoryLocalStorageAsFile( repository );

            // add context for repository
            ctxLocal =
                nexusIndexer.addIndexingContextForced( getLocalContextId( repository.getId() ), repository.getId(),
                                                       repoRoot, new File( getWorkingDirectory(),
                                                                           getLocalContextId( repository.getId() ) ),
                                                       null, null, indexCreators );
            ctxLocal.setSearchable( repository.isIndexable() );

            ctxRemote =
                nexusIndexer.addIndexingContextForced( getRemoteContextId( repository.getId() ), repository.getId(),
                                                       repoRoot, new File( getWorkingDirectory(),
                                                                           getRemoteContextId( repository.getId() ) ),
                                                       null, null, indexCreators );
            ctxRemote.setSearchable( repository.isIndexable() );
        }
    }

    public void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( !isIndexingSupported( repository ) )
        {
            return;
        }

        // remove context for repository
        nexusIndexer.removeIndexingContext( getRepositoryLocalIndexContext( repository ), deleteFiles );
        nexusIndexer.removeIndexingContext( getRepositoryRemoteIndexContext( repository ), deleteFiles );
    }

    public void updateRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( !isIndexingSupported( repository ) )
        {
            return;
        }

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            // group repository
            repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );
        }
        else
        {
            repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );
        }

        File repoRoot = getRepositoryLocalStorageAsFile( repository );

        // get context for repository, check is change needed
        IndexingContext ctx = getRepositoryLocalIndexContext( repository );

        if ( !ctx.getRepository().getAbsolutePath().equals( repoRoot.getAbsolutePath() ) )
        {
            // recreate the context
            removeRepositoryIndexContext( repositoryId, false );

            addRepositoryIndexContext( repositoryId );
        }

        // set include in search/indexable
        setRepositoryIndexContextSearchable( repositoryId, repository.isIndexable() );
    }

    public IndexingContext getRepositoryIndexContext( String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        return getRepositoryIndexContext( repository );
    }

    public IndexingContext getRepositoryIndexContext( Repository repository )
        throws IOException
    {
        String repoId = repository.getId();
        Lock lock = getLock( repoId ).readLock();
        lock.lock();
        try
        {
            IndexingContext localContext = getRepositoryLocalIndexContext( repository );
            IndexingContext remoteContext = getRepositoryLocalIndexContext( repository );

            IndexingContext mergedContext = getTempContext( localContext );
            mergedContext.merge( localContext.getIndexDirectory() );
            mergedContext.merge( remoteContext.getIndexDirectory() );
            return mergedContext;
        }
        finally
        {
            lock.unlock();
        }
    }

    public IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        return getRepositoryLocalIndexContext( repository );
    }

    public IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        return getRepositoryRemoteIndexContext( repository );
    }

    public IndexingContext getRepositoryLocalIndexContext( Repository repository )
    {
        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( repository.getId() + CTX_LOCAL_SUFIX );

        return ctx;
    }

    public IndexingContext getRepositoryRemoteIndexContext( Repository repository )
    {
        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( repository.getId() + CTX_REMOTE_SUFIX );

        return ctx;
    }

    public void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( !isIndexingSupported( repository ) )
        {
            return;
        }

        IndexingContext ctx = getRepositoryLocalIndexContext( repository );

        IndexingContext rctx = getRepositoryRemoteIndexContext( repository );

        if ( !ctx.isSearchable() && searchable )
        {
            // we have a !searchable -> searchable transition, reindex it
            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );

            rt.setRepositoryId( repositoryId );

            nexusScheduler.submit( "Searchable re-enabled", rt );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                               "Indexing on repository ID='" + repositoryId + "' is enabled: "
                                   + String.valueOf( searchable ) );
        }

        ctx.setSearchable( searchable );

        rctx.setSearchable( searchable );
    }

    /**
     * Extracts the repo root on local FS as File. It may return null!
     * 
     * @param repository
     * @return
     * @throws MalformedURLException
     */
    protected File getRepositoryLocalStorageAsFile( Repository repository )
    {
        File repoRoot = null;

        if ( repository.getLocalUrl() != null
            && repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage )
        {
            try
            {
                URL url = new URL( repository.getLocalUrl() );
                try
                {
                    repoRoot = new File( url.toURI() );
                }
                catch ( Throwable t )
                {
                    repoRoot = new File( url.getPath() );
                }
            }
            catch ( MalformedURLException e )
            {
                // Try just a regular file
                repoRoot = new File( repository.getLocalUrl() );
            }

        }

        return repoRoot;
    }

    // ----------------------------------------------------------------------------
    // Publish the used NexusIndexer
    // ----------------------------------------------------------------------------

    public NexusIndexer getNexusIndexer()
    {
        return nexusIndexer;
    }

    // ----------------------------------------------------------------------------
    // Reindexing related
    // ----------------------------------------------------------------------------

    public void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
            {
                reindexRepository( repository, fullReindex );
            }
        }

        publishAllIndex();
    }

    public void reindexRepository( String path, String repositoryId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        reindexRepository( repository, fullReindex );

        publishRepositoryIndex( repositoryId );
    }

    public void reindexRepositoryGroup( String path, String repositoryGroupId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        List<Repository> group =
            repositoryRegistry.getRepositoryWithFacet( repositoryGroupId, GroupRepository.class )
                .getMemberRepositories();

        for ( Repository repository : group )
        {
            reindexRepository( repository, fullReindex );
        }

        publishRepositoryGroupIndex( repositoryGroupId );
    }

    public void resetGroupIndex( String groupId )
        throws NoSuchRepositoryException, IOException
    {
        getLogger().info( "Remerging group '" + groupId + "'" );
        GroupRepository group = repositoryRegistry.getRepositoryWithFacet( groupId, GroupRepository.class );
        List<Repository> repositoriesList = group.getMemberRepositories();

        purgeCurrentIndex( group );

        // purge it, and below will be repopulated
        purgeRepositoryGroupIndex( group );

        for ( Repository repository : repositoriesList )
        {
            getLogger().info( "Remerging '" + repository.getId() + "' to '" + groupId + "'" );
            mergeRepositoryGroupIndexWithMember( repository );
        }

        publishRepositoryGroupIndex( groupId );
    }

    protected void reindexRepository( Repository repository, boolean fullReindex )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return;
        }

        boolean repositoryIndexable = repository.isIndexable();

        String repositoryId = repository.getId();
        Lock lock = getLock( repositoryId ).writeLock();
        lock.lock();

        try
        {
            repository.setIndexable( false );

            purgeCurrentIndex( repository );

            IndexingContext context = getRepositoryLocalIndexContext( repository );

            if ( fullReindex )
            {
                nexusIndexer.scan( context, false );
            }
            else
            {
                nexusIndexer.scan( context, true );
            }

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ) );
            }

            mergeRepositoryGroupIndexWithMember( repository );
        }
        finally
        {
            lock.unlock();
            repository.setIndexable( repositoryIndexable );
        }
    }

    private void purgeCurrentIndex( Repository repository )
        throws IOException
    {
        IndexingContext context = getRepositoryLocalIndexContext( repository );

        Lock lock = getLock( repository.getId() ).writeLock();
        lock.lock();
        try
        {
            File repoDir = context.getRepository();
            if ( repoDir != null && repoDir.isDirectory() )
            {
                File indexDir = new File( repoDir, ".index" );
                FileUtils.forceDelete( indexDir );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    protected void purgeRepositoryGroupIndex( GroupRepository group )
        throws IOException
    {
        Lock lock = getLock( group.getId() ).writeLock();
        lock.lock();

        IndexingContext context = getRepositoryRemoteIndexContext( group );
        IndexingContext localContext = getRepositoryLocalIndexContext( group );

        try
        {
            context.purge();
            localContext.purge();
        }
        finally
        {
            lock.unlock();
        }
    }

    // ----------------------------------------------------------------------------
    // Downloading remote indexes (will do remote-download, merge only)
    // ----------------------------------------------------------------------------

    public void downloadAllIndex()
        throws IOException
    {
        List<ProxyRepository> reposes = repositoryRegistry.getRepositoriesWithFacet( ProxyRepository.class );

        for ( ProxyRepository repository : reposes )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) && downloadRepositoryIndex( repository ) )
            {
                mergeRepositoryGroupIndexWithMember( repository );
            }
        }
    }

    public void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        ProxyRepository repository = repositoryRegistry.getRepositoryWithFacet( repositoryId, ProxyRepository.class );

        if ( downloadRepositoryIndex( repository ) )
        {
            mergeRepositoryGroupIndexWithMember( repository );
        }
    }

    public void downloadRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        List<Repository> group =
            repositoryRegistry.getRepositoryWithFacet( repositoryGroupId, GroupRepository.class )
                .getMemberRepositories();

        for ( Repository repository : group )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                if ( downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ) ) )
                {
                    mergeRepositoryGroupIndexWithMember( repository );
                }
            }
        }
    }

    protected boolean downloadRepositoryIndex( ProxyRepository repository )
        throws IOException
    {
        boolean repositoryIndexable = repository.isIndexable();

        try
        {
            repository.setIndexable( false );

            return updateIndexForRemoteRepository( repository );
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );
        }
    }

    protected boolean updateIndexForRemoteRepository( ProxyRepository repository )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            MavenProxyRepository mpr = repository.adaptToFacet( MavenProxyRepository.class );

            boolean shouldDownloadRemoteIndex = mpr.isDownloadRemoteIndexes();

            boolean hasRemoteIndex = false;

            if ( shouldDownloadRemoteIndex )
            {
                try
                {
                    getLogger().info( "Trying to get remote index for repository " + repository.getId() );

                    hasRemoteIndex = updateRemoteIndex( repository );

                    if ( hasRemoteIndex )
                    {
                        getLogger().info( "Remote indexes updated successfully for repository " + repository.getId() );
                    }
                    else
                    {
                        getLogger().info(
                                          "Remote indexes unchanged (no update needed) for repository "
                                              + repository.getId() );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Cannot fetch remote index for repository " + repository.getId(), e );
                }
            }
            else
            {
                Lock lock = getLock( repository.getId() ).writeLock();
                lock.lock();

                try
                {
                    // make empty the remote context
                    IndexingContext context = getRepositoryRemoteIndexContext( repository );

                    context.purge();
                }
                finally
                {
                    lock.unlock();
                }

                // XXX remove obsolete files, should remove all index fragments
                // deleteItem( repository, ctx, zipUid );
                // deleteItem( repository, ctx, chunkUid ) ;
            }

            return hasRemoteIndex;
        }
        else
        {
            return false;
        }
    }

    protected boolean updateRemoteIndex( final ProxyRepository repository )
        throws IOException, IllegalOperationException, ItemNotFoundException
    {
        // this will force remote check for newer files
        repository.expireCaches( new ResourceStoreRequest( "/.index" ) );

        IndexingContext context = getRepositoryRemoteIndexContext( repository );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );

        if ( repository instanceof MavenRepository )
        {
            MavenRepository mrepository = (MavenRepository) repository;

            updateRequest.setDocumentFilter( mrepository.getRepositoryPolicy().getFilter() );
        }

        updateRequest.setResourceFetcher( new ResourceFetcher()
        {
            public void connect( String id, String url )
                throws IOException
            {
            }

            public void disconnect()
                throws IOException
            {
            }

            // TODO is there a better way to fetch a file at given location?
            public void retrieve( String name, File targetFile )
                throws IOException
            {
                ResourceStoreRequest req = new ResourceStoreRequest( "/.index/" + name );

                OutputStream fos = null;
                InputStream is = null;

                try
                {
                    StorageFileItem item = null;

                    // XXX: ensure it goes to remote only and throws FileNotFoundException if nothing found on remote
                    // kinda turn off transparent proxying for this method
                    // We need to use ProxyRepository and get it's RemoteStorage stuff to completely
                    // avoid "transparent" proxying, and even the slightest possibility to return
                    // some stale file from cache to the updater.
                    if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                    {
                        ProxyRepository proxy = repository.adaptToFacet( ProxyRepository.class );

                        item =
                            (StorageFileItem) proxy.getRemoteStorage().retrieveItem( proxy, req, proxy.getRemoteUrl() );
                    }
                    else
                    {
                        throw new ItemNotFoundException( req, repository );
                    }

                    is = item.getInputStream();

                    fos = new FileOutputStream( targetFile );

                    IOUtil.copy( is, fos, 8192 );
                }
                catch ( RemoteAccessException ex )
                {
                    // XXX: But we should detect this? Maybe a permission problem?
                    throw new FileNotFoundException( name + " (" + ex.getMessage() + ")" );
                }
                catch ( ItemNotFoundException ex )
                {
                    throw new FileNotFoundException( name + " (item not found)" );
                }
                finally
                {
                    IOUtil.close( is );
                    IOUtil.close( fos );
                }
            }
        } );

        Date contextTimestamp = indexUpdater.fetchAndUpdateIndex( updateRequest );

        return contextTimestamp != null;
    }

    protected void mergeRepositoryGroupIndexWithMember( Repository repository )
        throws IOException
    {
        String repoId = repository.getId();
        List<GroupRepository> groupsOfRepository = repositoryRegistry.getGroupsOfRepository( repository );

        Lock repoLock = getLock( repoId ).readLock();
        repoLock.lock();
        try
        {
            for ( GroupRepository group : groupsOfRepository )
            {
                String groupId = group.getId();
                getLogger().info(
                                  "Cascading merge of group indexes for group '" + groupId + "', where repository '"
                                      + repoId + "' is member." );

                // get the groups target ctx
                IndexingContext groupContext = getRepositoryRemoteIndexContext( group );

                Lock groupLock = getLock( groupId ).writeLock();
                groupLock.lock();

                try
                {
                    // local index include all repositories
                    IndexingContext localContext = getRepositoryLocalIndexContext( repository );
                    IndexingContext remoteContext = getRepositoryRemoteIndexContext( repository );

                    groupContext.merge( localContext.getIndexDirectory() );
                    groupContext.merge( remoteContext.getIndexDirectory() );

                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Rebuilding groups in merged index for repository group " + group );
                    }

                    // rebuild group info
                    groupContext.rebuildGroups();

                    // committing changes
                    groupContext.getIndexWriter().flush();

                    groupContext.updateTimestamp();
                }
                finally
                {
                    groupLock.unlock();
                }
            }
        }
        finally
        {
            repoLock.unlock();
        }
    }

    // ----------------------------------------------------------------------------
    // Publishing index (will do publish only)
    // ----------------------------------------------------------------------------

    public void publishAllIndex()
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
            {
                publishRepositoryIndex( repository );
            }
        }

        List<GroupRepository> groups = repositoryRegistry.getRepositoriesWithFacet( GroupRepository.class );

        for ( GroupRepository group : groups )
        {
            publishRepositoryGroupIndex( group );
        }
    }

    public void publishRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        publishRepositoryIndex( repositoryRegistry.getRepository( repositoryId ) );
    }

    public void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        GroupRepository group = repositoryRegistry.getRepositoryWithFacet( repositoryGroupId, GroupRepository.class );

        for ( Repository repository : group.getMemberRepositories() )
        {
            publishRepositoryIndex( repository );
        }

        publishRepositoryGroupIndex( group );
    }

    protected void publishRepositoryIndex( Repository repository )
        throws IOException
    {
        // shadows are not capable to publish indexes
        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return;
        }

        boolean repositoryIndexable = repository.isIndexable();

        File targetDir = null;
        IndexingContext mergedContext = null;

        try
        {
            repository.setIndexable( false );

            getLogger().info( "Publishing best index for repository " + repository.getId() );

            // publish index update, publish the best context we have downstream
            mergedContext = getRepositoryIndexContext( repository );

            targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

            if ( !targetDir.mkdirs() )
            {
                throw new IOException( "Could not create temp dir for packing indexes: " + targetDir );
            }

            // copy the current properties file to the temp directory, this is what the indexer uses to
            // decide
            // if chunks are necessary, and what to label it as
            copyIndexPropertiesToTempDir( repository, targetDir );

            IndexPackingRequest packReq = new IndexPackingRequest( mergedContext, targetDir );

            packReq.setCreateIncrementalChunks( true );

            packReq.setUseTargetProperties( true );

            indexPacker.packIndex( packReq );

            File[] files = targetDir.listFiles();

            if ( files != null )
            {
                for ( File file : files )
                {
                    storeItem( repository, file, mergedContext );
                }
            }

        }
        finally
        {
            if ( targetDir != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Cleanup of temp files..." );
                }

                FileUtils.deleteDirectory( targetDir );
            }

            repository.setIndexable( repositoryIndexable );

            if ( mergedContext != null )
            {
                mergedContext.close( true );

                FileUtils.forceDelete( mergedContext.getIndexDirectoryFile() );
            }
        }
    }

    protected void publishRepositoryGroupIndex( GroupRepository groupRepository )
        throws IOException
    {
        String repoId = groupRepository.getId();
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Publishing merged index for repository group " + repoId );
        }

        // groups contains the merged context in -remote idx context
        IndexingContext context = getRepositoryRemoteIndexContext( groupRepository );

        File targetDir = null;

        Lock lock = getLock( repoId ).writeLock();
        lock.lock();
        try
        {
            targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

            if ( !targetDir.mkdirs() )
            {
                throw new IOException( "Could not create temp dir for packing indexes: " + targetDir );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Packing the merged index context." );
            }

            // copy the current properties file to the temp directory, this is what the indexer uses to decide
            // if chunks are necessary, and what to label it as
            copyIndexPropertiesToTempDir( groupRepository, targetDir );

            IndexPackingRequest packReq = new IndexPackingRequest( context, targetDir );

            packReq.setCreateIncrementalChunks( true );

            packReq.setUseTargetProperties( true );

            indexPacker.packIndex( packReq );

            File[] files = targetDir.listFiles();

            if ( files != null )
            {
                for ( File file : files )
                {
                    storeItem( groupRepository, file, context );
                }
            }
        }
        finally
        {
            lock.unlock();

            if ( targetDir != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Cleanup of temp files..." );
                }

                FileUtils.deleteDirectory( targetDir );
            }
        }
    }

    private void copyIndexPropertiesToTempDir( Repository repository, File tempDir )
    {
        InputStream is = null;
        try
        {
            // Need to use RepositoryUID to get around security
            ResourceStoreRequest req =
                new ResourceStoreRequest( "/.index/" + IndexingContext.INDEX_FILE + ".properties" );

            req.setRequestLocalOnly( true );

            StorageFileItem item = (StorageFileItem) repository.retrieveItem( false, req );

            // Hack to make sure that group properties isn't retrieved from child repo
            if ( repository.getId().equals( item.getRepositoryId() ) )
            {
                is = item.getInputStream();

                FileUtils.copyStreamToFile( new RawInputStreamFacade( is ), new File( tempDir,
                                                                                      IndexingContext.INDEX_FILE
                                                                                          + ".properties" ) );
            }
        }
        catch ( Exception e )
        {
            getLogger().debug( "Unable to copy index properties file, continuing without it", e );
        }
        finally
        {
            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    getLogger().debug( "Unable to close file handle!!", e );
                }
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    private void storeItem( Repository repository, File file, IndexingContext context )
    {
        String path = "/.index/" + file.getName();

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream( file );

            DefaultStorageFileItem fItem =
                new DefaultStorageFileItem( repository, path, true, true, new PreparedContentLocator( fis ) );

            if ( context.getTimestamp() == null )
            {
                fItem.setModified( 0 );

                fItem.setCreated( 0 );
            }
            else
            {
                fItem.setModified( context.getTimestamp().getTime() );

                fItem.setCreated( context.getTimestamp().getTime() );
            }

            if ( repository instanceof MavenRepository )
            {
                // this is maven repo, so use the checksumming facility
                ( (MavenRepository) repository ).storeItemWithChecksums( false, fItem );
            }
            else
            {
                // simply store it
                repository.storeItem( false, fItem );
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Cannot store index file " + path, e );
        }
        finally
        {
            IOUtil.close( fis );
        }
    }

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        return nexusIndexer.identify( type, checksum );
    }

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException
    {
        IndexingContext localContext = null;
        IndexingContext remoteContext = null;

        Lock lock = null;
        try
        {
            if ( repositoryId != null )
            {
                lock = getLock( repositoryId ).readLock();
                lock.lock();

                localContext = getRepositoryLocalIndexContext( repositoryId );
                remoteContext = getRepositoryRemoteIndexContext( repositoryId );
            }

            Query q1 = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, term );

            Query q2 = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, term );

            BooleanQuery bq = new BooleanQuery();

            bq.add( q1, BooleanClause.Occur.SHOULD );

            bq.add( q2, BooleanClause.Occur.SHOULD );

            FlatSearchRequest req = null;

            if ( repositoryId == null )
            {
                req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
            }
            else
            {
                req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );

                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            if ( from != null )
            {
                req.setStart( from );
            }

            if ( count != null )
            {
                req.setAiCount( count );
            }

            try
            {
                FlatSearchResponse result = nexusIndexer.searchFlat( req );

                postprocessResults( result.getResults() );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new FlatSearchResponse( req.getQuery(), -1, new HashSet<ArtifactInfo>() );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + term + "\"", e );

                return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
            }
        }
        finally
        {
            if ( lock != null )
            {
                lock.unlock();
            }
        }
    }

    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException
    {
        IndexingContext localContext = null;
        IndexingContext remoteContext = null;

        Lock lock = null;
        try
        {
            if ( repositoryId != null )
            {
                lock = getLock( repositoryId ).readLock();
                lock.lock();

                localContext = getRepositoryLocalIndexContext( repositoryId );
                remoteContext = getRepositoryRemoteIndexContext( repositoryId );
            }

            if ( term.endsWith( ".class" ) )
            {
                term = term.substring( 0, term.length() - 6 );
            }

            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, term );

            FlatSearchRequest req = null;

            if ( repositoryId == null )
            {
                req = new FlatSearchRequest( q, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
            }
            else
            {
                req = new FlatSearchRequest( q, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );

                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            if ( from != null )
            {
                req.setStart( from );
            }

            if ( count != null )
            {
                req.setAiCount( count );
            }

            try
            {
                FlatSearchResponse result = nexusIndexer.searchFlat( req );

                postprocessResults( result.getResults() );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new FlatSearchResponse( req.getQuery(), -1, new HashSet<ArtifactInfo>() );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + term + "\"", e );

                return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
            }
        }
        finally
        {
            if ( lock != null )
            {
                lock.unlock();
            }
        }
    }

    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                                  String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException
    {
        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return new FlatSearchResponse( null, -1, new HashSet<ArtifactInfo>() );
        }

        IndexingContext localContext = null;
        IndexingContext remoteContext = null;

        Lock lock = null;
        try
        {
            if ( repositoryId != null )
            {
                lock = getLock( repositoryId ).readLock();
                lock.lock();

                localContext = getRepositoryLocalIndexContext( repositoryId );
                remoteContext = getRepositoryRemoteIndexContext( repositoryId );
            }
            BooleanQuery bq = new BooleanQuery();

            if ( gTerm != null )
            {
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, gTerm ), BooleanClause.Occur.MUST );
            }

            if ( aTerm != null )
            {
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, aTerm ), BooleanClause.Occur.MUST );
            }

            if ( vTerm != null )
            {
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.VERSION, vTerm ), BooleanClause.Occur.MUST );
            }

            if ( pTerm != null )
            {
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.PACKAGING, pTerm ), BooleanClause.Occur.MUST );
            }

            if ( cTerm != null )
            {
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.CLASSIFIER, cTerm ), BooleanClause.Occur.MUST );
            }

            FlatSearchRequest req = null;

            if ( repositoryId == null )
            {
                req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
            }
            else
            {
                req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );

                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            if ( from != null )
            {
                req.setStart( from );
            }

            if ( count != null )
            {
                req.setAiCount( count );
            }

            try
            {
                FlatSearchResponse result = nexusIndexer.searchFlat( req );

                postprocessResults( result.getResults() );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new FlatSearchResponse( req.getQuery(), -1, new HashSet<ArtifactInfo>() );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
            }
        }
        finally
        {
            if ( lock != null )
            {
                lock.unlock();
            }
        }
    }

    protected void postprocessResults( Collection<ArtifactInfo> res )
    {
        for ( Iterator<ArtifactInfo> i = res.iterator(); i.hasNext(); )
        {
            ArtifactInfo ai = i.next();

            if ( this.indexArtifactFilter.filterArtifactInfo( ai ) )
            {
                ai.context = formatContextId( ai );
            }
            else
            {
                // remove the artifact, the user does not have access to it
                i.remove();
            }
        }

    }

    protected String formatContextId( ArtifactInfo ai )
    {
        String result = ai.context;

        try
        {
            Repository sourceRepository = repositoryRegistry.getRepository( ai.repository );

            if ( ai.context.endsWith( CTX_LOCAL_SUFIX ) )
            {
                if ( sourceRepository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                {
                    result = sourceRepository.getName() + " (Cache)";
                }
                else
                {
                    result = sourceRepository.getName() + " (Local)";
                }
            }
            else if ( ai.context.endsWith( CTX_REMOTE_SUFIX ) )
            {
                result = sourceRepository.getName() + " (Remote)";
            }

        }
        catch ( NoSuchRepositoryException e )
        {
            // nothing
        }

        return result;
    }

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    public Query constructQuery( String field, String query )
    {
        return nexusIndexer.constructQuery( field, query );
    }

    // ----------------------------------------------------------------------------
    // PRIVATE
    // ----------------------------------------------------------------------------

    protected String getLocalContextId( String repositoryId )
    {
        return repositoryId + CTX_LOCAL_SUFIX;
    }

    protected String getRemoteContextId( String repositoryId )
    {
        return repositoryId + CTX_REMOTE_SUFIX;
    }

    protected IndexingContext getTempContext( IndexingContext baseContext )
        throws IOException
    {
        File indexDir = baseContext.getIndexDirectoryFile();
        File dir = null;
        if ( indexDir != null )
        {
            dir = indexDir.getParentFile();
        }

        File tmpFile = File.createTempFile( baseContext.getId() + "-tmp", "", dir );
        File tmpDir = new File( tmpFile.getParentFile(), tmpFile.getName() + ".dir" );
        if ( !tmpDir.mkdirs() )
        {
            throw new IOException( "Cannot create temporary directory: " + tmpDir );
        }

        FileUtils.forceDelete( tmpFile );

        IndexingContext tmpContext = null;
        FSDirectory directory = FSDirectory.getDirectory( tmpDir );

        try
        {
            tmpContext = new DefaultIndexingContext( baseContext.getId() + "-tmp", //
                                                     baseContext.getRepositoryId(), //
                                                     baseContext.getRepository(), //
                                                     directory, //
                                                     baseContext.getRepositoryUrl(), //
                                                     baseContext.getIndexUpdateUrl(), //
                                                     baseContext.getIndexCreators(), //
                                                     true );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            getLogger().error( e.getMessage(), e );
            throw new IOException( e.getMessage() );
        }

        return tmpContext;
    }

    public void onEvent( Event<?> evt )
    {
        if ( evt instanceof RepositoryGroupMembersChangedEvent )
        {
            GroupRepository repo = ( (RepositoryGroupMembersChangedEvent) evt ).getGroupRepository();

            // Update the repo
            ResetGroupIndexTask rt = nexusScheduler.createTaskInstance( ResetGroupIndexTask.class );
            rt.setRepositoryGroupId( repo.getId() );
            nexusScheduler.submit( "Update group index.", rt );
        }
    }

    public void initialize()
    {
        applicationEventMulticaster.addEventListener( this );
    }

}
