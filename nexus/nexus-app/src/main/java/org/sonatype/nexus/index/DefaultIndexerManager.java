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
package org.sonatype.nexus.index;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.updater.IndexUpdater;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.router.DefaultGroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.ResourceStoreIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RootRepositoryRouter;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;

/**
 * Indexer Manager
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

    /** Context id merged suffix */
    public static final String CTX_MERGED_SUFIX = "-merged";

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

    @Requirement( role = RootRepositoryRouter.class )
    private ResourceStoreIdBasedRepositoryRouter rootRouter;

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
            // ctx.close( deleteFiles );
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

        File repoRoot = getRepositoryLocalStorageAsFile( repository );

        // add context for repository
        IndexingContext ctxLocal = nexusIndexer.addIndexingContextForced(
            getLocalContextId( repository.getId() ),
            repository.getId(),
            repoRoot,
            new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ),
            repository.getRemoteUrl(),
            null,
            NexusIndexer.FULL_INDEX );

        ctxLocal.setSearchable( repository.isIndexable() );

        if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
        {
            IndexingContext ctxRemote = nexusIndexer.addIndexingContextForced(
                getRemoteContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                repository.getRemoteUrl(),
                repository.getRemoteUrl(),
                NexusIndexer.FULL_INDEX );

            ctxRemote.setSearchable( repository.isIndexable() );
        }
    }

    public void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryException
    {
        // remove context for repository
        nexusIndexer.removeIndexingContext(
            nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) ),
            deleteFiles );

        if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repositoryId ) ) )
        {
            nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                getRemoteContextId( repositoryId ) ), deleteFiles );
        }
    }

    public void updateRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        File repoRoot = getRepositoryLocalStorageAsFile( repository );

        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

        ctx.setRepository( repoRoot );

        ctx.setRepositoryUrl( repository.getRemoteUrl() );

        // watch for HOSTED -> PROXY and PROXY -> HOSTED transitions (existence of the remote idx context)
        if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
        {
            if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repository.getId() ) ) )
            {
                // good, it should have remote context and there is already one
                ctx = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repository.getId() ) );

                ctx.setRepository( repoRoot );

                ctx.setRepositoryUrl( repository.getRemoteUrl() );

                ctx.setIndexUpdateUrl( repository.getRemoteUrl() );
            }
            else
            {
                // it should have remote context, but there is none. Create one on the fly.
                IndexingContext ctxRemote = nexusIndexer.addIndexingContextForced(
                    getRemoteContextId( repository.getId() ),
                    repository.getId(),
                    repoRoot,
                    new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                    repository.getRemoteUrl(),
                    repository.getRemoteUrl(),
                    NexusIndexer.FULL_INDEX );

                ctxRemote.setSearchable( repository.isIndexable() );
            }
        }
        else
        {
            if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repository.getId() ) ) )
            {
                // bad, it should have no remote context and there is already one
                nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                    getRemoteContextId( repositoryId ) ), true );
            }
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

    public void addRepositoryGroupIndexContext( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException
    {
        // just to throw NoSuchRepositoryGroupException if not existing
        repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        IndexingContext ctxMerged = nexusIndexer.addIndexingContextForced(
            getMergedContextId( repositoryGroupId ),
            repositoryGroupId,
            null,
            new File( getWorkingDirectory(), getMergedContextId( repositoryGroupId ) ),
            null,
            null,
            NexusIndexer.FULL_INDEX );

        // the merged context is not adding any value, so it does not takes part in searches
        // it gets only published

        ctxMerged.setSearchable( false );

        // if ( ctxMerged.getTimestamp() == null )
        // {
        // // it is probably new or first start
        // ctxMerged.updateTimestamp();
        // }
    }

    public void removeRepositoryGroupIndexContext( String repositoryGroupId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryGroupException
    {
        // just to throw NoSuchRepositoryGroupException if not existing
        repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        // remove context for repository
        if ( nexusIndexer.getIndexingContexts().containsKey( getMergedContextId( repositoryGroupId ) ) )
        {
            nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                getMergedContextId( repositoryGroupId ) ), deleteFiles );
        }
    }

    public IndexingContext getRepositoryGroupContext( String repositoryGroupId )
        throws NoSuchRepositoryGroupException
    {
        // just to throw NoSuchRepositoryGroupException if not existing
        repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        // get context for repository
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getMergedContextId( repositoryGroupId ) );

        return ctx;
    }

    public void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException,
            NoSuchRepositoryException
    {
        IndexingContext ctx = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );

        if ( !ctx.isSearchable() && searchable )
        {
            // we have a !searchable -> searchable transition, reindex it
            ReindexTask rt = (ReindexTask) nexusScheduler.createTaskInstance( ReindexTask.class );

            rt.setRepositoryId( repositoryId );

            nexusScheduler.submit( "Searchable re-enabled", rt );
        }

        ctx.setSearchable( searchable );

        if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repositoryId ) ) )
        {
            nexusIndexer.getIndexingContexts().get( getRemoteContextId( repositoryId ) ).setSearchable( searchable );
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
            URL url = null;

            try
            {
                url = new URL( repository.getLocalUrl() );

                repoRoot = new File( url.toURI() );
            }
            catch ( MalformedURLException e )
            {
                // Try just a regular file
                repoRoot = new File( repository.getLocalUrl() );
            }
            catch ( Throwable t )
            {
                repoRoot = new File( url.getPath() );
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

        List<String> groupIds = repositoryRegistry.getRepositoryGroupIds();

        for ( String groupId : groupIds )
        {
            try
            {
                publishRepositoryGroupIndex( groupId, repositoryRegistry.getRepositoryGroup( groupId ) );
            }
            catch ( NoSuchRepositoryGroupException e )
            {
                // will not be thrown
            }
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
            NoSuchRepositoryGroupException
    {
        List<Repository> group = repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        for ( Repository repository : group )
        {
            publishRepositoryIndex( repository );
        }

        publishRepositoryGroupIndex( repositoryGroupId, group );
    }

    protected void publishRepositoryIndex( Repository repository )
        throws IOException
    {
        // shadows are not capable to publish indexes
        if ( RepositoryType.SHADOW.equals( repository.getRepositoryType() ) )
        {
            return;
        }

        boolean repositoryIndexable = repository.isIndexable();

        try
        {
            repository.setIndexable( false );

            // only proxies have remote indexes
            if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
            {
                updateIndexForRemoteRepository( repository );
            }

            getLogger().info( "Publishing local index for repository " + repository.getId() );

            // publish index update
            IndexingContext context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

            File targetDir = null;

            try
            {
                targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

                if ( !targetDir.mkdirs() )
                {
                    getLogger().error( "Could not create temp dir for packing indexes: " + targetDir );
                }
                else
                {
                    indexPacker.packIndex( context, targetDir );

                    File[] files = targetDir.listFiles();

                    if ( files != null )
                    {
                        for ( File file : files )
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
                                    fis );
                                if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
                                {
                                    // XXX review this! index pieces need to be proxied differently
                                    // for proxy reposes, date is important
                                    // for locally published indexes in proxy reposes, set file dates old
                                    // by setting spoofed file timestamps old, we will refetch them when we can
                                    fItem.setModified( 1 );
                                    fItem.setCreated( 1 );
                                }

                                repository.storeItem( fItem );
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

    private boolean updateIndexForRemoteRepository( Repository repository )
        throws IOException
    {
        boolean shouldDownloadRemoteIndex = false;

        try
        {
            CRepository repoModel = nexusConfiguration.readRepository( repository.getId() );

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
                getLogger().info( "Trying to get remote index for repository " + repository.getId() );

                updateRemoteIndex( repository );

                getLogger().info( "Remote indexes updated successfully for repository " + repository.getId() );

                hasRemoteIndex = true;
            }
            catch ( Exception e )
            {
                getLogger().warn( "Cannot fetch remote index:", e );
            }
        }
        else
        {
            // make empty the remote context
            IndexingContext context = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repository.getId() ) );

            context.purge();

            // XXX remove obsolete files, should remove all index fragments
            // deleteItem( repository, ctx, zipUid );
            // deleteItem( repository, ctx, chunkUid ) ;
        }

        return hasRemoteIndex;
    }

    private boolean updateRemoteIndex( Repository repository )
        throws IOException,
            RepositoryNotAvailableException,
            ItemNotFoundException
    {
        // this will force redownload
        // XXX should only force downloading of the .properties file
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

        Date contextTimestamp = context.getTimestamp();

        RepositoryItemUid propsUid = repository.createUid( //
            "/.index/" + IndexingContext.INDEX_FILE + ".properties" );

        Map<String, Object> ctx = new HashMap<String, Object>();

        StorageFileItem propItem = retrieveItem( repository, ctx, propsUid );

        File tmpdir = null;

        FSDirectory directory = null;

        try
        {
            if ( contextTimestamp != null )
            {
                Properties properties = loadProperties( propItem );

                Date updateTimestamp = indexUpdater.getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

                if ( updateTimestamp != null && contextTimestamp.after( updateTimestamp ) )
                {
                    return true; // index is up to date
                }

                String chunkName = indexUpdater.getUpdateChunkName( contextTimestamp, properties );

                if ( chunkName != null )
                {
                    // download update index chunk
                    RepositoryItemUid zipUid = repository.createUid( "/.index/" + chunkName );

                    StorageFileItem chunkItem = retrieveItem( repository, ctx, zipUid );

                    tmpdir = createTmpDir();

                    directory = unpackIndex( chunkItem, repository, tmpdir );

                    context.merge( directory );

                    return true;
                }
            }

            // download full index
            RepositoryItemUid zipUid = //
            repository.createUid( "/.index/" + IndexingContext.INDEX_FILE + ".zip" );

            StorageFileItem zipItem = retrieveItem( repository, ctx, zipUid );

            tmpdir = createTmpDir();

            directory = unpackIndex( zipItem, repository, tmpdir );

            context.replace( directory );

            return true;
        }
        finally
        {
            if ( directory != null )
            {
                directory.close();
            }

            if ( tmpdir != null )
            {
                try
                {
                    FileUtils.deleteDirectory( tmpdir );
                }
                catch ( IOException ex )
                {
                    // ignore
                }
            }
        }
    }

    private Properties loadProperties( StorageFileItem item )
        throws IOException
    {
        InputStream is = null;
        try
        {
            is = item.getInputStream();

            Properties properties = new Properties();

            properties.load( is );

            return properties;
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    private StorageFileItem retrieveItem( Repository repository, Map<String, Object> ctx, RepositoryItemUid uid )
        throws StorageException,
            RepositoryNotAvailableException,
            ItemNotFoundException
    {
        try
        {
            return (StorageFileItem) repository.retrieveItem( false, uid, ctx );
        }
        catch ( StorageException ex )
        {
            deleteItem( repository, ctx, uid );

            throw ex;
        }
        catch ( ItemNotFoundException ex )
        {
            deleteItem( repository, ctx, uid );

            throw ex;
        }
        catch ( RepositoryNotAvailableException ex )
        {
            deleteItem( repository, ctx, uid );

            throw ex;
        }
    }

    private File createTmpDir()
    {
        File tmpdir = new File( getTempDirectory(), "nx-remote-index" + System.currentTimeMillis() );

        tmpdir.mkdirs();

        return tmpdir;
    }

    private FSDirectory unpackIndex( StorageFileItem item, Repository repository, File tmpdir )
        throws IOException
    {
        FSDirectory directory = FSDirectory.getDirectory( tmpdir );

        BufferedInputStream is = new BufferedInputStream( item.getInputStream(), 4096 );

        IndexUtils.unpackIndexArchive( is, directory );

        if ( repository instanceof MavenRepository )
        {
            getLogger().info( "Filtering downloaded index..." );

            MavenRepository mrepository = (MavenRepository) repository;

            IndexUtils.filterDirectory( directory, mrepository.getRepositoryPolicy().getFilter() );
        }

        return directory;
    }

    private void deleteItem( Repository repository, Map<String, Object> ctx, RepositoryItemUid uid )
    {
        if ( uid != null )
        {
            try
            {
                repository.deleteItem( uid, ctx );
            }
            catch ( ItemNotFoundException ex )
            {
                // silent
            }
            catch ( Exception ex )
            {
                getLogger().warn( "Cannot delete index part:", ex );
            }
        }
    }

    protected void publishRepositoryGroupIndex( String repositoryGroupId, List<Repository> repositories )
        throws IOException,
            NoSuchRepositoryGroupException
    {
        getLogger().info( "Merging and publishing index for repository group " + repositoryGroupId );

        IndexingContext context = nexusIndexer.getIndexingContexts().get( getMergedContextId( repositoryGroupId ) );

        context.purge();

        IndexingContext bestContext = null;

        for ( Repository repo : repositories )
        {
            // only merge non-shadows and indexable reposes
            if ( !RepositoryType.SHADOW.equals( repo.getRepositoryType() ) && repo.isIndexable() )
            {
                // local idx has every repo
                try
                {
                    bestContext = getRepositoryBestIndexContext( repo.getId() );
                }
                catch ( NoSuchRepositoryException e )
                {
                    // not to happen, we are iterating over them
                }

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        " ...found best context " + bestContext.getId() + " for repository "
                            + bestContext.getRepositoryId() + ", merging it..." );
                }

                context.merge( bestContext.getIndexDirectory() );
            }
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

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Publishing merged index for repository group " + repositoryGroupId );
        }

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

                indexPacker.packIndex( context, targetDir );

                FileInputStream fi = null;

                DigestInputStream sha1Is = null;

                DigestInputStream md5Is = null;

                File[] files = targetDir.listFiles();

                if ( files != null )
                {
                    try
                    {
                        for ( File file : files )
                        {
                            fi = new FileInputStream( file );

                            sha1Is = new DigestInputStream( fi, MessageDigest.getInstance( "SHA1" ) );

                            md5Is = new DigestInputStream( sha1Is, MessageDigest.getInstance( "MD5" ) );

                            String filePath = DefaultGroupIdBasedRepositoryRouter.ID + "/" + repositoryGroupId
                                + "/.index/" + file.getName();

                            RepositoryRouter router = (RepositoryRouter) rootRouter.resolveResourceStore(
                                new ResourceStoreRequest( filePath, true ) ).get( 0 );

                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug(
                                    "Storing the " + file.getName() + " file in the " + router.getId() + " router." );
                            }

                            router.storeItem( repositoryGroupId + "/.index/" + file.getName(), md5Is );

                            String sha1Sum = new String( Hex.encodeHex( sha1Is.getMessageDigest().digest() ) );

                            String md5Sum = new String( Hex.encodeHex( md5Is.getMessageDigest().digest() ) );

                            router.storeItem(
                                repositoryGroupId + "/.index/" + file.getName() + ".sha1",
                                new ByteArrayInputStream( sha1Sum.getBytes() ) );

                            router.storeItem(
                                repositoryGroupId + "/.index/" + file.getName() + ".md5",
                                new ByteArrayInputStream( md5Sum.getBytes() ) );
                        }
                    }
                    catch ( NoSuchAlgorithmException e )
                    {
                        // will not happen
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
        throws NoSuchRepositoryGroupException,
            IOException
    {
        List<Repository> group = repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        for ( Repository repository : group )
        {
            reindexRepository( repository );
        }

        publishRepositoryGroupIndex( repositoryGroupId );
    }

    protected void reindexRepository( Repository repository )
        throws IOException
    {
        boolean repositoryIndexable = repository.isIndexable();

        IndexingContext tmpContext = null;

        File tmpdir = null;

        try
        {
            repository.setIndexable( false );

            tmpdir = new File( getTempDirectory(), "nx-remote-index" + System.currentTimeMillis() );

            tmpdir.mkdirs();

            FSDirectory directory = FSDirectory.getDirectory( tmpdir );

            IndexingContext context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

            tmpContext = nexusIndexer.addIndexingContextForced(
                context.getId() + "-tmp",
                context.getRepositoryId(),
                context.getRepository(),
                directory,
                context.getRepositoryUrl(),
                context.getIndexUpdateUrl(),
                context.getIndexCreators() );

            nexusIndexer.scan( tmpContext );

            tmpContext.updateTimestamp( true );

            context.replace( tmpContext.getIndexDirectory() );
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );

            if ( tmpContext != null )
            {
                nexusIndexer.removeIndexingContext( tmpContext, true );
            }

            if ( tmpdir != null )
            {
                FileUtils.deleteDirectory( tmpdir );
            }
        }
    }

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.identify( type, checksum );
    }

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, String groupId, Integer from,
        Integer count )
    {
        IndexingContext context = null;

        if ( groupId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( groupId ) );
        }

        if ( repositoryId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );
        }

        FlatSearchRequest req = null;

        try
        {
            Query q1 = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, term );

            Query q2 = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, term );

            BooleanQuery bq = new BooleanQuery();

            bq.add( q1, BooleanClause.Occur.SHOULD );

            bq.add( q2, BooleanClause.Occur.SHOULD );

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

            FlatSearchResponse result = nexusIndexer.searchFlat( req );

            postprocessResults( result.getResults() );

            return result;
        }
        catch ( IndexContextInInconsistentStateException e )
        {
            getLogger().error( "Inconsistent index context state while searching for query \"" + term + "\"", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Got I/O exception while searching for query \"" + term + "\"", e );
        }

        return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
    }

    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, String groupId, Integer from,
        Integer count )
    {
        IndexingContext context = null;

        if ( groupId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( groupId ) );
        }

        if ( repositoryId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );
        }

        FlatSearchRequest req = null;

        try
        {
            if ( term.endsWith( ".class" ) )
            {
                term = term.substring( 0, term.length() - 6 );
            }

            term = StringUtils.replace( term, '.', '/' );

            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, term );

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

            FlatSearchResponse result = nexusIndexer.searchFlat( req );

            postprocessResults( result.getResults() );

            return result;
        }
        catch ( IndexContextInInconsistentStateException e )
        {
            getLogger().error( "Inconsistent index context state while searching for query \"" + term + "\"", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Got I/O exception while searching for query \"" + term + "\"", e );
        }

        return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
    }

    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, String groupId, Integer from, Integer count )
    {
        IndexingContext context = null;

        if ( gTerm == null && aTerm == null && vTerm == null )
        {
            return new FlatSearchResponse( null, 0, new HashSet<ArtifactInfo>() );
        }

        if ( groupId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( groupId ) );
        }

        if ( repositoryId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );
        }

        BooleanQuery bq = null;

        FlatSearchRequest req = null;

        try
        {
            bq = new BooleanQuery();

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
                // classifiers are sadly not indexed
            }

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

            FlatSearchResponse result = nexusIndexer.searchFlat( req );

            postprocessResults( result.getResults() );

            return result;
        }
        catch ( IndexContextInInconsistentStateException e )
        {
            getLogger().error(
                "Inconsistent index context state while searching for query \"" + bq.toString() + "\"",
                e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Got I/O exception while searching for query \"" + bq.toString() + "\"", e );
        }

        return new FlatSearchResponse( req.getQuery(), 0, new HashSet<ArtifactInfo>() );
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
                if ( RepositoryType.PROXY.equals( sourceRepository.getRepositoryType() ) )
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
            else if ( ai.context.endsWith( CTX_MERGED_SUFIX ) )
            {
                // TODO: this is not repo!
                result = result + " (Merged)";
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

    // == En Privee

    protected String getLocalContextId( String repositoryId )
    {
        return repositoryId + CTX_LOCAL_SUFIX;
    }

    protected String getRemoteContextId( String repositoryId )
    {
        return repositoryId + CTX_REMOTE_SUFIX;
    }

    protected String getMergedContextId( String groupId )
    {
        return groupId + CTX_MERGED_SUFIX;
    }

}
