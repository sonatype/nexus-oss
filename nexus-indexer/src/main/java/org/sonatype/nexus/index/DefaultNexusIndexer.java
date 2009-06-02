/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

/**
 * A default {@link NexusIndexer} implementation.
 * 
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 */
@Component( role = NexusIndexer.class )
public class DefaultNexusIndexer
    extends AbstractLogEnabled
    implements NexusIndexer
{
    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    @Requirement
    private Scanner scanner;

    @Requirement
    private SearchEngine searcher;

    @Requirement
    private IndexerEngine indexerEngine;

    @Requirement
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
                                               String repositoryUrl, String indexUpdateUrl,
                                               List<? extends IndexCreator> indexers )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        IndexingContext context =
            new DefaultIndexingContext( id, repositoryId, repository, indexDirectory, repositoryUrl, indexUpdateUrl,
                                        indexers, false );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContextForced( String id, String repositoryId, File repository,
                                                     File indexDirectory, String repositoryUrl, String indexUpdateUrl,
                                                     List<? extends IndexCreator> indexers )
        throws IOException
    {
        IndexingContext context = null;

        try
        {
            context =
                new DefaultIndexingContext( id, repositoryId, repository, indexDirectory, repositoryUrl,
                                            indexUpdateUrl, indexers, true );

            indexingContexts.put( context.getId(), context );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            // will not be thrown
        }

        return context;
    }

    @Deprecated
    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
                                               String repositoryUrl, String indexUpdateUrl,
                                               List<? extends IndexCreator> indexers, boolean reclaimIndexOwnership )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        IndexingContext context =
            new DefaultIndexingContext( id, repositoryId, repository, indexDirectory, repositoryUrl, indexUpdateUrl,
                                        indexers, reclaimIndexOwnership );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
                                               String repositoryUrl, String indexUpdateUrl,
                                               List<? extends IndexCreator> indexers )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        IndexingContext context =
            new DefaultIndexingContext( id, repositoryId, repository, directory, repositoryUrl, indexUpdateUrl,
                                        indexers, false );

        indexingContexts.put( context.getId(), context );

        return context;
    }

    public IndexingContext addIndexingContextForced( String id, String repositoryId, File repository,
                                                     Directory directory, String repositoryUrl, String indexUpdateUrl,
                                                     List<? extends IndexCreator> indexers )
        throws IOException
    {
        IndexingContext context = null;

        try
        {
            context =
                new DefaultIndexingContext( id, repositoryId, repository, directory, repositoryUrl, indexUpdateUrl,
                                            indexers, true );

            indexingContexts.put( context.getId(), context );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            // will not be thrown
        }

        return context;
    }

    @Deprecated
    public IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
                                               String repositoryUrl, String indexUpdateUrl,
                                               List<? extends IndexCreator> indexers, boolean reclaimIndexOwnership )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        IndexingContext context =
            new DefaultIndexingContext( id, repositoryId, repository, directory, repositoryUrl, indexUpdateUrl,
                                        indexers, reclaimIndexOwnership );

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

    public void scan( IndexingContext context, boolean update )
        throws IOException
    {
        scan( context, null, update );
    }

    public void scan( IndexingContext context, final ArtifactScanningListener listener )
        throws IOException
    {
        scan( context, listener, false );
    }

    /**
     * Uses {@link Scanner} to scan repository content. A {@link ArtifactScanningListener} is used to process found
     * artifacts and to add them to the index using
     * {@link NexusIndexer#artifactDiscovered(ArtifactContext, IndexingContext)}.
     * 
     * @see DefaultScannerListener
     * @see #artifactDiscovered(ArtifactContext, IndexingContext)
     */
    public void scan( IndexingContext context, final ArtifactScanningListener listener, boolean update )
        throws IOException
    {
        File repositoryDirectory = context.getRepository();

        if ( !repositoryDirectory.exists() )
        {
            throw new IOException( "Repository directory " + repositoryDirectory + " does not exist" );
        }

        // always use temporary context when reindexing
        File indexDir = context.getIndexDirectoryFile();
        File dir = null;
        if ( indexDir != null )
        {
            dir = indexDir.getParentFile();
        }

        File tmpFile = File.createTempFile( context.getId() + "-tmp", "", dir );
        File tmpDir = new File( tmpFile.getParentFile(), tmpFile.getName() + ".dir" );
        if ( !tmpDir.mkdirs() )
        {
            throw new IOException( "Cannot create temporary directory: " + tmpDir );
        }

        IndexingContext tmpContext = null;
        try
        {
            FSDirectory directory = FSDirectory.getDirectory( tmpDir );

            if ( update )
            {
                Directory.copy( context.getIndexDirectory(), directory, false );
            }

            tmpContext = new DefaultIndexingContext( context.getId() + "-tmp", //
                                                     context.getRepositoryId(), //
                                                     context.getRepository(), //
                                                     directory, //
                                                     context.getRepositoryUrl(), // 
                                                     context.getIndexUpdateUrl(), //
                                                     context.getIndexCreators(), //
                                                     true );

            scanner
                .scan( new ScanningRequest( tmpContext, //
                                            new DefaultScannerListener( tmpContext, indexerEngine, update, listener ) ) );

            tmpContext.updateTimestamp( true );
            context.replace( tmpContext.getIndexDirectory() );

            removeIndexingContext( tmpContext, true );
        }
        catch ( Exception ex )
        {
            getLogger().warn( "Error scanning context: " + context.getId(), ex );
            throw new IOException( "Error scanning context " + context.getId() + ": " + ex.getMessage() );
        }
        finally
        {
            if ( tmpContext != null )
            {
                tmpContext.close( true );
            }

            if ( tmpFile.exists() )
            {
                tmpFile.delete();
            }

            FileUtils.deleteDirectory( tmpDir );
        }

    }

    /**
     * Delegates to the {@link IndexerEngine} to add a new artifact to the index
     */
    public void artifactDiscovered( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexerEngine.index( context, ac );
        }
    }

    // ----------------------------------------------------------------------------
    // Modifying
    // ----------------------------------------------------------------------------

    /**
     * Delegates to the {@link IndexerEngine} to update artifact to the index
     */
    public void addArtifactToIndex( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexerEngine.update( context, ac );
        }
    }

    /**
     * Delegates to the {@link IndexerEngine} to remove artifact from the index
     */
    public void deleteArtifactFromIndex( ArtifactContext ac, IndexingContext context )
        throws IOException
    {
        if ( ac != null )
        {
            indexerEngine.remove( context, ac );
        }
    }

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    /**
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */
    public Collection<ArtifactInfo> searchFlat( Query query )
        throws IOException
    {
        return searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );
    }

    /**
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */
    public Collection<ArtifactInfo> searchFlat( Query query, IndexingContext context )
        throws IOException
    {
        return searchFlat( ArtifactInfo.VERSION_COMPARATOR, query, context );
    }

    /**
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */
    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException
    {
        return searcher.searchFlat( artifactInfoComparator, indexingContexts.values(), query );
    }

    /**
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */
    public Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
                                                IndexingContext context )
        throws IOException
    {
        return searcher.searchFlat( artifactInfoComparator, context, query );
    }

    public FlatSearchResponse searchFlat( FlatSearchRequest request )
        throws IOException
    {
        if ( request.getContexts().isEmpty() )
        {
            return searcher.searchFlatPaged( request, indexingContexts.values() );
        }
        else
        {
            return searcher.searchFlatPaged( request, request.getContexts() );
        }
    }

    /**
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)

     */
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query )
        throws IOException
    {
        return searchGrouped( grouping, String.CASE_INSENSITIVE_ORDER, query );
    }

    /**
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)

     */
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query, IndexingContext context )
        throws IOException
    {
        return searchGrouped( grouping, String.CASE_INSENSITIVE_ORDER, query, context );
    }

    /**
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)

     */
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
                                                         Query query )
        throws IOException
    {
        return searcher.searchGrouped( new GroupedSearchRequest( query, grouping, groupKeyComparator ),
                                       indexingContexts.values() ).getResults();
    }

    /**
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)

     */
    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
                                                         Query query, IndexingContext context )
        throws IOException
    {
        return searcher.searchGrouped( new GroupedSearchRequest( query, grouping, groupKeyComparator ),
                                       Arrays.asList( new IndexingContext[] { context } ) ).getResults();
    }

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException
    {
        if ( request.getContexts().isEmpty() )
        {
            // search all
            return searcher.searchGrouped( request, indexingContexts.values() );
        }
        else
        {
            // search targeted
            return searcher.searchGrouped( request, request.getContexts() );
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
        throws IOException
    {
        FileInputStream is = null;

        try
        {
            MessageDigest sha1 = MessageDigest.getInstance( "SHA-1" );

            is = new FileInputStream( artifact );

            byte[] buff = new byte[4096];

            int n;

            while ( ( n = is.read( buff ) ) > -1 )
            {
                sha1.update( buff, 0, n );
            }

            byte[] digest = sha1.digest();

            // String sha1 = new Sha1Digester().calc( artifact );

            return identify( ArtifactInfo.SHA1, encode( digest ) );
        }
        catch ( NoSuchAlgorithmException ex )
        {
            throw new IOException( "Unable to calculate digest" );
        }
        finally
        {
            IOUtil.close( is );
        }

    }

    private static String encode( byte[] digest )
    {
        char[] buff = new char[digest.length * 2];

        int n = 0;

        for ( byte b : digest )
        {
            buff[n++] = DIGITS[( 0xF0 & b ) >> 4];
            buff[n++] = DIGITS[0x0F & b];
        }

        return new String( buff );
    }

    public ArtifactInfo identify( String field, String query )
        throws IOException
    {
        return identify( new TermQuery( new Term( field, query ) ) );
    }

    public ArtifactInfo identify( Query query )
        throws IOException
    {
        return identify( query, indexingContexts.values() );
    }

    public ArtifactInfo identify( Query query, Collection<IndexingContext> contexts )
        throws IOException
    {
        Set<ArtifactInfo> result =
            searcher.searchFlatPaged( new FlatSearchRequest( query, ArtifactInfo.VERSION_COMPARATOR ), contexts )
                .getResults();

        if ( result.size() == 1 )
        {
            return result.iterator().next();
        }
        return null;
    }

}
