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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;
import org.sonatype.nexus.index.updater.ResourceFetcher;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;

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
    implements IndexerManager

{
    /** Context id local suffix */
    public static final String CTX_LOCAL_SUFIX = "-local";

    /** Context id remote suffix */
    public static final String CTX_REMOTE_SUFIX = "-remote";

    /** Virgin date :) */
    private static final Date VIRGIN_CONTEXT_DATE = new Date( 1 );

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

    private File workingDirectory;

    private File tempDirectory;

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
    }

    public void resetConfiguration()
    {
        workingDirectory = null;

        tempDirectory = null;
    }

    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    public void addRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        IndexingContext ctxLocal = null;
        IndexingContext ctxRemote = null;

        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            // shadows are left out completely for now
            return;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            // group repository
            // just to throw NoSuchRepositoryGroupException if not existing
            repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

            File repoRoot = getRepositoryLocalStorageAsFile( repository );

            // add context for repository
            // context do not take part in "search all" ops, since they contain
            // the member reposes only, so it would duplicate results
            ctxLocal = nexusIndexer.addIndexingContextForced(
                getLocalContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ),
                null,
                null,
                NexusIndexer.FULL_INDEX );
            ctxLocal.setSearchable( false );

            ctxRemote = nexusIndexer.addIndexingContextForced(
                getRemoteContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                null,
                null,
                NexusIndexer.FULL_INDEX );
            ctxRemote.setSearchable( false );
        }
        else
        {
            repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );

            File repoRoot = getRepositoryLocalStorageAsFile( repository );

            // add context for repository
            ctxLocal = nexusIndexer.addIndexingContextForced(
                getLocalContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ),
                null,
                null,
                NexusIndexer.FULL_INDEX );
            ctxLocal.setSearchable( repository.isIndexable() );

            ctxRemote = nexusIndexer.addIndexingContextForced(
                getRemoteContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                null,
                null,
                NexusIndexer.FULL_INDEX );
            ctxRemote.setSearchable( repository.isIndexable() );
        }
    }

    public void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            // shadows are left out completely for now
            return;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            // group repository
            // just to throw NoSuchRepositoryGroupException if not existing
            repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );
        }
        else
        {
            repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );
        }

        // remove context for repository
        nexusIndexer.removeIndexingContext(
            nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) ),
            deleteFiles );

        nexusIndexer.removeIndexingContext(
            nexusIndexer.getIndexingContexts().get( getRemoteContextId( repositoryId ) ),
            deleteFiles );
    }

    public void updateRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            // shadows are left out completely for now
            return;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
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
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

        if ( !ctx.getRepository().getAbsolutePath().equals( repoRoot.getAbsolutePath() ) )
        {
            // recreate the context
            removeRepositoryIndexContext( repositoryId, false );

            addRepositoryIndexContext( repositoryId );
        }
    }

    public IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

        return ctx;
    }

    public IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repository.getId() ) );

        return ctx;
    }

    public IndexingContext getRepositoryBestIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        IndexingContext bestContext = getRepositoryLocalIndexContext( repositoryId );

        IndexingContext remoteContext = getRepositoryRemoteIndexContext( repositoryId );

        if ( remoteContext != null )
        {
            try
            {
                // if remote is here and is downloaded, it is the best (it is always the superset of local cache)
                if ( bestContext.getIndexReader().numDocs() < remoteContext.getIndexReader().numDocs() )
                {
                    bestContext = remoteContext;
                }
            }
            catch ( IOException e )
            {
                // silent
            }
        }

        return bestContext;
    }

    public void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException,
            NoSuchRepositoryException
    {
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );

        IndexingContext rctx = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repositoryId ) );

        if ( !ctx.isSearchable() && searchable )
        {
            // we have a !searchable -> searchable transition, reindex it
            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );

            rt.setRepositoryId( repositoryId );

            nexusScheduler.submit( "Searchable re-enabled", rt );
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
    // Publishing index
    // ----------------------------------------------------------------------------

    public void publishAllIndex()
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            publishRepositoryIndex( repository );
        }

        List<GroupRepository> groups = repositoryRegistry.getRepositoriesWithFacet( GroupRepository.class );

        for ( GroupRepository group : groups )
        {
            publishRepositoryGroupIndex( group );
        }
    }

    public void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        publishRepositoryIndex( repositoryRegistry.getRepository( repositoryId ) );
    }

    public void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryException
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

        try
        {
            repository.setIndexable( false );

            getLogger().info( "Publishing best index for repository " + repository.getId() );

            // publish index update, publish the best context we have downstream
            IndexingContext context = null;

            try
            {
                context = getRepositoryBestIndexContext( repository.getId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                // will not happen, but...
            }

            File targetDir = null;

            try
            {
                targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

                if ( !targetDir.mkdirs() )
                {
                    getLogger().error( "Could not create temp dir for packing indexes: " + targetDir );

                    throw new IOException( "Could not create temp dir for packing indexes: " + targetDir );
                }
                else
                {
                    // XXX: a hack follows, remove it when fixed!
                    boolean hacked = context.getTimestamp() == null;

                    if ( hacked )
                    {
                        context.updateTimestamp( false, VIRGIN_CONTEXT_DATE );
                    }
                    // XXX: end of the hack

                    IndexPackingRequest packReq = new IndexPackingRequest( context, targetDir );

                    packReq.setCreateIncrementalChunks( false );

                    indexPacker.packIndex( packReq );

                    // XXX: a hack follows, remove it when fixed!
                    if ( hacked )
                    {
                        context.updateTimestamp( false, null );
                    }
                    // XXX: end of the hack

                    File[] files = targetDir.listFiles();

                    if ( files != null )
                    {
                        for ( File file : files )
                        {
                            storeItem( repository, file, context );
                        }
                    }
                }
            }
            finally
            {
                if ( targetDir != null )
                {
                    FileUtils.deleteDirectory( targetDir );
                }
            }
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );
        }
    }

    private void storeItem( Repository repository, File file, IndexingContext context )
    {
        String path = "/.index/" + file.getName();

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream( file );

            DefaultStorageFileItem fItem = new DefaultStorageFileItem(
                repository,
                path,
                true,
                true,
                new PreparedContentLocator( fis ) );

            fItem.setModified( context.getTimestamp().getTime() );
            fItem.setCreated( context.getTimestamp().getTime() );

            if ( repository instanceof MavenRepository )
            {
                // this is maven repo, so use the checksumming facility
                ( (MavenRepository) repository ).storeItemWithChecksums( fItem );
            }
            else
            {
                // simply store it
                repository.storeItem( fItem );
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

    protected void publishRepositoryGroupIndex( GroupRepository groupRepository )
        throws IOException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Publishing merged index for repository group " + groupRepository.getId() );
        }

        IndexingContext context = nexusIndexer
            .getIndexingContexts().get( getRemoteContextId( groupRepository.getId() ) );

        File targetDir = null;

        try
        {
            targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

            if ( targetDir.mkdirs() )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Packing the merged index context." );
                }

                // XXX: a hack follows, remove it when fixed!
                boolean hacked = context.getTimestamp() == null;

                if ( hacked )
                {
                    context.updateTimestamp( false, VIRGIN_CONTEXT_DATE );
                }
                // XXX: end of the hack

                IndexPackingRequest packReq = new IndexPackingRequest( context, targetDir );

                packReq.setCreateIncrementalChunks( false );

                indexPacker.packIndex( packReq );

                // XXX: a hack follows, remove it when fixed!
                if ( hacked )
                {
                    context.updateTimestamp( false, null );
                }
                // XXX: end of the hack

                File[] files = targetDir.listFiles();

                if ( files != null )
                {
                    for ( File file : files )
                    {
                        storeItem( groupRepository, file, context );
                    }
                }
            }
            else
            {
                getLogger().warn( "Could not create temp dir for packing indexes: " + targetDir );
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
        }
    }

    // ----------------------------------------------------------------------------
    // Reindexing related
    // ----------------------------------------------------------------------------

    public void reindexAllRepositories( String path )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        // purge all group idxes, below will get all repopulated
        for ( GroupRepository groupRepo : repositoryRegistry.getRepositoriesWithFacet( GroupRepository.class ) )
        {
            purgeRepositoryGroupIndex( groupRepo.getId() );
        }

        for ( Repository repository : reposes )
        {
            reindexRepository( repository );
        }

        publishAllIndex();
    }

    public void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        reindexRepository( repository );

        publishRepositoryIndex( repositoryId );
    }

    public void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException
    {
        List<Repository> group = repositoryRegistry
            .getRepositoryWithFacet( repositoryGroupId, GroupRepository.class ).getMemberRepositories();

        // purge it, and below will be repopulated
        purgeRepositoryGroupIndex( repositoryGroupId );

        for ( Repository repository : group )
        {
            reindexRepository( repository );
        }

        publishRepositoryGroupIndex( repositoryGroupId );
    }

    protected void reindexRepository( Repository repository )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return;
        }

        boolean repositoryIndexable = repository.isIndexable();

        try
        {
            repository.setIndexable( false );

            IndexingContext context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

            nexusIndexer.scan( context );

            updateIndexForRemoteRepository( repository );

            mergeRepositoryGroupIndexWithMember( repository );
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );
        }
    }

    protected void mergeRepositoryGroupIndexWithMember( Repository repository )
        throws IOException
    {
        List<String> groupsOfRepository = repositoryRegistry.getGroupsOfRepository( repository.getId() );

        for ( String repositoryGroupId : groupsOfRepository )
        {
            getLogger().info(
                "Cascading merge of group indexes for group '" + repositoryGroupId + "', where repository '"
                    + repository.getId() + "' is member." );

            IndexingContext context = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repositoryGroupId ) );

            synchronized ( context )
            {
                // local index include all repositories
                try
                {
                    IndexingContext bestContext = getRepositoryBestIndexContext( repository.getId() );

                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            " ...found best context " + bestContext.getId() + " for repository "
                                + bestContext.getRepositoryId() + ", merging it..." );
                    }

                    context.merge( bestContext.getIndexDirectory() );
                }
                catch ( NoSuchRepositoryException e )
                {
                    // not to happen, we are iterating over them
                }

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Rebuilding groups in merged index for repository group " + repositoryGroupId );
                }

                // rebuild group info
                nexusIndexer.rebuildGroups( context );

                // committing changes
                context.getIndexWriter().flush();

                context.updateTimestamp();
            }
        }
    }

    protected void purgeRepositoryGroupIndex( String repositoryGroupId )
        throws IOException
    {
        IndexingContext context = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repositoryGroupId ) );

        if ( context != null )
        {
            context.purge();
        }
    }

    private boolean updateIndexForRemoteRepository( Repository repository )
        throws IOException
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            boolean shouldDownloadRemoteIndex = false;

            ProxyRepository proxy = repository.adaptToFacet( ProxyRepository.class );

            try
            {
                CRepository repoModel = nexusConfiguration.readRepository( proxy.getId() );

                shouldDownloadRemoteIndex = repoModel.isDownloadRemoteIndexes();
            }
            catch ( NoSuchRepositoryException e )
            {
                // TODO: heee?
            }

            boolean hasRemoteIndex = false;

            if ( shouldDownloadRemoteIndex )
            {
                try
                {
                    getLogger().info( "Trying to get remote index for repository " + proxy.getId() );

                    hasRemoteIndex = updateRemoteIndex( proxy );

                    if ( hasRemoteIndex )
                    {
                        getLogger().info( "Remote indexes updated successfully for repository " + proxy.getId() );
                    }
                    else
                    {
                        getLogger()
                            .info( "Remote indexes unchanged (no update needed) for repository " + proxy.getId() );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Cannot fetch remote index:", e );
                }
            }
            else
            {
                // make empty the remote context
                IndexingContext context = nexusIndexer.getIndexingContexts().get( getRemoteContextId( proxy.getId() ) );

                context.purge();

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

    private boolean updateRemoteIndex( final ProxyRepository repository )
        throws IOException,
            IllegalOperationException,
            ItemNotFoundException
    {
        // this will force remote check for newer files
        repository.clearCaches( "/.index" );

        IndexingContext context = null;

        try
        {
            context = getRepositoryRemoteIndexContext( repository.getId() );
        }
        catch ( NoSuchRepositoryException e )
        {
            // will not happen
        }

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );

        if ( repository instanceof MavenRepository )
        {
            MavenRepository mrepository = (MavenRepository) repository;

            updateRequest.setDocumentFilter( mrepository.getRepositoryPolicy().getFilter() );
        }

        updateRequest.setResourceFetcher( new ResourceFetcher()
        {
            Map<String, Object> ctx = new HashMap<String, Object>();

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
                RepositoryItemUid uid = repository.createUid( //
                    "/.index/" + name );

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

                        item = (StorageFileItem) proxy.getRemoteStorage().retrieveItem(
                            proxy,
                            ctx,
                            proxy.getRemoteUrl(),
                            uid.getPath() );
                    }
                    else
                    {
                        throw new ItemNotFoundException( uid );
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
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryBestIndexContext( repositoryId );
        }

        Query q1 = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, term );

        Query q2 = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, term );

        BooleanQuery bq = new BooleanQuery();

        bq.add( q1, BooleanClause.Occur.SHOULD );

        bq.add( q2, BooleanClause.Occur.SHOULD );

        FlatSearchRequest req = null;

        if ( context == null )
        {
            req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        }
        else
        {
            req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR, context );
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

    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException
    {
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryBestIndexContext( repositoryId );
        }

        if ( term.endsWith( ".class" ) )
        {
            term = term.substring( 0, term.length() - 6 );
        }

        Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, term );

        FlatSearchRequest req = null;

        if ( context == null )
        {
            req = new FlatSearchRequest( q, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        }
        else
        {
            req = new FlatSearchRequest( q, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR, context );
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

    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException
    {
        IndexingContext context = null;

        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return new FlatSearchResponse( null, -1, new HashSet<ArtifactInfo>() );
        }

        if ( repositoryId != null )
        {
            context = getRepositoryBestIndexContext( repositoryId );
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

        if ( context == null )
        {
            req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        }
        else
        {
            req = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR, context );
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

    protected void postprocessResults( Collection<ArtifactInfo> res )
    {
        for ( Iterator<ArtifactInfo> i = res.iterator(); i.hasNext(); )
        {
            ArtifactInfo ai = i.next();

            ai.context = formatContextId( ai );
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
}
