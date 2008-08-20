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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventUpdate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventRemove;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.router.DefaultGroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.ResourceStoreIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;

/**
 * IndexManager
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultIndexerManager
    extends AbstractLogEnabled
    implements IndexerManager, Initializable, ConfigurationChangeListener, EventListener

{
    /** Context id local suffix */
    public static final String CTX_LOCAL_SUFIX = "-local";

    /** Context id remote suffix */
    public static final String CTX_REMOTE_SUFIX = "-remote";

    /** Context id merged suffix */
    public static final String CTX_MERGED_SUFIX = "-merged";

    /**
     * @plexus.requirement role="org.sonatype.nexus.index.ArtifactContextProducer"
     */
    private ArtifactContextProducer artifactContextProducer;

    /**
     * @plexus.requirement role="org.sonatype.nexus.index.NexusIndexer"
     */
    private NexusIndexer nexusIndexer;

    /**
     * @plexus.requirement role="org.sonatype.nexus.index.packer.IndexPacker"
     */
    private IndexPacker indexPacker;

    /**
     * @plexus.requirement
     */
    private NexusConfiguration nexusConfiguration;

    /**
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @plexus.requirement
     */
    private NexusScheduler nexusScheduler;

    /**
     * @plexus.requirement role="org.sonatype.nexus.proxy.router.RootRepositoryRouter"
     */
    private ResourceStoreIdBasedRepositoryRouter rootRouter;

    private File workingDirectory;

    private File tempDirectory;

    public void initialize()
        throws InitializationException
    {
        nexusConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        workingDirectory = null;

        tempDirectory = null;
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
            // ctx.close( deleteFiles );
        }
    }

    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    public void addRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        String remoteRoot = null;

        if ( repository.getRemoteUrl() != null )
        {
            remoteRoot = repository.getRemoteUrl();
        }

        File repoRoot = getRepositoryLocalStorageAsFile( repository );

        // add context for repository
        IndexingContext ctxLocal = nexusIndexer.addIndexingContextForced(
            getLocalContextId( repository.getId() ),
            repository.getId(),
            repoRoot,
            new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ),
            remoteRoot,
            null,
            NexusIndexer.FULL_INDEX );

        ctxLocal.setSearchable( repository.isIndexable() );

        if ( ctxLocal.getTimestamp() == null )
        {
            // it is probably new or first start
            ctxLocal.updateTimestamp();
        }

        if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
        {
            IndexingContext ctxRemote = nexusIndexer.addIndexingContextForced(
                getRemoteContextId( repository.getId() ),
                repository.getId(),
                repoRoot,
                new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                remoteRoot,
                remoteRoot,
                NexusIndexer.FULL_INDEX );

            ctxRemote.setSearchable( repository.isIndexable() );

            if ( ctxRemote.getTimestamp() == null )
            {
                // it is probably new or first start
                ctxRemote.updateTimestamp();
            }
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

        if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repository.getId() ) ) )
        {
            ctx = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repository.getId() ) );

            ctx.setRepository( repoRoot );
        }
    }

    public void addRepositoryGroupIndexContext( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException
    {
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

        if ( ctxMerged.getTimestamp() == null )
        {
            // it is probably new or first start
            ctxMerged.updateTimestamp();
        }
    }

    public void removeRepositoryGroupIndexContext( String repositoryGroupId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryGroupException
    {
        // remove context for repository
        if ( nexusIndexer.getIndexingContexts().containsKey( getMergedContextId( repositoryGroupId ) ) )
        {
            nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                getMergedContextId( repositoryGroupId ) ), deleteFiles );
        }
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
            catch ( URISyntaxException e )
            {
                repoRoot = new File( url.getPath() );
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

            boolean hasRemoteIndex = false;

            boolean shouldDownloadRemoteIndex = false;

            // only proxies have remote indexes
            if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
            {
                Map<String, Object> ctx = new HashMap<String, Object>();

                try
                {
                    CRepository repoModel = nexusConfiguration.readRepository( repository.getId() );

                    shouldDownloadRemoteIndex = repoModel.isDownloadRemoteIndexes();
                }
                catch ( NoSuchRepositoryException e )
                {
                    // TODO: heee?
                    shouldDownloadRemoteIndex = false;
                }

                RepositoryItemUid propsUid = repository.createUid( "/.index/" + IndexingContext.INDEX_FILE
                    + ".properties" );

                RepositoryItemUid zipUid = repository.createUid( "/.index/" + IndexingContext.INDEX_FILE + ".zip" );

                try
                {
                    if ( shouldDownloadRemoteIndex )
                    {
                        File tmpdir = null;

                        try
                        {
                            getLogger().info( "Trying to get remote index for repository " + repository.getId() );

                            // this will force redownload
                            repository.clearCaches( "/.index" );

                            StorageFileItem fitem = (StorageFileItem) repository.retrieveItem( false, propsUid, ctx );

                            fitem = (StorageFileItem) repository.retrieveItem( false, zipUid, ctx );

                            tmpdir = new File( getTempDirectory(), "nx-remote-index" + System.currentTimeMillis() );

                            tmpdir.mkdirs();

                            FSDirectory directory = FSDirectory.getDirectory( tmpdir );

                            BufferedInputStream is = new BufferedInputStream( fitem.getInputStream(), 4096 );

                            IndexUtils.unpackIndexArchive( is, directory );

                            if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
                            {
                                getLogger().info( "Filtering downloaded index..." );

                                MavenRepository mrepository = (MavenRepository) repository;

                                if ( RepositoryPolicy.RELEASE.equals( mrepository.getRepositoryPolicy() ) )
                                {
                                    IndexUtils.filterDirectory( directory, new DocumentFilter()
                                    {
                                        public boolean accept( Document doc )
                                        {
                                            String uinfo = doc.get( ArtifactInfo.UINFO );

                                            if ( uinfo != null )
                                            {
                                                String[] r = AbstractIndexCreator.FS_PATTERN.split( uinfo );

                                                String version = r[2];

                                                return !VersionUtils.isSnapshot( version );
                                            }
                                            else
                                            {
                                                // not an artifactInfo record, accept it
                                                return true;
                                            }
                                        }
                                    } );
                                }
                                else if ( RepositoryPolicy.SNAPSHOT.equals( mrepository.getRepositoryPolicy() ) )
                                {
                                    IndexUtils.filterDirectory( directory, new DocumentFilter()
                                    {
                                        public boolean accept( Document doc )
                                        {
                                            String uinfo = doc.get( ArtifactInfo.UINFO );

                                            if ( uinfo != null )
                                            {
                                                String[] r = AbstractIndexCreator.FS_PATTERN.split( uinfo );

                                                String version = r[2];

                                                return VersionUtils.isSnapshot( version );
                                            }
                                            else
                                            {
                                                // not an artifactInfo record, accept it
                                                return true;
                                            }
                                        }
                                    } );
                                }
                            }

                            IndexingContext context = nexusIndexer.getIndexingContexts().get(
                                getRemoteContextId( repository.getId() ) );

                            context.replace( directory );
                            /*
                             * if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) ) {
                             * getLogger().info( "Filtering downloaded index..." ); MavenRepository mrepository =
                             * (MavenRepository) repository; if ( RepositoryPolicy.RELEASE.equals(
                             * mrepository.getRepositoryPolicy() ) ) { context.filter( new ArtifactInfoFilter() { public
                             * boolean accept( ArtifactInfo ai ) { return !VersionUtils.isSnapshot( ai.version ); } } );
                             * } else if ( RepositoryPolicy.SNAPSHOT.equals( mrepository.getRepositoryPolicy() ) ) {
                             * context.filter( new ArtifactInfoFilter() { public boolean accept( ArtifactInfo ai ) {
                             * return VersionUtils.isSnapshot( ai.version ); } } ); } }
                             */
                            getLogger().info(
                                "Remote indexes published and imported succesfully for repository "
                                    + repository.getId() );

                            hasRemoteIndex = true;
                        }
                        catch ( ItemNotFoundException e )
                        {
                            getLogger().info(
                                "Repository " + repository.getId()
                                    + " has no available remote indexes but it is set to download them." );

                            hasRemoteIndex = false;
                        }
                        catch ( Exception e )
                        {
                            getLogger().warn( "Cannot fetch remote index:", e );

                            hasRemoteIndex = false;

                        }
                        finally
                        {
                            if ( tmpdir != null )
                            {
                                FileUtils.deleteDirectory( tmpdir );
                            }
                        }
                    }
                    else
                    {
                        // make empty the remote context
                        IndexingContext context = nexusIndexer.getIndexingContexts().get(
                            getRemoteContextId( repository.getId() ) );

                        context.purge();

                        hasRemoteIndex = false;
                    }

                    // clean up then
                    // if was exception, to clean partially downloaded stuff
                    // if was downloadRemoteIndexes false, to remove obsolete files
                    if ( !hasRemoteIndex )
                    {
                        // delete the potentially partially downloaded files
                        if ( propsUid != null )
                        {
                            try
                            {
                                repository.deleteItem( propsUid, ctx );
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

                        // delete the potentially partially downloaded files
                        if ( zipUid != null )
                        {
                            try
                            {
                                repository.deleteItem( zipUid, ctx );
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
                }
                finally
                {
                    repository.releaseUid( propsUid );

                    repository.releaseUid( zipUid );
                }
            }

            IndexingContext context = null;

            File targetDir = null;

            if ( !hasRemoteIndex )
            {
                getLogger().info( "Publishing local index for repository " + repository.getId() );

                // otherwise publish local cache index
                context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

                try
                {
                    targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

                    if ( targetDir.mkdirs() )
                    {
                        indexPacker.packIndex( context, targetDir );

                        FileInputStream fis = null;

                        DefaultStorageFileItem fItem = null;

                        File[] files = targetDir.listFiles();

                        if ( files != null )
                        {
                            for ( File file : files )
                            {
                                try
                                {
                                    fis = new FileInputStream( file );

                                    fItem = new DefaultStorageFileItem(
                                        repository,
                                        "/.index/" + file.getName(),
                                        true,
                                        true,
                                        fis );

                                    if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
                                    {
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
                                    getLogger().error( "Cannot store index file " + fItem.getPath(), e );
                                }
                                finally
                                {
                                    IOUtil.close( fis );
                                }
                            }
                        }
                    }
                    else
                    {
                        getLogger().error( "Could not create temp dir for packing indexes: " + targetDir );
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
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );
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
                bestContext = nexusIndexer.getIndexingContexts().get( getLocalContextId( repo.getId() ) );

                if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repo.getId() ) ) )
                {
                    // if remote is here and is downloaded, it is the best
                    if ( bestContext.getIndexReader().numDocs() < nexusIndexer.getIndexingContexts().get(
                        getRemoteContextId( repo.getId() ) ).getIndexReader().numDocs() )
                    {
                        bestContext = nexusIndexer.getIndexingContexts().get( getRemoteContextId( repo.getId() ) );
                    }
                }

                if ( bestContext != null )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            " ...found best context " + bestContext.getId() + " for repository "
                                + bestContext.getRepositoryId() + ", merging it..." );
                    }
                    context.merge( bestContext.getIndexDirectory() );
                }
            }
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Rebuilding IDE information in merged index for repository group " + repositoryGroupId );
        }

        // rebuild IDE information
        nexusIndexer.rebuildGroups( context );

        // commiting changes
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

            IndexingContext context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

            tmpdir = new File( getTempDirectory(), "nx-remote-index" + System.currentTimeMillis() );

            tmpdir.mkdirs();

            FSDirectory directory = FSDirectory.getDirectory( tmpdir );

            tmpContext = nexusIndexer.addIndexingContextForced(
                context.getId() + "-tmp",
                context.getRepositoryId(),
                context.getRepository(),
                directory,
                context.getRepositoryUrl(),
                context.getIndexUpdateUrl(),
                context.getIndexCreators() );

            nexusIndexer.scan( tmpContext );

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
                bq.add( nexusIndexer.constructQuery( ArtifactInfo.PACKAGING, vTerm ), BooleanClause.Occur.MUST );
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

    // ----------------------------------------------------------------------------
    // Event handling
    // ----------------------------------------------------------------------------

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Processing event (" + evt.toString() + ")" );
        }

        if ( RepositoryRegistryEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            try
            {
                Repository repository = ( (RepositoryRegistryEvent) evt ).getRepository();

                // we are handling repo events, like addition and removal
                if ( RepositoryRegistryEventAdd.class.isAssignableFrom( evt.getClass() ) )
                {
                    addRepositoryIndexContext( repository.getId() );
                }
                else if ( RepositoryRegistryEventRemove.class.isAssignableFrom( evt.getClass() ) )
                {
                    removeRepositoryIndexContext( repository.getId(), false );
                }
                else if ( RepositoryRegistryEventUpdate.class.isAssignableFrom( evt.getClass() ) )
                {
                    updateRepositoryIndexContext( repository.getId() );
                }
            }
            catch ( Exception e )
            {
                getLogger().error( "Could not maintain indexing contexts!", e );
            }
        }
        else if ( RepositoryRegistryGroupEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            try
            {
                RepositoryRegistryGroupEvent gevt = (RepositoryRegistryGroupEvent) evt;

                // we are handling repo events, like addition and removal
                if ( RepositoryRegistryGroupEventAdd.class.isAssignableFrom( evt.getClass() ) )
                {
                    addRepositoryGroupIndexContext( gevt.getGroupId() );
                }
                else if ( RepositoryRegistryGroupEventRemove.class.isAssignableFrom( evt.getClass() ) )
                {
                    removeRepositoryGroupIndexContext( gevt.getGroupId(), false );
                }
            }
            catch ( Exception e )
            {
                getLogger().error( "Could not maintain group (merged) indexing contexts!", e );
            }
        }
        else if ( RepositoryItemEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            try
            {
                RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

                // sadly, the nexus-indexer is maven2 only, hence we check is the repo
                // from where we get the event is a maven2 repo
                if ( !MavenRepository.class.isAssignableFrom( ievt.getRepository().getClass() ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "This is not a MavenRepository instance, will not process it." );
                    }

                    return;
                }

                // should we sync at all
                if ( ievt.getRepository().isIndexable()
                    && ( RepositoryItemEventStore.class.isAssignableFrom( ievt.getClass() )
                        || RepositoryItemEventCache.class.isAssignableFrom( ievt.getClass() ) || RepositoryItemEventDelete.class
                        .isAssignableFrom( ievt.getClass() ) ) )
                {
                    IndexingContext context = nexusIndexer.getIndexingContexts().get(
                        getLocalContextId( ievt.getRepository().getId() ) );

                    // by calculating GAV we check wether the request is against a repo artifact at all
                    Gav gav = ( (MavenRepository) ievt.getRepository() ).getGavCalculator().pathToGav(
                        ievt.getItemUid().getPath() );

                    if ( context != null && gav != null )
                    {
                        // if we have a valid indexing context and have access to a File
                        if ( DefaultFSLocalRepositoryStorage.class.isAssignableFrom( ievt
                            .getItemUid().getRepository().getLocalStorage().getClass() ) )
                        {
                            File file = ( (DefaultFSLocalRepositoryStorage) ievt.getRepository().getLocalStorage() )
                                .getFileFromBase( ievt.getItemUid() );

                            if ( file.exists() )
                            {
                                ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

                                if ( ac != null )
                                {
                                    if ( getLogger().isDebugEnabled() )
                                    {
                                        getLogger()
                                            .debug( "The ArtifactContext created from file is fine, continuing." );
                                    }

                                    ArtifactInfo ai = ac.getArtifactInfo();

                                    if ( ievt instanceof RepositoryItemEventCache )
                                    {
                                        // add file to index
                                        getLogger().debug(
                                            "Adding artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                                + " to index (CACHE)." );
                                        nexusIndexer.addArtifactToIndex( ac, context );
                                    }
                                    else if ( ievt instanceof RepositoryItemEventStore )
                                    {
                                        // add file to index
                                        getLogger().debug(
                                            "Adding artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                                + " to index (STORE)." );
                                        nexusIndexer.addArtifactToIndex( ac, context );
                                    }
                                    else if ( ievt instanceof RepositoryItemEventDelete )
                                    {
                                        // remove file from index
                                        getLogger().debug(
                                            "Deleting artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                                + " from index (DELETE)." );
                                        nexusIndexer.deleteArtifactFromIndex( ac, context );
                                    }
                                }
                            }
                        }
                        else
                        {
                            if ( ievt instanceof RepositoryItemEventDelete )
                            {
                                ArtifactInfo ai = new ArtifactInfo();

                                ai.groupId = gav.getGroupId();

                                ai.artifactId = gav.getArtifactId();

                                ai.version = gav.getVersion();

                                ai.classifier = gav.getClassifier();

                                ArtifactContext ac = new ArtifactContext( null, null, null, ai );

                                // remove file from index
                                getLogger().debug(
                                    "Deleting artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                        + " from index (DELETE)." );
                                nexusIndexer.deleteArtifactFromIndex( ac, context );
                            }
                        }
                    }
                }
            }
            catch ( Exception e ) // TODO be more specific
            {
                getLogger().error( "Could not maintain index!", e );
            }
        }
    }

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
