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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryGroupEventRemove;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * IndexManager
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultIndexerManager
    extends AbstractLogEnabled
    implements IndexerManager, Initializable, ConfigurationChangeListener
{

    /** Context id local suffix */
    public static final String CTX_LOCAL_SUFIX = "-local";

    /** Context id remote suffix */
    public static final String CTX_REMOTE_SUFIX = "-remote";

    /** Context id merged suffix */
    public static final String CTX_MERGED_SUFIX = "-merged";

    /** Treshold for result set TODO: implement this in nexus indexer! */
    private static final int RESULT_LIMIT = 100;

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
    public void shutdown()
    {
        for ( IndexingContext ctx : nexusIndexer.getIndexingContexts().values() )
        {
            try
            {
                ctx.close( true );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Could not close indexing context " + ctx.getId(), e );
            }
        }
    }

    // =============
    // Index publishing

    public void publishAllIndex()
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            publishRepositoryIndex( repository );
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
                try
                {
                    CRepository repoModel = nexusConfiguration.readRepository( repository.getId() );

                    shouldDownloadRemoteIndex = repoModel.isDownloadRemoteIndexes();
                }
                catch ( NoSuchRepositoryException e )
                {
                    shouldDownloadRemoteIndex = false;
                }

                if ( shouldDownloadRemoteIndex )
                {
                    try
                    {
                        getLogger().info( "Trying to get remote index for repository " + repository.getId() );

                        // this will force redownload
                        repository.clearCaches( "/.index" );

                        RepositoryItemUid uid = new RepositoryItemUid( repository, "/.index/"
                            + IndexingContext.INDEX_FILE + ".properties" );

                        StorageFileItem fitem = (StorageFileItem) repository.retrieveItem( false, uid );

                        uid = new RepositoryItemUid( repository, "/.index/" + IndexingContext.INDEX_FILE + ".zip" );

                        fitem = (StorageFileItem) repository.retrieveItem( false, uid );

                        RAMDirectory directory = new RAMDirectory();

                        BufferedInputStream is = new BufferedInputStream( fitem.getInputStream(), 4096 );

                        IndexUtils.unpackIndexArchive( is, directory );

                        IndexingContext context = nexusIndexer.getIndexingContexts().get(
                            getRemoteContextId( repository.getId() ) );

                        context.replace( directory );

                        context.updateTimestamp();

                        getLogger().info(
                            "Remote indexes published and merged succesfully for repository " + repository.getId() );

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
                }
            }

            IndexingContext context = null;

            File targetDir = null;

            if ( !hasRemoteIndex )
            {
                getLogger().info( "Publishing local index for repository " + repository.getId() );

                // otherwise publich local cache index
                context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

                try
                {
                    targetDir = new File( getTempDirectory(), "nx-index" + System.currentTimeMillis() );

                    if ( targetDir.mkdirs() )
                    {
                        indexPacker.packIndex( context, targetDir );

                        FileInputStream fis = null;

                        DefaultStorageFileItem fItem = null;

                        for ( File file : targetDir.listFiles() )
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

    // =============
    // Search and indexing related

    public void reindexAllRepositories()
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repository : reposes )
        {
            reindexRepository( repository );
        }
    }

    public void reindexRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        reindexRepository( repository );
    }

    public void reindexRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        List<Repository> group = repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        for ( Repository repository : group )
        {
            reindexRepository( repository );
        }
    }

    protected void reindexRepository( Repository repository )
        throws IOException
    {
        boolean repositoryIndexable = repository.isIndexable();

        try
        {
            repository.setIndexable( false );

            IndexingContext context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repository.getId() ) );

            nexusIndexer.scan( context );

            publishRepositoryIndex( repository );
        }
        finally
        {
            repository.setIndexable( repositoryIndexable );
        }
    }

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.identify( type, checksum );
    }

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    public Collection<ArtifactInfo> searchFlat( Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return limitResults( nexusIndexer.searchFlat( query ), RESULT_LIMIT );
    }

    public Collection<ArtifactInfo> searchFlat( Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return limitResults( nexusIndexer.searchFlat( query, context ), RESULT_LIMIT );
    }

    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return limitResults( nexusIndexer.searchFlat( artifactInfoComparator, query ), RESULT_LIMIT );
    }

    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
        IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return limitResults( nexusIndexer.searchFlat( artifactInfoComparator, query, context ), RESULT_LIMIT );
    }

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.searchGrouped( grouping, query );
    }

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.searchGrouped( grouping, query, context );
    }

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.searchGrouped( grouping, groupKeyComparator, query );
    }

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return nexusIndexer.searchGrouped( grouping, groupKeyComparator, query, context );
    }

    protected Collection<ArtifactInfo> limitResults( Collection<ArtifactInfo> res, int count )
    {
        List<ArtifactInfo> result = new ArrayList<ArtifactInfo>( count );

        for ( Iterator<ArtifactInfo> i = res.iterator(); i.hasNext(); )
        {
            ArtifactInfo ai = i.next();

            ai.context = formatContextId( ai );

            result.add( ai );

            if ( result.size() > count )
            {
                break;
            }
        }

        return result;
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

        }
        catch ( NoSuchRepositoryException e )
        {
            // nothing
        }

        return result;
    }

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    public Collection<ArtifactInfo> searchArtifactFlat( String term, String repositoryId, String groupId )
    {
        IndexingContext context = null;

        if ( repositoryId != null )
        {
            context = nexusIndexer.getIndexingContexts().get( getLocalContextId( repositoryId ) );
        }

        try
        {
            Collection<ArtifactInfo> infos;

            Query q1 = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, term );

            Query q2 = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, term );

            BooleanQuery bq = new BooleanQuery();

            bq.add( q1, BooleanClause.Occur.SHOULD );

            bq.add( q2, BooleanClause.Occur.SHOULD );

            if ( context == null )
            {
                infos = nexusIndexer.searchFlat( bq );
            }
            else
            {
                infos = nexusIndexer.searchFlat( bq, context );
            }

            return limitResults( infos, RESULT_LIMIT );
        }
        catch ( IndexContextInInconsistentStateException e )
        {
            getLogger().error( "Inconsistent index context state while searching for query \"" + term + "\"", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Got I/O exception while searching for query \"" + term + "\"", e );
        }
        return Collections.emptyList();
    }

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    public Query constructQuery( String field, String query )
    {
        return nexusIndexer.constructQuery( field, query );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( RepositoryRegistryEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            try
            {
                Repository repository = ( (RepositoryRegistryEvent) evt ).getRepository();

                String remoteRoot = null;
                if ( repository.getRemoteUrl() != null )
                {
                    remoteRoot = repository.getRemoteUrl();
                }

                // we are handling repo events, like addition and removal
                if ( RepositoryRegistryEventAdd.class.isAssignableFrom( evt.getClass() ) )
                {
                    // add context for repository
                    File repoRoot = null;
                    if ( repository.getLocalUrl() != null
                        && repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage )
                    {
                        URL url = new URL( repository.getLocalUrl() );
                        try
                        {
                            repoRoot = new File( url.toURI() );
                        }
                        catch ( URISyntaxException e )
                        {
                            repoRoot = new File( url.getPath() );
                        }
                    }

                    IndexingContext ctxLocal = nexusIndexer.addIndexingContext(
                        getLocalContextId( repository.getId() ),
                        repository.getId(),
                        repoRoot,
                        new File( getWorkingDirectory(), getLocalContextId( repository.getId() ) ),
                        remoteRoot,
                        null,
                        NexusIndexer.FULL_INDEX,
                        true );

                    if ( ctxLocal.getTimestamp() == null )
                    {
                        // it is probably new or first start
                        ctxLocal.updateTimestamp();
                    }

                    if ( RepositoryType.PROXY.equals( repository.getRepositoryType() ) )
                    {
                        IndexingContext ctxRemote = nexusIndexer.addIndexingContext(
                            getRemoteContextId( repository.getId() ),
                            repository.getId(),
                            repoRoot,
                            new File( getWorkingDirectory(), getRemoteContextId( repository.getId() ) ),
                            remoteRoot,
                            remoteRoot,
                            NexusIndexer.FULL_INDEX,
                            true );

                        if ( ctxRemote.getTimestamp() == null )
                        {
                            // it is probably new or first start
                            ctxRemote.updateTimestamp();
                        }
                    }

                }
                else if ( RepositoryRegistryEventRemove.class.isAssignableFrom( evt.getClass() ) )
                {
                    // remove context for repository
                    nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                        getLocalContextId( repository.getId() ) ), true );

                    if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( repository.getId() ) ) )
                    {
                        nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                            getRemoteContextId( repository.getId() ) ), true );
                    }
                }
            }
            catch ( Exception e ) // TODO be more specific
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
                    IndexingContext ctxLocal = nexusIndexer.addIndexingContext(
                        getLocalContextId( gevt.getGroupId() ),
                        gevt.getGroupId(),
                        null,
                        new File( getWorkingDirectory(), getLocalContextId( gevt.getGroupId() ) ),
                        null,
                        null,
                        NexusIndexer.FULL_INDEX,
                        true );

                    IndexingContext ctxRemote = nexusIndexer.addIndexingContext(
                        getRemoteContextId( gevt.getGroupId() ),
                        gevt.getGroupId(),
                        null,
                        new File( getWorkingDirectory(), getRemoteContextId( gevt.getGroupId() ) ),
                        null,
                        null,
                        NexusIndexer.FULL_INDEX,
                        true );

                    List<Repository> members = repositoryRegistry.getRepositoryGroup( gevt.getGroupId() );

                    /*
                     * TODO: NX-463 Commented, to fix bundle issues until we implement this for ( Repository repo :
                     * members ) { if ( nexusIndexer.getIndexingContexts().containsKey( getLocalContextId( repo.getId() ) ) ) {
                     * ctxLocal.merge( nexusIndexer .getIndexingContexts().get( getLocalContextId( repo.getId() )
                     * ).getIndexDirectory() ); } if ( nexusIndexer.getIndexingContexts().containsKey(
                     * getRemoteContextId( repo.getId() ) ) ) { ctxRemote.merge( nexusIndexer
                     * .getIndexingContexts().get( getRemoteContextId( repo.getId() ) ).getIndexDirectory() ); } }
                     */

                    if ( ctxLocal.getTimestamp() == null )
                    {
                        // it is probably new or first start
                        ctxLocal.updateTimestamp();
                    }
                    if ( ctxRemote.getTimestamp() == null )
                    {
                        // it is probably new or first start
                        ctxRemote.updateTimestamp();
                    }

                }
                else if ( RepositoryRegistryGroupEventRemove.class.isAssignableFrom( evt.getClass() ) )
                {
                    // remove context for repository
                    if ( nexusIndexer.getIndexingContexts().containsKey( getLocalContextId( gevt.getGroupId() ) ) )
                    {
                        nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                            getLocalContextId( gevt.getGroupId() ) ), true );
                    }

                    if ( nexusIndexer.getIndexingContexts().containsKey( getRemoteContextId( gevt.getGroupId() ) ) )
                    {
                        nexusIndexer.removeIndexingContext( nexusIndexer.getIndexingContexts().get(
                            getRemoteContextId( gevt.getGroupId() ) ), true );
                    }
                }
            }
            catch ( Exception e ) // TODO be more specific
            {
                getLogger().error( "Could not maintain group indexing contexts!", e );
            }
        }
        else if ( RepositoryItemEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            try
            {
                RepositoryItemEvent ievt = (RepositoryItemEvent) evt;
                // should we sync at all
                if ( ievt.getRepository().isIndexable()
                    && ( RepositoryItemEventStore.class.isAssignableFrom( ievt.getClass() )
                        || RepositoryItemEventCache.class.isAssignableFrom( ievt.getClass() ) || RepositoryItemEventDelete.class
                        .isAssignableFrom( ievt.getClass() ) ) )
                {
                    IndexingContext context = nexusIndexer.getIndexingContexts().get(
                        getLocalContextId( ievt.getRepository().getId() ) );

                    // by calculating GAV we check wether the request is against a repo artifact at all
                    Gav gav = M2GavCalculator.calculate( ievt.getItemUid().getPath() );

                    if ( context != null && gav != null )
                    {
                        // if we have a valid indexing context and have access to a File
                        if ( ievt.getContext().containsKey( DefaultFSLocalRepositoryStorage.FS_FILE ) )
                        {
                            File file = (File) ievt.getContext().get( DefaultFSLocalRepositoryStorage.FS_FILE );

                            if ( file.exists() )
                            {
                                ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

                                if ( ac != null )
                                {
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
}
