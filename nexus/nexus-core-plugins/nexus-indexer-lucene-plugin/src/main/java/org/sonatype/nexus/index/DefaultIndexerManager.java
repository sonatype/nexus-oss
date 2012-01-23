/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.maven.index.artifact.VersionUtils;
import org.apache.maven.index.context.ContextMemberProvider;
import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.DocumentFilter;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.context.UnsupportedExistingLuceneIndexException;
import org.apache.maven.index.expr.SearchExpression;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.packer.IndexPackingRequest;
import org.apache.maven.index.packer.IndexPackingRequest.IndexFormat;
import org.apache.maven.index.treeview.IndexTreeView;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeNodeFactory;
import org.apache.maven.index.treeview.TreeViewRequest;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskUtil;

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
    public static final String CTX_SUFIX = "-ctx";

    /** Path prefix where index publishing happens */
    public static final String PUBLISHING_PATH_PREFIX = "/.index";

    private static final Map<String, ReadWriteLock> locks = new HashMap<String, ReadWriteLock>();

    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

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

    @Requirement( hint = "maven2" )
    private ContentClass maven2;

    @Requirement( role = IndexCreator.class )
    private List<IndexCreator> indexCreators;

    @Requirement
    private IndexArtifactFilter indexArtifactFilter;

    @Requirement
    private ArtifactContextProducer artifactContextProducer;

    @Requirement
    private MimeSupport mimeSupport;

    @Requirement
    private IndexTreeView indexTreeView;

    private File workingDirectory;

    private File tempDirectory;

    public DefaultIndexerManager()
    {
        // Note: this is needed and used in ITs only!
        // See org.sonatype.nexus.rt.boot.ITIndexerActivationEventInspector for details
        if ( SystemPropertiesHelper.getBoolean( "mavenIndexerBlockingCommits", DefaultIndexingContext.BLOCKING_COMMIT ) )
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

            IndexingContext ctx = null;

            File indexDirectory = new File( getWorkingDirectory(), getContextId( repository.getId() ) );
            indexDirectory.mkdirs();

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
                            IndexingContext ctx = getRepositoryIndexContext( member );

                            if ( ctx != null )
                            {
                                result.add( ctx );
                            }
                        }

                        return result;
                    }
                };

                ctx =
                    nexusIndexer.addMergedIndexingContext( getContextId( repository.getId() ), repository.getId(),
                        repoRoot, indexDirectory, repository.isSearchable(), localCtxProvider );

                ctx.setSearchable( repository.isSearchable() );
            }
            else
            {
                repositoryRegistry.getRepositoryWithFacet( repositoryId, Repository.class );

                File repoRoot = getRepositoryLocalStorageAsFile( repository );

                // add context for repository
                ctx =
                    nexusIndexer.addIndexingContextForced( getContextId( repository.getId() ), repository.getId(),
                        repoRoot, indexDirectory, null, null, indexCreators );
                ctx.setSearchable( repository.isSearchable() );
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

            IndexingContext ctx = getRepositoryIndexContext( repository );

            if ( ctx != null )
            {
                nexusIndexer.removeIndexingContext( ctx, deleteFiles );
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
            IndexingContext ctx = getRepositoryIndexContext( repository );

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

    public IndexingContext getRepositoryIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        return getRepositoryIndexContext( repository );
    }

    public IndexingContext getRepositoryIndexContext( Repository repository )
    {
        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getContextId( repository.getId() ) );

        return ctx;
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

        IndexingContext ctx = getRepositoryIndexContext( repository );

        // do this only if we have contexts, otherwise be muted
        if ( ctx != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Searching on repository ID='" + repositoryId + "' is set to: " + String.valueOf( searchable ) );
            }

            ctx.setSearchable( searchable );
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
        if ( repository.getLocalUrl() != null
            && repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage )
        {
            try
            {
                File baseDir =
                    ( (DefaultFSLocalRepositoryStorage) repository.getLocalStorage() ).getBaseDir( repository,
                        new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ) );

                return baseDir;
            }
            catch ( LocalStorageException e )
            {
                getLogger().warn(
                    String.format( "Cannot determine \"%s\" (ID=%s) repository's basedir:", repository.getName(),
                        repository.getId() ), e );
            }
        }

        return null;
    }

    // ----------------------------------------------------------------------------
    // Publish the used NexusIndexer
    // ----------------------------------------------------------------------------

    protected NexusIndexer getNexusIndexer()
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
        if ( item.getRepositoryItemUid().getBooleanAttributeValue( IsHiddenAttribute.class ) )
        {
            getLogger().debug( "Will not index hidden file path: " + item.getRepositoryItemUid().toString() );

            return;
        }

        // do the work
        // Maybe detect Merged context and NOT do the work? Everything works transparently, but still... a lot of calls
        // for nothing
        IndexingContext context = getRepositoryIndexContext( repository );

        if ( context != null )
        {
            // by calculating GAV we check wether the request is against a repo artifact at all
            Gav gav = null;

            gav = ( (MavenRepository) repository ).getGavCalculator().pathToGav( item.getRepositoryItemUid().getPath() );

            // signatures and hashes are not considered for processing
            // reason (NEXUS-814 related): the actual artifact and it's POM will (or already did)
            // emitted events about modifying them
            if ( gav == null || gav.isSignature() || gav.isHash() )
            {
                // we do not index these
                return;
            }

            final RepositoryItemUidLock uidLock = item.getRepositoryItemUid().getLock();

            uidLock.lock( Action.read );

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
                        catch ( IllegalArgumentException e )
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
                                ai.sha1 = item.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
                            }
                        }
                    }
                }

                // and finally: index it
                getNexusIndexer().addArtifactToIndex( ac, context );
            }
            finally
            {
                uidLock.unlock();
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

        // index for proxy repos shouldn't change just because you deleted something locally
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // do the work
        IndexingContext context = getRepositoryIndexContext( repository );

        if ( context != null )
        {
            // by calculating GAV we check wether the request is against a repo artifact at all
            Gav gav = null;

            gav = ( (MavenRepository) repository ).getGavCalculator().pathToGav( item.getRepositoryItemUid().getPath() );

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
            
            // we need to convert Nexus Gav to Indexer Gav
            org.apache.maven.index.artifact.Gav igav = GavUtils.convert( gav );

            try
            {
                ac = new ArtifactContext( null, null, null, ai, igav );
            }
            catch ( IllegalArgumentException e )
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
                final RepositoryItemUidLock uidLock = item.getRepositoryItemUid().getLock();

                uidLock.lock( Action.read );

                try
                {
                    getNexusIndexer().deleteArtifactFromIndex( ac, context );
                }
                finally
                {
                    uidLock.unlock();
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
    // TODO: NEXUS-4052 and NEXUS-4053
    // when sorted out, these constants will help the change, just remove them

    // all index related operation cascade (currently yes)
    private static final boolean CASCADE = true;

    // reindex() method does publishing too (currently yes)
    private static final boolean REINDEX_PUBLISHES = true;

    // ----------------------------------------------------------------------------

    // ----------------------------------------------------------------------------
    // Reindexing related
    // ----------------------------------------------------------------------------

    public void reindexAllRepositories( final String path, final boolean fullReindex )
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            // going directly to single-shot, we are iterating over all reposes anyway
            reindexRepository( repository, path, fullReindex );
        }

        if ( REINDEX_PUBLISHES )
        {
            publishAllIndex();
        }
    }

    public void reindexRepository( final String path, final String repositoryId, final boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        reindexRepository( path, repository, fullReindex, new HashSet<String>() );
    }

    protected void reindexRepository( final String path, final Repository repository, final boolean fullReindex,
                                      final Set<String> processedRepositoryIds )
        throws IOException
    {
        if ( !processedRepositoryIds.add( repository.getId() ) )
        {
            // already processed, bail out
            return;
        }

        if ( CASCADE )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                List<Repository> members = repository.adaptToFacet( GroupRepository.class ).getMemberRepositories();

                for ( Repository member : members )
                {
                    reindexRepository( path, member, fullReindex, processedRepositoryIds );
                }
            }
        }

        reindexRepository( repository, path, fullReindex );

        if ( REINDEX_PUBLISHES )
        {
            publishRepositoryIndex( repository );
        }
    }

    protected void reindexRepository( final Repository repository, final String fromPath, final boolean fullReindex )
        throws IOException
    {
        if ( !LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
        {
            return;
        }

        if ( !isIndexingSupported( repository ) )
        {
            return;
        }

        if ( !repository.isIndexable() )
        {
            return;
        }

        if ( isAlreadyBeingIndexed( repository.getId() ) )
        {
            logAlreadyBeingIndexed( repository.getId(), "re-indexing" );
            return;
        }

        Lock lock = getLock( repository.getId() ).writeLock();
        lock.lock();

        try
        {
            IndexingContext context = getRepositoryIndexContext( repository );

            if ( fullReindex )
            {
                TaskUtil.checkInterruption();

                context.purge();

                deleteIndexItems( repository );
            }

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                TaskUtil.checkInterruption();

                downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ), fullReindex );
            }

            if ( !repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                TaskUtil.checkInterruption();

                // update always true, since we manually manage ctx purge
                nexusIndexer.scan( context, fromPath, null, true );
            }
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
            downloadRepositoryIndex( repository, false );
        }
    }

    public void downloadRepositoryIndex( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        downloadRepositoryIndex( repository, new HashSet<String>() );
    }

    public void downloadRepositoryIndex( final Repository repository, final Set<String> processedRepositoryIds )
        throws IOException
    {
        if ( !processedRepositoryIds.add( repository.getId() ) )
        {
            // already processed, bail out
            return;
        }

        if ( CASCADE )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                List<Repository> members = repository.adaptToFacet( GroupRepository.class ).getMemberRepositories();

                for ( Repository member : members )
                {
                    TaskUtil.checkInterruption();

                    downloadRepositoryIndex( member, processedRepositoryIds );
                }
            }
        }

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            TaskUtil.checkInterruption();

            downloadRepositoryIndex( repository.adaptToFacet( ProxyRepository.class ), false );
        }
    }

    protected boolean downloadRepositoryIndex( ProxyRepository repository, boolean forceFullUpdate )
        throws IOException
    {
        if ( !LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
        {
            return false;
        }

        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return false;
        }

        if ( !repository.isIndexable() )
        {
            return false;
        }

        // ensure this is a proxy repo, since download may happen with proxies only
        if ( !repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            return false;
        }

        if ( isAlreadyBeingIndexed( repository.getId() ) )
        {
            logAlreadyBeingIndexed( repository.getId(), "downloading index" );
            return false;
        }

        MavenProxyRepository mpr = repository.adaptToFacet( MavenProxyRepository.class );

        Lock lock = getLock( repository.getId() ).writeLock();
        lock.lock();

        try
        {
            TaskUtil.checkInterruption();

            // just keep the context 'out of service' while indexing, will be added at end
            boolean shouldDownloadRemoteIndex = mpr.isDownloadRemoteIndexes();

            boolean hasRemoteIndex = false;

            if ( shouldDownloadRemoteIndex )
            {
                try
                {
                    getLogger().info(
                        RepositoryStringUtils.getFormattedMessage( "Trying to get remote index for repository %s",
                            repository ) );

                    hasRemoteIndex = updateRemoteIndex( repository, forceFullUpdate );

                    if ( hasRemoteIndex )
                    {
                        getLogger().info(
                            RepositoryStringUtils.getFormattedMessage(
                                "Remote indexes updated successfully for repository %s", repository ) );
                    }
                    else
                    {
                        getLogger().info(
                            RepositoryStringUtils.getFormattedMessage(
                                "Remote indexes unchanged (no update needed) for repository %s", repository ) );
                    }
                }
                catch ( TaskInterruptedException e )
                {
                    getLogger().warn(
                        RepositoryStringUtils.getFormattedMessage(
                            "Cannot fetch remote index for repository %s, task cancelled.", repository ) );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        RepositoryStringUtils.getFormattedMessage( "Cannot fetch remote index for repository %s",
                            repository ), e );
                }
            }

            return hasRemoteIndex;

        }
        finally
        {
            lock.unlock();
        }
    }

    protected boolean updateRemoteIndex( final ProxyRepository repository, boolean forceFullUpdate )
        throws IOException, IllegalOperationException, ItemNotFoundException
    {
        TaskUtil.checkInterruption();

        // this will force remote check for newer files
        repository.expireCaches( new ResourceStoreRequest( PUBLISHING_PATH_PREFIX ) );

        IndexingContext context = getRepositoryIndexContext( repository );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context, new ResourceFetcher()
        {
            public void connect( String id, String url )
                throws IOException
            {
            }

            public void disconnect()
                throws IOException
            {
            }

            public InputStream retrieve( String name )
                throws IOException
            {
                TaskUtil.checkInterruption();

                ResourceStoreRequest req = new ResourceStoreRequest( PUBLISHING_PATH_PREFIX + "/" + name );

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

                    return item.getInputStream();
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
            }
        } );

        updateRequest.setForceFullUpdate( forceFullUpdate );

        if ( repository instanceof MavenRepository )
        {
            MavenRepository mrepository = (MavenRepository) repository;

            updateRequest.setDocumentFilter( getFilterFor( mrepository.getRepositoryPolicy() ) );
        }

        TaskUtil.checkInterruption();

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

        // just publish all, since we use merged context, no need for double pass
        for ( Repository repository : reposes )
        {
            publishRepositoryIndex( repository );
        }
    }

    public void publishRepositoryIndex( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        publishRepositoryIndex( repository, new HashSet<String>() );
    }

    protected void publishRepositoryIndex( final Repository repository, Set<String> processedRepositoryIds )
        throws IOException
    {
        if ( !processedRepositoryIds.add( repository.getId() ) )
        {
            // already processed, bail out
            return;
        }

        if ( CASCADE )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                List<Repository> members = repository.adaptToFacet( GroupRepository.class ).getMemberRepositories();

                for ( Repository member : members )
                {
                    TaskUtil.checkInterruption();

                    publishRepositoryIndex( member, processedRepositoryIds );
                }
            }
        }

        TaskUtil.checkInterruption();

        publishRepositoryIndex( repository );
    }

    protected void publishRepositoryIndex( final Repository repository )
        throws IOException
    {
        if ( !LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
        {
            return;
        }

        // is indexing supported at all?
        if ( !isIndexingSupported( repository ) )
        {
            logSkippingRepositoryMessage( repository );

            return;
        }

        // shadows are not capable to publish indexes
        if ( !repository.isIndexable() )
        {
            return;
        }

        if ( isAlreadyBeingIndexed( repository.getId() ) )
        {
            logAlreadyBeingIndexed( repository.getId(), "publishing index" );
            return;
        }

        File targetDir = null;

        Lock lock = getLock( repository.getId() ).readLock();
        lock.lock();

        try
        {
            TaskUtil.checkInterruption();

            getLogger().info( "Publishing index for repository " + repository.getId() );

            IndexingContext context = getRepositoryIndexContext( repository );

            targetDir = new File( getTempDirectory(), "nx-index-" + Long.toHexString( System.nanoTime() ) );

            if ( !targetDir.mkdirs() )
            {
                throw new IOException( "Could not create temp dir for packing indexes: " + targetDir );
            }

            IndexPackingRequest packReq = new IndexPackingRequest( context, targetDir );
            packReq.setCreateIncrementalChunks( true );

            // not publishing legacy format anymore
            packReq.setFormats( Arrays.asList( IndexFormat.FORMAT_V1 ) );
            indexPacker.packIndex( packReq );

            File[] files = targetDir.listFiles();

            if ( files != null )
            {
                for ( File file : files )
                {
                    TaskUtil.checkInterruption();

                    storeIndexItem( repository, file, context );
                }
            }
        }
        finally
        {
            lock.unlock();

            Exception lastException = null;

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
                IOException eek = new IOException( lastException );

                throw eek;
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    protected void deleteIndexItems( Repository repository )
    {
        ResourceStoreRequest request = new ResourceStoreRequest( PUBLISHING_PATH_PREFIX );

        try
        {
            repository.deleteItem( false, request );
        }
        catch ( ItemNotFoundException e )
        {
            // nothing serious, no index was published yet, keep it silent
        }
        catch ( Exception e )
        {
            getLogger().error( "Cannot delete index items!", e );
        }
    }

    protected void storeIndexItem( Repository repository, File file, IndexingContext context )
    {
        String path = PUBLISHING_PATH_PREFIX + "/" + file.getName();

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream( file );

            ResourceStoreRequest request = new ResourceStoreRequest( path );
            DefaultStorageFileItem fItem =
                new DefaultStorageFileItem( repository, request, true, true, new PreparedContentLocator( fis,
                    mimeSupport.guessMimeTypeFromPath( repository.getMimeRulesSource(), file.getAbsolutePath() ) ) );

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
            optimizeRepositoryIndex( repository );
        }
    }

    public void optimizeRepositoryIndex( final String repositoryId )
        throws NoSuchRepositoryException, IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        optimizeIndex( repository, new HashSet<String>() );
    }

    protected void optimizeIndex( final Repository repository, final Set<String> processedRepositoryIds )
        throws CorruptIndexException, IOException
    {
        if ( !processedRepositoryIds.add( repository.getId() ) )
        {
            // already processed, bail out
            return;
        }

        if ( CASCADE )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                GroupRepository group = repository.adaptToFacet( GroupRepository.class );

                for ( Repository member : group.getMemberRepositories() )
                {
                    TaskUtil.checkInterruption();

                    optimizeIndex( member, processedRepositoryIds );
                }
            }
        }

        TaskUtil.checkInterruption();

        optimizeRepositoryIndex( repository );
    }

    protected void optimizeRepositoryIndex( final Repository repository )
        throws CorruptIndexException, IOException
    {
        if ( !LocalStatus.IN_SERVICE.equals( repository.getLocalStatus() ) )
        {
            return;
        }

        // local
        IndexingContext context = getRepositoryIndexContext( repository );

        TaskUtil.checkInterruption();

        if ( context != null )
        {
            getLogger().debug( "Optimizing local index context for repository: " + repository.getId() );

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
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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

            req.getContexts().add( context );
        }

        // if ( from != null )
        // {
        // req.setStart( from );
        // }

        // MINDEXER-14: no hit limit anymore. But to make change least obtrusive, we set hitLimit as count 1st, and if
        // user set count, it will override it anyway
        if ( hitLimit != null )
        {
            req.setCount( hitLimit );
        }

        if ( count != null )
        {
            req.setCount( count );
        }

        // if ( hitLimit != null )
        // {
        // req._setResultHitLimit( hitLimit );
        // }

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

    @Deprecated
    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count,
                                                       Integer hitLimit )
        throws NoSuchRepositoryException
    {
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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

            req.getContexts().add( context );
        }

        // if ( from != null )
        // {
        // req.setStart( from );
        // }

        // MINDEXER-14: no hit limit anymore. But to make change least obtrusive, we set hitLimit as count 1st, and if
        // user set count, it will override it anyway
        if ( hitLimit != null )
        {
            req.setCount( hitLimit );
        }

        if ( count != null )
        {
            req.setCount( count );
        }

        // if ( hitLimit != null )
        // {
        // req._setResultHitLimit( hitLimit );
        // }

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

    @Deprecated
    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                                  String repositoryId, Integer from, Integer count, Integer hitLimit )
        throws NoSuchRepositoryException
    {
        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return new FlatSearchResponse( null, -1, new HashSet<ArtifactInfo>() );
        }

        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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

            req.getContexts().add( context );
        }

        // if ( from != null )
        // {
        // req.setStart( from );
        // }

        // MINDEXER-14: no hit limit anymore. But to make change least obtrusive, we set hitLimit as count 1st, and if
        // user set count, it will override it anyway
        if ( hitLimit != null )
        {
            req.setCount( hitLimit );
        }

        if ( count != null )
        {
            req.setCount( count );
        }

        // if ( hitLimit != null )
        // {
        // req._setResultHitLimit( hitLimit );
        // }

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

            result = sourceRepository.getName();
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

                        result = sourceRepository.getName();
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

        // MINDEXER-14: no hit limit anymore. But to make change least obtrusive, we set hitLimit as count 1st, and if
        // user set count, it will override it anyway
        if ( hitLimit != null )
        {
            req.setCount( hitLimit );
        }

        if ( count != null )
        {
            req.setCount( count );
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
            IndexingContext context = getRepositoryIndexContext( repositoryId );

            if ( context != null )
            {
                req.getContexts().add( context );
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
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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

        req.getMatchHighlightRequests().add( new MatchHighlightRequest( MAVEN.GROUP_ID, q1, MatchHighlightMode.HTML ) );
        req.getMatchHighlightRequests().add( new MatchHighlightRequest( MAVEN.ARTIFACT_ID, q2, MatchHighlightMode.HTML ) );

        if ( repositoryId != null )
        {
            req.getContexts().add( context );
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

    public IteratorSearchResponse searchArtifactClassIterator( String term, String repositoryId, Integer from,
                                                               Integer count, Integer hitLimit, SearchType searchType,
                                                               List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
        }

        if ( term.endsWith( ".class" ) )
        {
            term = term.substring( 0, term.length() - 6 );
        }

        Query q = constructQuery( MAVEN.CLASSNAMES, term, searchType );

        IteratorSearchRequest req = createRequest( q, from, count, hitLimit, false, filters );

        req.getMatchHighlightRequests().add( new MatchHighlightRequest( MAVEN.CLASSNAMES, q, MatchHighlightMode.HTML ) );

        if ( repositoryId != null )
        {
            req.getContexts().add( context );
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

        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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
            req.getContexts().add( context );
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

    public IteratorSearchResponse searchArtifactSha1ChecksumIterator( String sha1Checksum, String repositoryId,
                                                                      Integer from, Integer count, Integer hitLimit,
                                                                      List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        if ( sha1Checksum == null || sha1Checksum.length() > 40 )
        {
            return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
        }

        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = getRepositoryIndexContext( repositoryId );
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
            req.getContexts().add( context );
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

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    @Deprecated
    public Query constructQuery( Field field, String query, SearchType type )
        throws IllegalArgumentException
    {
        return nexusIndexer.constructQuery( field, query, type );
    }

    public Query constructQuery( Field field, SearchExpression expression )
        throws IllegalArgumentException
    {
        return nexusIndexer.constructQuery( field, expression );
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
        IndexingContext ctx = getRepositoryIndexContext( repositoryId );

        TreeViewRequest request = new TreeViewRequest( factory, path, hints, artifactInfoFilter, ctx );

        return indexTreeView.listNodes( request );
    }

    // ----------------------------------------------------------------------------
    // PRIVATE
    // ----------------------------------------------------------------------------

    protected String getContextId( String repoId )
    {
        return repoId + CTX_SUFIX;
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

    protected synchronized ReadWriteLock getLock( String repositoryId )
    {
        if ( !locks.containsKey( repositoryId ) )
        {
            locks.put( repositoryId, new ReentrantReadWriteLock() );
        }

        return locks.get( repositoryId );
    }

    protected boolean isAlreadyBeingIndexed( String repositoryId )
    {
        Lock lock = getLock( repositoryId ).readLock();

        boolean locked = lock.tryLock();

        if ( locked )
        {
            lock.unlock();
        }

        // if I can't get a read lock means someone else has the write lock (index tasks do write lock)
        return !locked;
    }

    private void logAlreadyBeingIndexed( final String repositoryId, final String processName )
    {
        getLogger().info(
            String.format( "Repository '%s' is already in the process of being re-indexed. Skipping %s'.",
                repositoryId, processName ) );
    }

}