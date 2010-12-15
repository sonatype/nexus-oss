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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.index.AndMultiArtifactInfoFilter;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.ArtifactInfoPostprocessor;
import org.apache.maven.index.Field;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.MatchHighlightMode;
import org.apache.maven.index.MatchHighlightRequest;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.apache.maven.index.artifact.VersionUtils;
import org.apache.maven.index.context.ContextMemberProvider;
import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.DocumentFilter;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.context.MergedIndexingContext;
import org.apache.maven.index.context.StaticContextMemberProvider;
import org.apache.maven.index.context.UnsupportedExistingLuceneIndexException;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.packer.IndexPackingRequest;
import org.apache.maven.index.packer.IndexPackingRequest.IndexFormat;
import org.apache.maven.index.treeview.IndexTreeView;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeNodeFactory;
import org.apache.maven.index.treeview.TreeViewRequest;
import org.apache.maven.index.updater.AbstractResourceFetcher;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenUidAttribute;
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
    /** The key used in working directory. */
    public static final String INDEXER_WORKING_DIRECTORY_KEY = "indexer";

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

    public DefaultIndexerManager()
    {
        // Note: this is needed and used in ITs only!
        // See org.sonatype.nexus.rt.boot.ITIndexerActivationEventInspector for details
        boolean mavenIndexerBlockingCommits = Boolean.getBoolean( "mavenIndexerBlockingCommits" );

        if ( mavenIndexerBlockingCommits )
        {
            DefaultIndexingContext.BLOCKING_COMMIT = true;
        }
        // This above is needed and used in ITs only!
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected File getWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            workingDirectory = nexusConfiguration.getWorkingDirectory( INDEXER_WORKING_DIRECTORY_KEY );
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

        Lock lock = getLock( repository.getId() ).writeLock();
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
                final GroupRepository groupRepository =
                    repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

                File repoRoot = getRepositoryLocalStorageAsFile( repository );

                ContextMemberProvider localCtxProvider = new ContextMemberProvider()
                {
                    @Override
                    public Collection<IndexingContext> getMembers()
                    {
                        List<Repository> members = groupRepository.getMemberRepositories();

                        ArrayList<IndexingContext> result = new ArrayList<IndexingContext>( members.size() );

                        for ( Repository member : members )
                        {
                            IndexingContext ctx = getRepositoryLocalIndexContext( member );
                            if ( ctx != null )
                            {
                                result.add( ctx );
                            }
                        }

                        return result;
                    }
                };

                ctxLocal =
                    nexusIndexer.addMergedIndexingContext( getLocalContextId( repository.getId() ), repository.getId(),
                        repoRoot, repository.isSearchable(), localCtxProvider );

                ContextMemberProvider remoteCtxProvider = new ContextMemberProvider()
                {
                    @Override
                    public Collection<IndexingContext> getMembers()
                    {
                        List<Repository> members = groupRepository.getMemberRepositories();

                        ArrayList<IndexingContext> result = new ArrayList<IndexingContext>( members.size() );

                        for ( Repository member : members )
                        {
                            IndexingContext ctx = getRepositoryRemoteIndexContext( member );
                            if ( ctx != null )
                            {
                                result.add( ctx );
                            }
                        }

                        return result;
                    }
                };

                ctxRemote =
                    nexusIndexer.addMergedIndexingContext( getRemoteContextId( repository.getId() ),
                        repository.getId(), repoRoot, repository.isSearchable(), remoteCtxProvider );
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
                        repository.getId(), repoRoot,
                        new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ), null, null,
                        indexCreators );
                ctxRemote.setSearchable( repository.isSearchable() );

                // this handles all legacy cases, when group used -remote context to hold merged data!
                // They still sit in there, with OLD data.
                // Since 1.6, groups are consistent, and their -local contexts holds the data (since all reposes are
                // equal)
                // -remote is used by proxy repositories only!
                if ( !repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                {
                    ctxRemote.purge();
                }
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

        Lock lock = getLock( repository.getId() ).writeLock();
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

        Lock lock = getLock( repository.getId() ).writeLock();
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
        return getRepositoryBestIndexContext( repositoryRegistry.getRepository( repositoryId ) );
    }

    public IndexingContext getRepositoryBestIndexContext( Repository repository )
    {
        IndexingContext bestContext = getRepositoryLocalIndexContext( repository );

        IndexingContext remoteContext = getRepositoryRemoteIndexContext( repository );

        if ( remoteContext != null )
        {
            try
            {
                // if remote is here and is downloaded, it is the best (it is always the superset of local cache)
                if ( bestContext.getSize() < remoteContext.getSize() )
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

        // is this hidden path?
        if ( item.getRepositoryItemUid().getBooleanAttributeValue( IsHiddenUidAttribute.class ) )
        {
            getLogger().debug( "Will not index hidden file path: " + item.getPath() );

            return;
        }

        // do the work
        IndexingContext context = getRepositoryLocalIndexContext( repository );

        if ( context != null )
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

            item.getRepositoryItemUid().lock( Action.read );

            try
            {

                ArtifactContext ac = null;

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

                // and finally: index it
                getNexusIndexer().addArtifactToIndex( ac, context );
            }
            finally
            {
                item.getRepositoryItemUid().unlock();
            }
        }
    }

    public void removeItemFromIndex( Repository repository, StorageItem item )
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

        // do the work
        IndexingContext context = getRepositoryLocalIndexContext( repository );

        if ( context != null )
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

            ArtifactContext ac = null;

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

            // NEXUS-814: we should not delete always
            if ( !item.getItemContext().containsKey( SnapshotRemover.MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV ) )
            {
                item.getRepositoryItemUid().lock( Action.read );

                try
                {
                    getNexusIndexer().deleteArtifactFromIndex( ac, context );
                }
                finally
                {
                    item.getRepositoryItemUid().unlock();
                }
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
    }

    // ----------------------------------------------------------------------------
    // Reindexing related
    // ----------------------------------------------------------------------------

    public void reindexAllRepositories( final String path, final boolean fullReindex )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
            {
                reindexRepository( repository, path, fullReindex );
            }
        }

        publishAllIndex();
    }

    public void reindexRepository( final String path, final String repositoryId, final boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            GroupRepository groupRepo = repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

            reindexRepositoryGroup( groupRepo, path, fullReindex );
        }
        else
        {
            if ( repository.isIndexable() )
            {
                reindexRepository( repository, path, fullReindex );

                publishRepositoryIndex( repositoryId );
            }
        }
    }

    private void reindexRepositoryGroup( final GroupRepository groupRepo, final String path, final boolean fullReindex )
        throws IOException, NoSuchRepositoryException
    {
        if ( groupRepo.isIndexable() )
        {
            List<Repository> group = groupRepo.getMemberRepositories();

            for ( Repository repository : group )
            {
                if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
                {
                    reindexRepositoryGroup( repository.adaptToFacet( GroupRepository.class ), path, fullReindex );
                }
                else
                {
                    reindexRepository( repository, path, fullReindex );
                }
            }

            publishRepositoryGroupIndex( groupRepo );
        }
    }

    protected void reindexRepository( final Repository repository, final String fromPath, final boolean fullReindex )
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

        if ( isAlreadyBeingIndexed( repository.getId() ) )
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

            nexusIndexer.scan( context, fromPath, null, !fullReindex );

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ) );
            }

            {
                // just optimize remote index, whatever happened above
                // (with hosted repositories, this will lessen file handles)
                // (with just updated proxy repositories will do nothing, since they will be already optimized)
                IndexingContext remoteContext = getRepositoryRemoteIndexContext( repository );

                remoteContext.optimize();
            }
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
                if ( LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
                {
                    downloadRepositoryIndex( repository );
                }
            }
        }
    }

    public void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            GroupRepository group = repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

            downloadRepositoryGroupIndex( group );
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) && repository.isIndexable() )
        {
            downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ) );
        }
    }

    protected void downloadRepositoryGroupIndex( GroupRepository group )
        throws IOException
    {
        List<Repository> members = group.getMemberRepositories();

        for ( Repository repository : members )
        {
            if ( !repository.isIndexable() )
            {
                continue;
            }

            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                downloadRepositoryGroupIndex( repository.adaptToFacet( GroupRepository.class ) );
            }

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ) );
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

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context, new AbstractResourceFetcher()
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
                    FileNotFoundException fne = new FileNotFoundException( name + " (item not found)" );
                    fne.initCause( ex );
                    throw fne;
                }
                finally
                {
                    IOUtil.close( is );
                    IOUtil.close( fos );
                }
            }
        } );

        if ( repository instanceof MavenRepository )
        {
            MavenRepository mrepository = (MavenRepository) repository;

            updateRequest.setDocumentFilter( getFilterFor( mrepository.getRepositoryPolicy() ) );
        }

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
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            GroupRepository group = repositoryRegistry.getRepositoryWithFacet( repositoryId, GroupRepository.class );

            publishRepositoryGroupIndex( group );
        }
        else
        {
            publishRepositoryIndex( repositoryRegistry.getRepository( repositoryId ) );
        }
    }

    protected void publishRepositoryGroupIndex( GroupRepository group )
        throws IOException
    {
        if ( group.isIndexable() )
        {
            for ( Repository repository : group.getMemberRepositories() )
            {
                if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
                {
                    publishRepositoryGroupIndex( repository.adaptToFacet( GroupRepository.class ) );
                }
                else
                {
                    publishRepositoryIndex( repository );
                }
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
            mergedContext =
                new MergedIndexingContext( repository.getId(), repository.getId(),
                    getRepositoryLocalStorageAsFile( repository ), false, new StaticContextMemberProvider(
                        Arrays.asList( new IndexingContext[] { context, remoteContext } ) ) );

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

            Exception lastException = null;

            if ( mergedContext != null )
            {
                try
                {
                    mergedContext.close( true );
                }
                catch ( Exception e )
                {
                    lastException = e;

                    getLogger().warn( "Could not close temporary indexing context!", e );
                }

                try
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Cleanup of temporary indexing context..." );
                    }

                    FileUtils.forceDelete( mergedContext.getIndexDirectoryFile() );
                }
                catch ( Exception e )
                {
                    lastException = e;

                    getLogger().debug( "Cleanup of temporary indexing context FAILED...", e );
                }
            }

            if ( targetDir != null )
            {
                try
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Cleanup of temp files..." );
                    }

                    FileUtils.deleteDirectory( targetDir );
                }
                catch ( IOException e )
                {
                    lastException = e;

                    getLogger().warn( "Cleanup of temp files FAILED...", e );
                }
            }

            if ( lastException != null )
            {
                // TODO: for god's sake, use Java6!
                IOException eek = new IOException( lastException.getMessage() );

                eek.initCause( lastException );

                throw eek;
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
    // Optimize
    // ----------------------------------------------------------------------------

    public void optimizeAllRepositoriesIndex()
        throws IOException
    {
        List<Repository> repos = repositoryRegistry.getRepositories();

        for ( Repository repository : repos )
        {
            optimizeIndex( repository );
        }
    }

    public void optimizeRepositoryIndex( String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        optimizeIndex( repository );
    }

    protected void optimizeIndex( Repository repo )
        throws CorruptIndexException, IOException
    {
        if ( repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            GroupRepository group = repo.adaptToFacet( GroupRepository.class );
            for ( Repository member : group.getMemberRepositories() )
            {
                optimizeIndex( member );
            }
        }

        // local
        IndexingContext context = getRepositoryLocalIndexContext( repo );
        if ( context != null )
        {
            getLogger().debug( "Optimizing local index context for repository: " + repo.getId() );
            context.optimize();
        }

        // remote
        context = getRepositoryRemoteIndexContext( repo );
        if ( context != null )
        {
            getLogger().debug( "Optimizing remote index context for repository: " + repo.getId() );
            context.optimize();
        }
    }

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    public Collection<ArtifactInfo> identifyArtifact( Field field, String data )
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

            Query q1 = nexusIndexer.constructQuery( MAVEN.GROUP_ID, term, SearchType.SCORED );

            Query q2 = nexusIndexer.constructQuery( MAVEN.ARTIFACT_ID, term, SearchType.SCORED );

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

            Query q = nexusIndexer.constructQuery( MAVEN.CLASSNAMES, term, SearchType.SCORED );

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
                bq.add( constructQuery( MAVEN.GROUP_ID, gTerm, SearchType.SCORED ), BooleanClause.Occur.MUST );
            }

            if ( aTerm != null )
            {
                bq.add( constructQuery( MAVEN.ARTIFACT_ID, aTerm, SearchType.SCORED ), BooleanClause.Occur.MUST );
            }

            if ( vTerm != null )
            {
                bq.add( constructQuery( MAVEN.VERSION, vTerm, SearchType.SCORED ), BooleanClause.Occur.MUST );
            }

            if ( pTerm != null )
            {
                bq.add( constructQuery( MAVEN.PACKAGING, pTerm, SearchType.SCORED ), BooleanClause.Occur.MUST );
            }

            if ( cTerm != null )
            {
                bq.add( constructQuery( MAVEN.CLASSIFIER, cTerm, SearchType.SCORED ), BooleanClause.Occur.MUST );
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

        if ( extraFilters != null && extraFilters.size() > 0 )
        {
            filters.addAll( extraFilters );
        }

        req.setArtifactInfoFilter( new AndMultiArtifactInfoFilter( filters ) );

        if ( uniqueRGA )
        {
            req.setArtifactInfoPostprocessor( new ArtifactInfoPostprocessor()
            {
                public void postprocess( IndexingContext ctx, ArtifactInfo ai )
                {
                    ai.context = "Aggregated";
                    ai.repository = null;
                }
            } );
        }
        else
        {
            // we may do this only when !uniqueRGA, otherwise UniqueGAArtifactFilterPostprocessor nullifies
            // ai.repository and ai.context
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

        return req;
    }

    public IteratorSearchResponse searchQueryIterator( Query query, String repositoryId, Integer from, Integer count,
                                                       Integer hitLimit, boolean uniqueRGA,
                                                       List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        IteratorSearchRequest req = createRequest( query, from, count, hitLimit, uniqueRGA, filters );

        if ( repositoryId != null )
        {
            IndexingContext localContext = getRepositoryLocalIndexContext( repositoryId );
            IndexingContext remoteContext = getRepositoryRemoteIndexContext( repositoryId );

            if ( localContext != null )
            {
                req.getContexts().add( localContext );
            }

            if ( remoteContext != null )
            {
                req.getContexts().add( remoteContext );
            }
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
            return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
        }
        catch ( IOException e )
        {
            getLogger().error( "Got I/O exception while searching for query \"" + query.toString() + "\"", e );

            return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
        }
    }

    public IteratorSearchResponse searchArtifactIterator( String term, String repositoryId, Integer from,
                                                          Integer count, Integer hitLimit, boolean uniqueRGA,
                                                          SearchType searchType, List<ArtifactInfoFilter> filters )
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

            Query q1 = constructQuery( MAVEN.GROUP_ID, term, searchType );

            q1.setBoost( 2.0f );

            Query q2 = constructQuery( MAVEN.ARTIFACT_ID, term, searchType );

            q2.setBoost( 2.0f );

            BooleanQuery bq = new BooleanQuery();

            bq.add( q1, BooleanClause.Occur.SHOULD );

            bq.add( q2, BooleanClause.Occur.SHOULD );

            // switch for "extended" keywords
            // if ( false )
            // {
            // Query q3 = constructQuery( MAVEN.VERSION, term, searchType );
            //
            // Query q4 = constructQuery( MAVEN.CLASSIFIER, term, searchType );
            //
            // Query q5 = constructQuery( MAVEN.NAME, term, searchType );
            //
            // Query q6 = constructQuery( MAVEN.DESCRIPTION, term, searchType );
            //
            // bq.add( q3, BooleanClause.Occur.SHOULD );
            //
            // bq.add( q4, BooleanClause.Occur.SHOULD );
            //
            // bq.add( q5, BooleanClause.Occur.SHOULD );
            //
            // bq.add( q6, BooleanClause.Occur.SHOULD );
            // }

            IteratorSearchRequest req = createRequest( bq, from, count, hitLimit, uniqueRGA, filters );

            req.getMatchHighlightRequests().add(
                new MatchHighlightRequest( MAVEN.GROUP_ID, q1, MatchHighlightMode.HTML ) );
            req.getMatchHighlightRequests().add(
                new MatchHighlightRequest( MAVEN.ARTIFACT_ID, q2, MatchHighlightMode.HTML ) );

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
                return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
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
                                                               Integer count, Integer hitLimit, SearchType searchType,
                                                               List<ArtifactInfoFilter> filters )
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

            Query q = constructQuery( MAVEN.CLASSNAMES, term, searchType );

            IteratorSearchRequest req = createRequest( q, from, count, hitLimit, false, filters );

            req.getMatchHighlightRequests().add(
                new MatchHighlightRequest( MAVEN.CLASSNAMES, q, MatchHighlightMode.HTML ) );

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
                return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + q.toString() + "\"", e );

                return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
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
                                                          Integer count, Integer hitLimit, boolean uniqueRGA,
                                                          SearchType searchType, List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
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
                bq.add( constructQuery( MAVEN.GROUP_ID, gTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( aTerm != null )
            {
                bq.add( constructQuery( MAVEN.ARTIFACT_ID, aTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( vTerm != null )
            {
                bq.add( constructQuery( MAVEN.VERSION, vTerm, searchType ), BooleanClause.Occur.MUST );
            }

            if ( pTerm != null )
            {
                bq.add( constructQuery( MAVEN.PACKAGING, pTerm, searchType ), BooleanClause.Occur.MUST );
            }

            // we can do this, since we enforce (above) that one of GAV is not empty, so we already have queries added
            // to bq
            if ( cTerm != null )
            {
                if ( Field.NOT_PRESENT.equalsIgnoreCase( cTerm ) )
                {
                    // bq.add( createQuery( MAVEN.CLASSIFIER, Field.NOT_PRESENT, SearchType.KEYWORD ),
                    // BooleanClause.Occur.MUST_NOT );
                    // This above should work too! -- TODO: fixit!
                    filters.add( 0, new ArtifactInfoFilter()
                    {
                        public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
                        {
                            return StringUtils.isBlank( ai.classifier );
                        }
                    } );
                }
                else
                {
                    bq.add( constructQuery( MAVEN.CLASSIFIER, cTerm, searchType ), BooleanClause.Occur.MUST );
                }
            }

            IteratorSearchRequest req = createRequest( bq, from, count, hitLimit, uniqueRGA, filters );

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
                return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
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
                                                                      Integer from, Integer count, Integer hitLimit,
                                                                      List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        if ( sha1Checksum == null || sha1Checksum.length() > 40 )
        {
            return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
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
                bq.add( constructQuery( MAVEN.SHA1, sha1Checksum, searchType ), BooleanClause.Occur.MUST );
            }

            IteratorSearchRequest req = createRequest( bq, from, count, hitLimit, false, filters );

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
                return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
            }
            catch ( IOException e )
            {
                getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );

                return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
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

    public Query constructQuery( Field field, String query, SearchType type )
        throws IllegalArgumentException
    {
        return nexusIndexer.constructQuery( field, query, type );
    }

    // ----------------------------------------------------------------------------
    // Tree nodes
    // ----------------------------------------------------------------------------

    public TreeNode listNodes( final TreeNodeFactory factory, final String path, final String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        return listNodes( factory, path, null, null, repositoryId );
    }

    public TreeNode listNodes( final TreeNodeFactory factory, final String path, final Map<Field, String> hints,
                               final ArtifactInfoFilter artifactInfoFilter, final String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        IndexingContext ctx = getRepositoryBestIndexContext( repositoryId );

        TreeViewRequest request = new TreeViewRequest( factory, path, hints, artifactInfoFilter, ctx );

        return indexTreeView.listNodes( request );
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

    /**
     * Creates a temporary empty indexing context, based on passed in basecontext.
     * 
     * @param baseContext
     * @return
     * @throws IOException
     */
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

        FSDirectory directory = FSDirectory.open( tmpDir );

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

            IOException eek = new IOException( e.getMessage(), e );

            throw eek;
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
}