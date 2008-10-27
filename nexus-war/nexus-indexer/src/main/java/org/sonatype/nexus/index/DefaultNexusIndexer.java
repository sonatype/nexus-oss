/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamas Cservenak (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.creator.IndexerEngine;
import org.sonatype.nexus.index.scan.DefaultScanningRequest;
import org.sonatype.nexus.index.scan.Scanner;
import org.sonatype.nexus.index.scan.ScanningException;
import org.sonatype.nexus.index.scan.ScanningRequest;
import org.sonatype.nexus.index.scan.ScanningResult;
import org.sonatype.nexus.index.search.SearchEngine;

/**
 * The default nexus indexer implementation.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexusIndexer
    extends AbstractLogEnabled
    implements NexusIndexer
{
    
    private static final char[] DIGITS = "0123456789ABCDEF".toCharArray();

    /** @plexus.requirement */
    private Scanner scanner;

    /** @plexus.requirement */
    private SearchEngine searcher;

    /** @plexus.requirement */
    private IndexerEngine indexer;

    /** @plexus.requirement */
    private QueryCreator queryCreator;

    private Map<String, IndexingContext> indexingContexts;

    public DefaultNexusIndexer()
    {
        this.indexingContexts = new ConcurrentHashMap<String, IndexingContext>();
    }

    // ----------------------------------------------------------------------------
    // Contexts
    // ----------------------------------------------------------------------------

    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        IndexingContext context = new DefaultIndexingContext(
            id,
            repositoryId,
            repository,
            indexDirectory,
            repositoryUrl,
            indexUpdateUrl,
            indexers,
            false );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContextForced( String id, String repositoryId, File repository,
        File indexDirectory, String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException
    {
        IndexingContext context = null;

        try
        {
            context = new DefaultIndexingContext(
                id,
                repositoryId,
                repository,
                indexDirectory,
                repositoryUrl,
                indexUpdateUrl,
                indexers,
                true );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            // will not be thrown
        }

        indexingContexts.put( context.getId(), context );

        return context;
    }

    @Deprecated
    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        IndexingContext context = new DefaultIndexingContext(
            id,
            repositoryId,
            repository,
            indexDirectory,
            repositoryUrl,
            indexUpdateUrl,
            indexers,
            reclaimIndexOwnership );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        IndexingContext context = new DefaultIndexingContext(
            id,
            repositoryId,
            repository,
            directory,
            repositoryUrl,
            indexUpdateUrl,
            indexers,
            false );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContextForced( String id, String repositoryId, File repository,
        Directory directory, String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException
    {
        IndexingContext context = null;

        try
        {
            context = new DefaultIndexingContext(
                id,
                repositoryId,
                repository,
                directory,
                repositoryUrl,
                indexUpdateUrl,
                indexers,
                true );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            // will not be thrown
        }

        indexingContexts.put( context.getId(), context );

        return context;
    }

    @Deprecated
    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        IndexingContext context = new DefaultIndexingContext(
            id,
            repositoryId,
            repository,
            directory,
            repositoryUrl,
            indexUpdateUrl,
            indexers,
            reclaimIndexOwnership );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public void removeIndexingContext( IndexingContext context, boolean deleteFiles )
        throws IOException
    {
        if ( indexingContexts.containsKey( context.getId() ) )
        {
            indexingContexts.remove( context.getId() );
            context.close( deleteFiles );
        }
    }

    public Map<String, IndexingContext> getIndexingContexts()
    {
        return Collections.unmodifiableMap( indexingContexts );
    }

    // ----------------------------------------------------------------------------
    // Scanning
    // ----------------------------------------------------------------------------

    public void scan( IndexingContext context )
        throws IOException
    {
        scan( context, null );
    }

    public void scan( IndexingContext context, final ArtifactScanningListener listener )
        throws IOException
    {
        scan( context, null, false );
    }

    public void scan( IndexingContext context, final ArtifactScanningListener listener, boolean update )
        throws IOException
    {
        final HashSet<String> allGroups = new HashSet<String>();

        final HashSet<String> groups = new HashSet<String>();

        HashSet<String> infos = null;

        IndexReader r = context.getIndexReader();

        if ( update )
        {
            infos = new HashSet<String>();

            for ( int i = 0; i < r.numDocs(); i++ )
            {
                if ( !r.isDeleted( i ) )
                {
                    Document d = r.document( i );

                    String uinfo = d.get( ArtifactInfo.UINFO );

                    if ( uinfo != null )
                    {
                        infos.add( uinfo );
                        // add all existing groupIds to the lists, as they will
                        // not be "discovered" and would be missing from the new list..
                        String grId = uinfo.substring( 0, uinfo.indexOf( '|' ) );
                        allGroups.add( grId );
                        int ind = grId.indexOf( '.' );
                        if ( ind > -1 )
                        {
                            grId = grId.substring( 0, ind );
                        }
                        groups.add( grId );
                    }
                }
            }
        }

        File repositoryDirectory = context.getRepository();

        if ( !repositoryDirectory.exists() )
        {
            getLogger().error( "Directory " + repositoryDirectory + " does not exist!" );

            return;
        }

        ArtifactScanningListener groupCollector = new ArtifactScanningListener()
        {
            public void artifactError( ArtifactContext ac, Exception e )
            {
                if ( listener != null )
                {
                    listener.artifactError( ac, e );
                }
            }

            public void scanningFinished( IndexingContext ctx, ScanningResult result )
            {
                if ( listener != null )
                {
                    listener.scanningFinished( ctx, result );
                }
            }

            public void scanningStarted( IndexingContext ctx )
            {
                if ( listener != null )
                {
                    listener.scanningStarted( ctx );
                }
            }

            public void artifactDiscovered( ArtifactContext ac )
            {
                String group = getRootGroup( ac.getArtifactInfo().groupId );
                groups.add( group );
                allGroups.add( ac.getArtifactInfo().groupId );

                if ( listener != null )
                {
                    listener.artifactDiscovered( ac );
                }
            }
        };

        ScanningRequest request = new DefaultScanningRequest( context, groupCollector, this, infos );

        ScanningResult result = null;

        if ( listener != null )
        {
            listener.scanningStarted( context );
        }

        indexer.beginIndexing( context );

        try
        {
            result = scanner.scan( request );

            setRootGroups( context, groups );

            setAllGroups( context, allGroups );

        }
        catch ( ScanningException e )
        {
            getLogger().error( "Error scanning repositoryDirectory for new artifacts.", e );
        }

        indexer.endIndexing( context );

        if ( listener != null )
        {
            listener.scanningFinished( context, result );
        }
    }

    String getRootGroup( String groupId )
    {
        String group = groupId;
        int n = group.indexOf( '.' );
        if ( n > -1 )
        {
            group = group.substring( 0, n );
        }
        return group;
    }

    public void artifactDiscovered( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexer.index( context, ac );
        }
    }

    // ----------------------------------------------------------------------------
    // Modifying
    // ----------------------------------------------------------------------------

    public void addArtifactToIndex( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexer.update( context, ac );

            updateGroups( ac, context );
        }
    }

    private void updateGroups( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        Set<String> groups = getRootGroups( context );
        String rootGroup = getRootGroup( ac.getArtifactInfo().groupId );
        if ( !groups.contains( rootGroup ) )
        {
            groups.add( rootGroup );
            setRootGroups( context, groups );
        }
        groups = getAllGroups( context );
        if ( !groups.contains( ac.getArtifactInfo().groupId ) )
        {
            groups.add( ac.getArtifactInfo().groupId );
            setAllGroups( context, groups );
        }
    }

    public void deleteArtifactFromIndex( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexer.remove( context, ac );
        }
    }

    // ----------------------------------------------------------------------------
    // Root groups
    // ----------------------------------------------------------------------------

    public Set<String> getRootGroups( IndexingContext context )
        throws IOException
    {
        Hits hits = context.getIndexSearcher().search(
            new TermQuery( new Term( ArtifactInfo.ROOT_GROUPS, ArtifactInfo.ROOT_GROUPS_VALUE ) ) );

        Set<String> groups = new LinkedHashSet<String>( Math.max( 10, hits.length() ) );

        if ( hits.length() > 0 )
        {
            Document doc = hits.doc( 0 );

            String groupList = doc.get( ArtifactInfo.ROOT_GROUPS_LIST );

            if ( groupList != null )
            {
                groups.addAll( Arrays.asList( groupList.split( "\\|" ) ) );
            }
        }

        return groups;
    }

    public void setRootGroups( IndexingContext context, Collection<String> groups )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.updateDocument(
            new Term( ArtifactInfo.ROOT_GROUPS, ArtifactInfo.ROOT_GROUPS_VALUE ),
            createRootGroupsDocument( groups ) );

        w.flush();
    }

    private Document createRootGroupsDocument( Collection<String> groups )
    {
        Document groupDoc = new Document();

        groupDoc.add( new Field( ArtifactInfo.ROOT_GROUPS, //
            ArtifactInfo.ROOT_GROUPS_VALUE,
            Field.Store.YES,
            Field.Index.UN_TOKENIZED ) );

        groupDoc.add( new Field( ArtifactInfo.ROOT_GROUPS_LIST, //
            AbstractIndexCreator.lst2str( groups ),
            Field.Store.YES,
            Field.Index.NO ) );

        return groupDoc;
    }

    // ----------------------------------------------------------------------------
    // All groups
    // ----------------------------------------------------------------------------

    public Set<String> getAllGroups( IndexingContext context )
        throws IOException
    {
        Hits hits = context.getIndexSearcher().search(
            new TermQuery( new Term( ArtifactInfo.ALL_GROUPS, ArtifactInfo.ALL_GROUPS_VALUE ) ) );
        Set<String> groups = new LinkedHashSet<String>( Math.max( 10, hits.length() ) );
        if ( hits.length() > 0 )
        {
            Document doc = hits.doc( 0 );

            String groupList = doc.get( ArtifactInfo.ALL_GROUPS_LIST );

            if ( groupList != null )
            {
                groups.addAll( Arrays.asList( groupList.split( "\\|" ) ) );
            }
        }

        return groups;
    }

    public void setAllGroups( IndexingContext context, Collection<String> groups )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.updateDocument(
            new Term( ArtifactInfo.ALL_GROUPS, ArtifactInfo.ALL_GROUPS_VALUE ),
            createAllGroupsDocument( groups ) );

        w.flush();
    }

    private Document createAllGroupsDocument( Collection<String> groups )
    {
        Document groupDoc = new Document();

        groupDoc.add( new Field( ArtifactInfo.ALL_GROUPS, //
            ArtifactInfo.ALL_GROUPS_VALUE,
            Field.Store.YES,
            Field.Index.UN_TOKENIZED ) );

        groupDoc.add( new Field( ArtifactInfo.ALL_GROUPS_LIST, //
            AbstractIndexCreator.lst2str( groups ),
            Field.Store.YES,
            Field.Index.NO ) );

        return groupDoc;
    }

    // ----------------------------------------------------------------------------
    // Groups utils
    // ----------------------------------------------------------------------------

    public void rebuildGroups( IndexingContext context )
        throws IOException
    {
        int numDocs = context.getIndexReader().numDocs();

        for ( int i = 0; i < numDocs; i++ )
        {
            if ( context.getIndexReader().isDeleted( i ) )
            {
                continue;
            }

            Document d = context.getIndexReader().document( i );

            String uinfo = d.get( ArtifactInfo.UINFO );

            if ( uinfo != null )
            {
                try
                {
                    ArtifactInfo info = context.constructArtifactInfo( d );

                    ArtifactContext artifactContext = new ArtifactContext( null, null, null, info, null );

                    updateGroups( artifactContext, context );
                }
                catch ( IndexContextInInconsistentStateException e )
                {
                    getLogger().info(
                        "Unrecognized record with field '" + ArtifactInfo.UINFO + "'='" + uinfo
                            + "' found on index but is not recognized as Nexus Indexer record.",
                        e );
                }
            }
        }

        context.getIndexWriter().optimize();

        context.getIndexWriter().flush();
    }

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    @Deprecated
    public Collection<ArtifactInfo> searchFlat( Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );
    }

    @Deprecated
    public Collection<ArtifactInfo> searchFlat( Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchFlat( ArtifactInfo.VERSION_COMPARATOR, query, context );
    }

    @Deprecated
    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searcher.searchFlat( artifactInfoComparator, indexingContexts.values(), query );
    }

    @Deprecated
    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
        IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searcher.searchFlat( artifactInfoComparator, context, query );
    }

    public FlatSearchResponse searchFlat( FlatSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        if ( request.getContext() == null )
        {
            return searcher.searchFlatPaged( request, indexingContexts.values() );
        }
        else
        {
            return searcher.searchFlatPaged( request );
        }
    }

    @Deprecated
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchGrouped( grouping, String.CASE_INSENSITIVE_ORDER, query );
    }

    @Deprecated
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchGrouped( grouping, String.CASE_INSENSITIVE_ORDER, query, context );
    }

    @Deprecated
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searcher.searchGrouped(
            new GroupedSearchRequest( query, grouping, groupKeyComparator ),
            indexingContexts.values() ).getResults();
    }

    @Deprecated
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searcher
            .searchGrouped( new GroupedSearchRequest( query, grouping, groupKeyComparator, context ) ).getResults();
    }

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        if ( request.getContext() == null )
        {
            return searcher.searchGrouped( request, indexingContexts.values() );
        }
        else
        {
            return searcher.searchGrouped( request );
        }
    }

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    public Query constructQuery( String field, String query )
    {
        return queryCreator.constructQuery( field, query );
    }

    // ----------------------------------------------------------------------------
    // Identification
    // ----------------------------------------------------------------------------

    public ArtifactInfo identify( File artifact )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        FileInputStream is = null;
        
        try
        {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            is = new FileInputStream(artifact);
            
            byte[] buff = new byte[ 4096 ];
            
            int n;
            
            while ( ( n = is.read( buff ) ) > -1 ) 
            {
                sha1.update( buff, 0, n );
            }
            
            byte[] digest = sha1.digest();
            
            // String sha1 = new Sha1Digester().calc( artifact );

            return identify( ArtifactInfo.SHA1, encode( digest ) );
        }
        catch (NoSuchAlgorithmException ex) 
        {
            throw new IOException( "Unable to calculate digest" );
        }
        finally
        {
            IOUtil.close( is );
        }

    }

    private String encode( byte[] digest ) 
    {
        char[] buff = new char[ digest.length * 2 ];
        
        int n = 0;
        
        for (byte b : digest) 
        {
            buff[ n++ ] = DIGITS[ ( 0xF0 & b ) >> 4 ];  
            buff[ n++ ] = DIGITS[ 0x0F & b ];  
        }
        
        return new String( buff );
    }

    public ArtifactInfo identify( String field, String query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return identify( constructQuery( field, query ) );
    }

    public ArtifactInfo identify( Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return identify( query, indexingContexts.values() );
    }

    public ArtifactInfo identify( Query query, Collection<IndexingContext> contexts )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        Set<ArtifactInfo> result = searcher.searchFlatPaged(
            new FlatSearchRequest( query, ArtifactInfo.VERSION_COMPARATOR ),
            indexingContexts.values() ).getResults();

        if ( result.size() == 1 )
        {
            return result.iterator().next();
        }
        return null;
    }

}
