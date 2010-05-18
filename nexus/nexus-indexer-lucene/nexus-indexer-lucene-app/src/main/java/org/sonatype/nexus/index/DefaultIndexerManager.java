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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.DocumentFilter;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.sonatype.nexus.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.packer.IndexPackingRequest.IndexFormat;
import org.sonatype.nexus.index.treeview.IndexTreeView;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;
import org.sonatype.nexus.index.updater.AbstractResourceFetcher;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdateResult;
import org.sonatype.nexus.index.updater.IndexUpdater;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * <p>
 * Indexer Manager. This is a thin layer above Nexus Indexer and simply manages indexingContext additions, updates and
 * removals. Every Nexus repository (except ShadowRepository, which are completely left out of indexing) has two
 * indexing context maintained: local and remote. In case of hosted/proxy repositories, the local context contains the
 * content/cache content and the remote context contains nothing/downloaded index (if remote index download happened and
 * remote peer is publishing index). In case of group reposes, the things are little different: their local context
 * contains the index of GroupRepository local storage, and remote context contains the merged indexes of it's member
 * repositories.
 * </p>
 * <p>
 * This indexer manager supports Maven2 repositories only (hosted/proxy/groups).
 * </p>
 * 
 * @author Tamas Cservenak
 */
@Component( role = IndexerManager.class )
public class DefaultIndexerManager
    implements IndexerManager
{
    /** Context id local suffix */
    public static final String CTX_LOCAL_SUFIX = "-local";

    /** Context id remote suffix */
    public static final String CTX_REMOTE_SUFIX = "-remote";

    private static final Map<String, ReadWriteLock> locks = new LinkedHashMap<String, ReadWriteLock>();

    @Requirement
    private Logger logger;

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

    @Requirement( role = IndexCreator.class, hints = { MinimalArtifactInfoIndexCreator.ID,
        MavenPluginArtifactInfoIndexCreator.ID, MavenArchetypeArtifactInfoIndexCreator.ID,
        JarFileContentsIndexCreator.ID } )
    private List<IndexCreator> indexCreators;

    @Requirement( hint = "maven2" )
    private ContentClass maven2;

    @Requirement
    private IndexArtifactFilter indexArtifactFilter;

    @Requirement
    private ArtifactContextProducer artifactContextProducer;

    @Requirement
    private MimeUtil mimeUtil;

    @Requirement
    private IndexTreeView indexTreeView;

    private File workingDirectory;

    private File tempDirectory;

    protected Logger getLogger()
    {
        return logger;
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
        // indexing is supported if:
        // repo has NO Shadow facet available (is not a shadow)
        // repo has facet MavenRepository available (is implementation tied)
        // repo had contentClass compatible with Maven2 contentClass
        return !repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class )
            && repository.getRepositoryKind().isFacetAvailable( MavenRepository.class )
            && repository.getRepositoryContentClass().isCompatible( maven2 );
    }

    protected void logSkippingRepositoryMessage( Repository repository )
    {
        boolean isSupported = isIndexingSupported( repository );
        boolean isIndexed = repository.isIndexable();

        if ( getLogger().isDebugEnabled() )
        {
            StringBuilder sb = new StringBuilder( "Indexing is " );

            if ( !isSupported )
            {
                sb.append( "not " );
            }

            sb.append( "supported on repository \"" + repository.getName() + "\" (ID=\"" + repository.getId() + "\")" );

            if ( isSupported )
            {
                sb.append( " and is set as " );

                if ( !isIndexed )
                {
                    sb.append( "not " );
                }

                sb.append( "indexed. " );
            }
            else
            {
                sb.append( ". " );
            }

            sb.append( "Skipping it." );

            getLogger().debug( sb.toString() );
        }
    }

    public void addRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        Lock lock = getLock( repository ).writeLock();
        lock.lock();

        try
        {
            if ( !isIndexingSupported( repository ) || !repository.isIndexable() )
            {
                logSkippingRepositoryMessage( repository );

                return;
            }

            IndexingContext ctxLocal = null;
            IndexingContext ctxRemote = null;

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
                        repoRoot, new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ), null,
                        null, indexCreators );
                ctxLocal.setSearchable( repository.isSearchable() );

                ctxRemote =
                    nexusIndexer.addIndexingContextForced( getRemoteContextId( repository.getId() ),
                        repository.getId(), repoRoot, new File( getWorkingDirectory(),
                            getRemoteContextId( repository.getId() ) ), null, null, indexCreators );
                ctxRemote.setSearchable( repository.isSearchable() );
            }
            else
            {
                repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );

                File repoRoot = getRepositoryLocalStorageAsFile( repository );

                // add context for repository
                ctxLocal =
                    nexusIndexer.addIndexingContextForced( getLocalContextId( repository.getId() ), repository.getId(),
                        repoRoot, new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ), null,
                        null, indexCreators );
                ctxLocal.setSearchable( repository.isSearchable() );

                ctxRemote =
                    nexusIndexer.addIndexingContextForced( getRemoteContextId( repository.getId() ),
                        repository.getId(), repoRoot, new File( getWorkingDirectory(),
                            getRemoteContextId( repository.getId() ) ), null, null, indexCreators );
                ctxRemote.setSearchable( repository.isSearchable() );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        Lock lock = getLock( repository ).writeLock();
        lock.lock();

        try
        {
            if ( !isIndexingSupported( repository ) )
            {
                logSkippingRepositoryMessage( repository );

                return;
            }

            IndexingContext localCtx = getRepositoryLocalIndexContext( repository );
            IndexingContext remoteCtx = getRepositoryRemoteIndexContext( repository );

            if ( localCtx != null )
            {
                nexusIndexer.removeIndexingContext( localCtx, deleteFiles );
            }

            if ( remoteCtx != null )
            {
                nexusIndexer.removeIndexingContext( remoteCtx, deleteFiles );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void updateRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        Lock lock = getLock( repository ).writeLock();
        lock.lock();

        try
        {
            // cannot do "!repository.isIndexable()" since we may be called to handle that config change (using events)!
            // the repo might be already non-indexable, but the context would still exist!
            if ( !isIndexingSupported( repository ) )
            {
                logSkippingRepositoryMessage( repository );

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

            // handle the isIndexed false->true transition, but also do this only if some specified properties changed
            if ( ctx != null
                && ( !ctx.getRepository().getAbsolutePath().equals( repoRoot.getAbsolutePath() ) || ctx.isSearchable() != repository.isSearchable() ) )
            {
                // recreate the context
                removeRepositoryIndexContext( repositoryId, false );
            }

            // we have to handle "transition" in configuration (indexable true->false)
            if ( repository.isIndexable() )
            {
                if ( ctx == null
                    || ( !ctx.getRepository().getAbsolutePath().equals( repoRoot.getAbsolutePath() ) || ctx.isSearchable() != repository.isSearchable() ) )
                {
                    // recreate the context
                    addRepositoryIndexContext( repositoryId );
                }

                // set include in search/indexable
                setRepositoryIndexContextSearchable( repositoryId, repository.isSearchable() );
            }
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

    protected IndexingContext getRepositoryLocalIndexContext( Repository repository )
    {
        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( repository.getId() + CTX_LOCAL_SUFIX );

        return ctx;
    }

    protected IndexingContext getRepositoryRemoteIndexContext( Repository repository )
    {
        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( repository.getId() + CTX_REMOTE_SUFIX );

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
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        // cannot do "!repository.isIndexable()" since we may be called to handle that config change (using events)!
        // the repo might be already non-indexable, but the context would still exist!
        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        IndexingContext ctx = getRepositoryLocalIndexContext( repository );

        IndexingContext rctx = getRepositoryRemoteIndexContext( repository );

        // do this only if we have contexts, otherwise be muted
        if ( ctx != null && rctx != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Searching on repository ID='" + repositoryId + "' is set to: " + String.valueOf( searchable ) );
            }

            ctx.setSearchable( searchable );

            rctx.setSearchable( searchable );
        }
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

    @Deprecated
    public NexusIndexer getNexusIndexer()
    {
        return nexusIndexer;
    }

    // ----------------------------------------------------------------------------
    // adding/removing on the fly
    // ----------------------------------------------------------------------------
    public void addItemToIndex( Repository repository, StorageItem item )
        throws IOException
    {
        addItemToIndex( repository, item, new ArrayList<Repository>(), null );
    }

    protected void addItemToIndex( Repository repository, StorageItem item, List<Repository> processedRepositories,
                                   ArtifactContext ac )
        throws IOException
    {
        // is indexing supported at all on this repository?
        // sadly, the nexus-indexer is maven2 only, hence we check is the repo
        // from where we get the event is a maven2 repo, is indexing supported at all
        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // do we have to maintain index context at all?
        if ( !repository.isIndexable() )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // check for cycles in recursion
        if ( processedRepositories.contains( repository ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Repository '" + repository.getId()
                        + "' is already processed in recursive calls, will not process it." );
            }

            return;
        }

        // is this hidden path?
        if ( item.getRepositoryItemUid().isHidden() )
        {
            getLogger().debug( "Will not index hidden file path: " + item.getPath() );

            return;
        }

        // do the work
        IndexingContext context = getRepositoryLocalIndexContext( repository );

        if ( context != null )
        {
            if ( ac == null )
            {
                // by calculating GAV we check wether the request is against a repo artifact at all
                Gav gav = null;

                try
                {
                    gav =
                        ( (MavenRepository) repository ).getGavCalculator().pathToGav(
                            item.getRepositoryItemUid().getPath() );
                }
                catch ( IllegalArtifactCoordinateException e )
                {
                    gav = null;
                }

                // signatures and hashes are not considered for processing
                // reason (NEXUS-814 related): the actual artifact and it's POM will (or already did)
                // emitted events about modifying them
                if ( gav == null || gav.isSignature() || gav.isHash() )
                {
                    // we do not index these
                    return;
                }

                // if we have a valid indexing context and have access to a File
                if ( DefaultFSLocalRepositoryStorage.class.isAssignableFrom( repository.getLocalStorage().getClass() ) )
                {
                    File file =
                        ( (DefaultFSLocalRepositoryStorage) repository.getLocalStorage() ).getFileFromBase( repository,
                            new ResourceStoreRequest( item ) );

                    if ( file.exists() )
                    {
                        try
                        {
                            ac = artifactContextProducer.getArtifactContext( context, file );
                        }
                        catch ( IllegalArtifactCoordinateException e )
                        {
                            // cannot create artifact context, forget it
                            return;
                        }

                        if ( ac != null )
                        {
                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug( "The ArtifactContext created from file is fine, continuing." );
                            }

                            ArtifactInfo ai = ac.getArtifactInfo();

                            if ( ai.sha1 == null )
                            {
                                // if repo has no sha1 checksum, odd nexus one
                                ai.sha1 = item.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
                            }
                        }
                    }
                }
            }

            // and finally: index it
            getNexusIndexer().addArtifactToIndex( ac, context );
        }

        // mark repository as "job done"
        processedRepositories.add( repository );

        // finally, propagate to group contexts where this repository is member
        List<GroupRepository> groupsOfRepository = repositoryRegistry.getGroupsOfRepository( repository );

        for ( GroupRepository group : groupsOfRepository )
        {
            try
            {
                addItemToIndex( group, item, processedRepositories, ac );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got IO exception during index processing, continuing to do the best...", e );
            }
        }
    }

    public void removeItemFromIndex( Repository repository, StorageItem item )
        throws IOException
    {
        // just call the recursive one with empty list
        removeItemFromIndex( repository, item, new ArrayList<Repository>(), null );
    }

    protected void removeItemFromIndex( Repository repository, StorageItem item,
                                        List<Repository> processedRepositories, ArtifactContext ac )
        throws IOException
    {
        // is indexing supported at all on this repository?
        // sadly, the nexus-indexer is maven2 only, hence we check is the repo
        // from where we get the event is a maven2 repo, is indexing supported at all
        if ( !isIndexingSupported( repository ) || !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // do we have to maintain index context at all?
        if ( !repository.isIndexable() )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // check for cycles in recursion
        if ( processedRepositories.contains( repository ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Repository '" + repository.getId()
                        + "' is already processed in recursive calls, will not process it." );
            }

            return;
        }

        // do the work
        IndexingContext context = getRepositoryLocalIndexContext( repository );

        if ( context != null )
        {
            if ( ac == null )
            {
                // by calculating GAV we check wether the request is against a repo artifact at all
                Gav gav = null;

                try
                {
                    gav =
                        ( (MavenRepository) repository ).getGavCalculator().pathToGav(
                            item.getRepositoryItemUid().getPath() );
                }
                catch ( IllegalArtifactCoordinateException e )
                {
                    gav = null;
                }

                // signatures and hashes are not considered for processing
                // reason (NEXUS-814 related): the actual artifact and it's POM will (or already did)
                // emitted events about modifying them
                if ( gav == null || gav.isSignature() || gav.isHash() )
                {
                    return;
                }

                ArtifactInfo ai =
                    new ArtifactInfo( context.getRepositoryId(), gav.getGroupId(), gav.getArtifactId(),
                        gav.getBaseVersion(), gav.getClassifier() );

                // store extension if classifier is not empty
                if ( !StringUtils.isEmpty( ai.classifier ) )
                {
                    ai.packaging = gav.getExtension();
                }

                try
                {
                    ac = new ArtifactContext( null, null, null, ai, gav );
                }
                catch ( IllegalArtifactCoordinateException e )
                {
                    // ac cannot be created, just forget it being indexed
                    return;
                }

                // remove file from index
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Deleting artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                            + " from index (DELETE)." );
                }
            }

            // NEXUS-814: we should not delete always
            if ( !item.getItemContext().containsKey( SnapshotRemover.MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV ) )
            {
                getNexusIndexer().deleteArtifactFromIndex( ac, context );
            }
            else
            {
                // do NOT remove file from index
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "NOT deleting artifact " + ac.getArtifactInfo().groupId + ":" + ac.getArtifactInfo().artifactId
                            + ":" + ac.getArtifactInfo().version
                            + " from index (DELETE), since it is a timestamped snapshot and more builds exists." );
                }
            }
        }

        // mark the repo as "job done"
        processedRepositories.add( repository );

        // finally, propagate to group contexts where this repository is member
        List<GroupRepository> groupsOfRepository = repositoryRegistry.getGroupsOfRepository( repository );

        for ( GroupRepository group : groupsOfRepository )
        {
            try
            {
                removeItemFromIndex( group, item, processedRepositories, ac );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got IO exception during index processing, continuing to do the best...", e );
            }
        }
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

        if ( repository.isIndexable() )
        {
            reindexRepository( repository, fullReindex );

            publishRepositoryIndex( repositoryId );
        }
    }

    public void reindexRepositoryGroup( String path, String repositoryGroupId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        GroupRepository groupRepo =
            repositoryRegistry.getRepositoryWithFacet( repositoryGroupId, GroupRepository.class );

        if ( groupRepo.isIndexable() )
        {
            List<Repository> group = groupRepo.getMemberRepositories();

            for ( Repository repository : group )
            {
                reindexRepository( repository, fullReindex );
            }

            publishRepositoryGroupIndex( repositoryGroupId );
        }
    }

    public void resetGroupIndex( String groupId )
        throws NoSuchRepositoryException, IOException
    {
        GroupRepository group = repositoryRegistry.getRepositoryWithFacet( groupId, GroupRepository.class );

        if ( !isIndexingSupported( group ) )
        {
            logSkippingRepositoryMessage( group );

            return;
        }

        if ( !group.isIndexable() )
        {
            logSkippingRepositoryMessage( group );

            return;
        }

        if ( isAlreadyBeingIndexed( groupId ) )
        {
            return;
        }

        Lock lock = getLock( groupId ).writeLock();
        lock.lock();
        try
        {
            getLogger().info( "Remerging group '" + groupId + "'" );

            List<Repository> repositoriesList = group.getMemberRepositories();

            IndexingContext localContext = getRepositoryLocalIndexContext( groupId );
            IndexingContext remoteContext = getRepositoryRemoteIndexContext( group );
            purgeCurrentIndex( localContext );

            // purge it, and below will be repopulated
            localContext.purge();
            remoteContext.purge();

            for ( Repository repository : repositoriesList )
            {
                getLogger().info( "Remerging '" + repository.getId() + "' to '" + groupId + "'" );
                mergeRepositoryGroupIndexWithMember( repository );
            }

            publishRepositoryGroupIndex( groupId );
        }
        finally
        {
            lock.unlock();
        }
    }

    protected void reindexRepository( Repository repository, boolean fullReindex )
        throws IOException
    {
        if ( !isIndexingSupported( repository )
            || repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return;
        }

        if ( !repository.isIndexable() )
        {
            return;
        }

        if ( isAlreadyBeingIndexed( repository ) )
        {
            return;
        }

        Lock lock = getLock( repository.getId() ).writeLock();
        lock.lock();

        try
        {
            IndexingContext context = getRepositoryLocalIndexContext( repository );

            if ( fullReindex )
            {
                purgeCurrentIndex( context );
            }

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
        }
    }

    private void purgeCurrentIndex( IndexingContext context )
        throws IOException
    {
        context.purge();

        File repoDir = context.getRepository();
        if ( repoDir != null && repoDir.isDirectory() )
        {
            File indexDir = new File( repoDir, ".index" );
            FileUtils.forceDelete( indexDir );
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
            if ( repository.isIndexable() )
            {
                if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() )
                    && downloadRepositoryIndex( repository ) )
                {
                    mergeRepositoryGroupIndexWithMember( repository );
                }
            }
        }
    }

    public void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        ProxyRepository repository = repositoryRegistry.getRepositoryWithFacet( repositoryId, ProxyRepository.class );

        if ( repository.isIndexable() && downloadRepositoryIndex( repository ) )
        {
            mergeRepositoryGroupIndexWithMember( repository );
        }
    }

    public void downloadRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        List<Repository> group =
            repositoryRegistry.getRepositoryWithFacet( repositoryGroupId, GroupRepository.class ).getMemberRepositories();

        for ( Repository repository : group )
        {
            if ( repository.isIndexable() && repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
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
        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return false;
        }

        // ensure this is a proxy repo, since download may happen with proxies only
        if ( !repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            return false;
        }

        if ( isAlreadyBeingIndexed( repository.getId() ) )
        {
            return false;
        }

        MavenProxyRepository mpr = repository.adaptToFacet( MavenProxyRepository.class );

        Lock lock = getLock( repository.getId() ).writeLock();
        lock.lock();

        try
        {

            // just keep the context 'out of service' while indexing, will be added at end
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
                            "Remote indexes unchanged (no update needed) for repository " + repository.getId() );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Cannot fetch remote index for repository " + repository.getId(), e );
                }
            }
            else
            {
                // make empty the remote context
                IndexingContext context = getRepositoryRemoteIndexContext( repository );
                context.purge();

                // XXX remove obsolete files, should remove all index fragments
                // deleteItem( repository, ctx, zipUid );
                // deleteItem( repository, ctx, chunkUid ) ;
            }

            return hasRemoteIndex;

        }
        finally
        {
            lock.unlock();
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

            updateRequest.setDocumentFilter( getFilterFor( mrepository.getRepositoryPolicy() ) );
        }

        updateRequest.setResourceFetcher( new AbstractResourceFetcher()
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

        IndexUpdateResult result = indexUpdater.fetchAndUpdateIndex( updateRequest );

        return result.getTimestamp() != null;
    }

    // TODO Toni Prior Snownexus, this was contained in RepositoryPolicy split to separate concerns (NEXUS-2872)
    private DocumentFilter getFilterFor( final RepositoryPolicy repositoryPolicy )
    {
        return new DocumentFilter()
        {
            public boolean accept( Document doc )
            {
                String uinfo = doc.get( ArtifactInfo.UINFO );

                if ( uinfo == null )
                {
                    return true;
                }

                String[] r = ArtifactInfo.FS_PATTERN.split( uinfo );
                if ( repositoryPolicy == RepositoryPolicy.SNAPSHOT )
                {
                    return VersionUtils.isSnapshot( r[2] );
                }
                else if ( repositoryPolicy == RepositoryPolicy.RELEASE )
                {
                    return !VersionUtils.isSnapshot( r[2] );
                }
                else
                {
                    return true;
                }
            }
        };
    }

    protected void mergeRepositoryGroupIndexWithMember( Repository repository )
        throws IOException
    {
        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        if ( !repository.isIndexable() )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        if ( isAlreadyBeingIndexed( repository ) )
        {
            return;
        }

        List<GroupRepository> groupsOfRepository = repositoryRegistry.getGroupsOfRepository( repository );

        Lock repoLock = getLock( repository.getId() ).readLock();
        repoLock.lock();
        try
        {
            for ( GroupRepository group : groupsOfRepository )
            {
                if ( !isIndexingSupported( group ) )
                {
                    logSkippingRepositoryMessage( group );

                    continue;
                }

                if ( !group.isIndexable() )
                {
                    logSkippingRepositoryMessage( group );

                    continue;
                }

                if ( isAlreadyBeingIndexed( group ) )
                {
                    continue;
                }

                String groupId = group.getId();
                getLogger().info(
                    "Cascading merge of group indexes for group \"" + groupId + "\", where repository \""
                        + repository.getId() + "\" is member." );

                // get the groups target ctx
                IndexingContext groupContext = getRepositoryLocalIndexContext( group );

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
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() )
                && !repository.getRepositoryKind().isFacetAvailable( GroupRepository.class )
                && repository.isIndexable() )
            {
                publishRepositoryIndex( repository );
            }
        }

        List<GroupRepository> groups = repositoryRegistry.getRepositoriesWithFacet( GroupRepository.class );

        for ( GroupRepository group : groups )
        {
            publishRepositoryIndex( group );
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

        if ( group.isIndexable() )
        {
            for ( Repository repository : group.getMemberRepositories() )
            {
                publishRepositoryIndex( repository );
            }

            publishRepositoryIndex( group );
        }
    }

    protected void publishRepositoryIndex( Repository repository )
        throws IOException
    {
        // is indexing supported at all?
        if ( !isIndexingSupported( repository ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Can't publish index on repository \"" + repository.getName() + "\" (ID=\"" + repository.getId()
                        + "\") since indexing is not supported on it!" );
            }

            return;
        }

        // shadows are not capable to publish indexes
        if ( !repository.isIndexable() )
        {
            return;
        }

        if ( isAlreadyBeingIndexed( repository.getId() ) )
        {
            return;
        }

        File targetDir = null;
        IndexingContext mergedContext = null;

        Lock lock = getLock( repository.getId() ).readLock();
        lock.lock();

        try
        {
            getLogger().info( "Publishing best index for repository " + repository.getId() );

            IndexingContext context = getRepositoryLocalIndexContext( repository );
            IndexingContext remoteContext = getRepositoryRemoteIndexContext( repository );
            mergedContext = mergeContexts( context, remoteContext );

            targetDir = new File( getTempDirectory(), "nx-index-" + Long.toHexString( System.nanoTime() ) );

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
            // not publishing legacy format anymore
            packReq.setFormats( Arrays.asList( IndexFormat.FORMAT_V1 ) );
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
            lock.unlock();

            if ( targetDir != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Cleanup of temp files..." );
                }

                FileUtils.deleteDirectory( targetDir );
            }

            if ( mergedContext != null )
            {
                mergedContext.close( true );

                FileUtils.forceDelete( mergedContext.getIndexDirectoryFile() );
            }
        }
    }

    private IndexingContext mergeContexts( IndexingContext... contexts )
        throws IOException
    {
        IndexingContext mergedContext = null;
        for ( IndexingContext context : contexts )
        {
            if ( mergedContext == null )
            {
                mergedContext = getTempContext( context );
            }

            mergedContext.merge( context.getIndexDirectory() );
        }

        return mergedContext;
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

            StorageFileItem item = (StorageFileItem) repository.retrieveItem( true, req );

            // Hack to make sure that group properties isn't retrieved from child repo
            if ( repository.getId().equals( item.getRepositoryId() ) )
            {
                is = item.getInputStream();

                // FileUtils.copyStreamToFile closes the stream!
                FileUtils.copyStreamToFile( new RawInputStreamFacade( is ), new File( tempDir,
                    IndexingContext.INDEX_FILE + ".properties" ) );
            }
        }
        catch ( Exception e )
        {
            getLogger().debug( "Unable to copy index properties file, continuing without it", e );
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
                new DefaultStorageFileItem( repository, path, true, true, new PreparedContentLocator( fis,
                    mimeUtil.getMimeType( file ) ) );

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

    @Deprecated
    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        return nexusIndexer.identify( type, checksum );
    }

    public ArtifactInfo identifyArtifact( Field field, String data )
        throws IOException
    {
        return nexusIndexer.identify( field, data );
    }

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    @Deprecated
    public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count,
                                                  Integer hitLimit )
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
                req.setCount( count );
            }

            if ( hitLimit != null )
            {
                req.setResultHitLimit( hitLimit );
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

    @Deprecated
    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count,
                                                       Integer hitLimit )
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
                req.setCount( count );
            }

            if ( hitLimit != null )
            {
                req.setResultHitLimit( hitLimit );
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

    @Deprecated
    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                                  String repositoryId, Integer from, Integer count, Integer hitLimit )
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
                req.setCount( count );
            }

            if ( hitLimit != null )
            {
                req.setResultHitLimit( hitLimit );
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

    @Deprecated
    protected void postprocessResults( Collection<ArtifactInfo> res )
    {
        for ( Iterator<ArtifactInfo> i = res.iterator(); i.hasNext(); )
        {
            ArtifactInfo ai = i.next();

            if ( indexArtifactFilter.filterArtifactInfo( ai ) )
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

    @Deprecated
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

    // == NG stuff

    protected Query createQuery( Field field, String term, SearchType type )
    {
        return nexusIndexer.constructQuery( field, term, type );
    }

    protected IteratorSearchRequest createRequest( Query bq, Integer from, Integer count, Integer hitLimit,
                                                   boolean uniqueRGA )
    {
        return createRequest( bq, from, count, hitLimit, uniqueRGA, null );
    }

    protected IteratorSearchRequest createRequest( Query bq, Integer from, Integer count, Integer hitLimit,
                                                   boolean uniqueRGA, List<ArtifactInfoFilter> extraFilters )
    {
        IteratorSearchRequest req = new IteratorSearchRequest( bq );

        List<ArtifactInfoFilter> filters = new ArrayList<ArtifactInfoFilter>();

        // security filter
        filters.add( new ArtifactInfoFilter()
        {
            public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
            {
                return indexArtifactFilter.filterArtifactInfo( ai );
            }
        } );

        if ( uniqueRGA )
        {
            filters.add( new UniqueGAArtifactFilterPostprocessor( false ) );
        }

        if ( extraFilters != null && extraFilters.size() > 0 )
        {
            filters.addAll( extraFilters );
        }

        req.setArtifactInfoFilter( new AndMultiArtifactInfoFilter( filters ) );

        req.setArtifactInfoPostprocessor( new ArtifactInfoPostprocessor()
        {
            public void postprocess( IndexingContext ctx, ArtifactInfo ai )
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

                ai.context = result;
            }
        } );

        if ( from != null )
        {
            req.setStart( from );
        }

        if ( count != null )
        {
            req.setCount( count );
        }
        else
        {
            // protect UI from break-down ;)
            req.setCount( 500 );
        }

        if ( hitLimit != null )
        {
            req.setResultHitLimit( hitLimit );
        }

        return req;
    }

    public IteratorSearchResponse searchArtifactIterator( String term, String repositoryId, Integer from,
                                                          Integer count, Integer hitLimit, boolean uniqueRGA,
                                                          SearchType searchType )
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

            Query q1 = createQuery( MAVEN.GROUP_ID, term, searchType );

            q1.setBoost( 2.0f );

            Query q2 = createQuery( MAVEN.ARTIFACT_ID, term, searchType );

            q2.setBoost( 2.0f );

            BooleanQuery bq = new BooleanQuery();

            bq.add( q1, BooleanClause.Occur.SHOULD );

            bq.add( q2, BooleanClause.Occur.SHOULD );

            // switch for "extended" keywords
            if ( false )
            {
                Query q3 = createQuery( MAVEN.VERSION, term, searchType );

                Query q4 = createQuery( MAVEN.CLASSIFIER, term, searchType );

                Query q5 = createQuery( MAVEN.NAME, term, searchType );

                Query q6 = createQuery( MAVEN.DESCRIPTION, term, searchType );

                bq.add( q3, BooleanClause.Occur.SHOULD );

                bq.add( q4, BooleanClause.Occur.SHOULD );

                bq.add( q5, BooleanClause.Occur.SHOULD );

                bq.add( q6, BooleanClause.Occur.SHOULD );
            }

            IteratorSearchRequest req = createRequest( bq, from, count, hitLimit, uniqueRGA );

            if ( repositoryId != null )
            {
                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            try
            {
                IteratorSearchResponse result = nexusIndexer.searchIterator( req );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new IteratorSearchResponse( req.getQuery(), -1, null );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return new IteratorSearchResponse( req.getQuery(), 0, null );
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

    public IteratorSearchResponse searchArtifactClassIterator( String term, String repositoryId, Integer from,
                                                               Integer count, Integer hitLimit, SearchType searchType )
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

            Query q = createQuery( MAVEN.CLASSNAMES, term, searchType );

            IteratorSearchRequest req = createRequest( q, from, count, hitLimit, false );

            if ( repositoryId != null )
            {
                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            try
            {
                IteratorSearchResponse result = nexusIndexer.searchIterator( req );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new IteratorSearchResponse( req.getQuery(), -1, null );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + q.toString() + "\"", e );

                return new IteratorSearchResponse( req.getQuery(), 0, null );
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

    public IteratorSearchResponse searchArtifactIterator( String gTerm, String aTerm, String vTerm, String pTerm,
                                                          String cTerm, String repositoryId, Integer from,
                                                          Integer count, Integer hitLimit, SearchType searchType )
        throws NoSuchRepositoryException
    {
        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return new IteratorSearchResponse( null, -1, null );
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
                bq.add( createQuery( MAVEN.GROUP_ID, gTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( aTerm != null )
            {
                bq.add( createQuery( MAVEN.ARTIFACT_ID, aTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( vTerm != null )
            {
                bq.add( createQuery( MAVEN.VERSION, vTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( pTerm != null )
            {
                bq.add( createQuery( MAVEN.PACKAGING, pTerm, searchType ), BooleanClause.Occur.MUST );
            }

            // we can do this, since we enforce (above) that one of GAV is not empty, so we already have queries added
            // to bq
            ArtifactInfoFilter npFilter = null;

            if ( cTerm != null )
            {
                if ( Field.NOT_PRESENT.equalsIgnoreCase( cTerm ) )
                {
                    // bq.add( createQuery( MAVEN.CLASSIFIER, Field.NOT_PRESENT, SearchType.KEYWORD ),
                    // BooleanClause.Occur.MUST_NOT );
                    // This above should work too! -- TODO: fixit!
                    npFilter = new ArtifactInfoFilter()
                    {
                        public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
                        {
                            return StringUtils.isBlank( ai.classifier );
                        }
                    };
                }
                else
                {
                    bq.add( createQuery( MAVEN.CLASSIFIER, cTerm, searchType ), BooleanClause.Occur.MUST );
                }
            }

            IteratorSearchRequest req = null;

            if ( npFilter != null )
            {
                req = createRequest( bq, from, count, hitLimit, false, Arrays.asList( npFilter ) );
            }
            else
            {
                req = createRequest( bq, from, count, hitLimit, false );
            }

            if ( repositoryId != null )
            {
                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            try
            {
                IteratorSearchResponse result = nexusIndexer.searchIterator( req );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new IteratorSearchResponse( req.getQuery(), -1, null );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return new IteratorSearchResponse( req.getQuery(), 0, null );
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

    public IteratorSearchResponse searchArtifactSha1ChecksumIterator( String sha1Checksum, String repositoryId,
                                                                      Integer from, Integer count, Integer hitLimit )
        throws NoSuchRepositoryException
    {
        if ( sha1Checksum == null || sha1Checksum.length() > 40 )
        {
            return new IteratorSearchResponse( null, -1, null );
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
            
            SearchType searchType = sha1Checksum.length() == 40 ? SearchType.EXACT : SearchType.SCORED;

            BooleanQuery bq = new BooleanQuery();

            if ( sha1Checksum != null )
            {
                bq.add( createQuery( MAVEN.SHA1, sha1Checksum, searchType ), BooleanClause.Occur.MUST );
            }

            IteratorSearchRequest req = createRequest( bq, from, count, hitLimit, false );

            if ( repositoryId != null )
            {
                req.getContexts().add( localContext );

                req.getContexts().add( remoteContext );
            }

            try
            {
                IteratorSearchResponse result = nexusIndexer.searchIterator( req );

                return result;
            }
            catch ( BooleanQuery.TooManyClauses e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Too many clauses exception caught:", e );
                }

                // XXX: a hack, I am sending too many results by setting the totalHits value to -1!
                return new IteratorSearchResponse( req.getQuery(), -1, null );
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return new IteratorSearchResponse( req.getQuery(), 0, null );
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

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    public Query constructQuery( String field, String query )
    {
        return nexusIndexer.constructQuery( field, query );
    }

    // ----------------------------------------------------------------------------
    // Tree nodes
    // ----------------------------------------------------------------------------

    public TreeNode listNodes( TreeNodeFactory factory, Repository repository, String path )
    {
        try
        {
            return indexTreeView.listNodes( factory, path );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            getLogger().error( "Error retrieving index nodes", e );
        }

        return null;
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

    // Lock management

    private ReadWriteLock getLock( String repositoryId )
    {
        if ( !locks.containsKey( repositoryId ) )
        {
            locks.put( repositoryId, new ReentrantReadWriteLock() );
        }
        return locks.get( repositoryId );
    }

    private ReadWriteLock getLock( Repository repository )
    {
        return getLock( repository.getId() );
    }

    private boolean isAlreadyBeingIndexed( String repositoryId )
    {
        Lock lock = getLock( repositoryId ).readLock();
        boolean locked = true;
        try
        {
            locked = lock.tryLock();
            // if I can't get a read lock means someone else has the write lock (index tasks do write lock)
            return !locked;
        }
        finally
        {
            if ( locked )
            {
                lock.unlock();
            }
        }
    }

    private boolean isAlreadyBeingIndexed( Repository repository )
    {
        return isAlreadyBeingIndexed( repository.getId() );
    }

}