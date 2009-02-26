/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.sonatype.nexus.index.cli.NexusIndexerCli;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.updater.IndexUpdater;

/**
 * The Nexus indexer is a statefull facade that maintains state of indexing
 * contexts.
 * 
 * <p>
 * The following code snippet shows how to register indexing context, which
 * should be done once on the application startup and Nexus indexer instance
 * should be reused after that.
 * 
 * <pre>
 * NexusIndexer indexer;
 * 
 * IndexingContext context = indexer.addIndexingContext(indexId, // index id (usually the same as repository id)
 *     repositoryId, // repository id
 *     directory, // Lucene directory where index is stored
 *     repositoryDir, // local repository dir or null for remote repo
 *     repositoryUrl, // repository url, used by index updater
 *     indexUpdateUrl, // index update url or null if derived from repositoryUrl
 *     false, false);
 * </pre>
 * 
 * An indexing context could be populated using one of
 * {@link #scan(IndexingContext)},
 * {@link #addArtifactToIndex(ArtifactContext, IndexingContext)} or
 * {@link #deleteArtifactFromIndex(ArtifactContext, IndexingContext)} methods.
 * 
 * <p>
 * An {@link IndexUpdater} could be used to fetch indexes from remote repositories.
 * These indexers could be created using the {@link NexusIndexerCli} command line tool
 * or {@link IndexPacker} API.  
 * 
 * <p>
 * Once index is populated you can perform search queries using field names
 * declared in the {@link ArtifactInfo}:
 * 
 * <pre>
 *   // run search query
 *   BooleanQuery q = new BooleanQuery();
 *   q.add(indexer.constructQuery(ArtifactInfo.GROUP_ID, term), Occur.SHOULD);
 *   q.add(indexer.constructQuery(ArtifactInfo.ARTIFACT_ID, term), Occur.SHOULD);
 *   q.add(new PrefixQuery(new Term(ArtifactInfo.SHA1, term)), Occur.SHOULD);
 *   
 *   FlatSearchRequest request = new FlatSearchRequest(q);
 *   FlatSearchResponse response = indexer.searchFlat(request);
 *   ...
 * </pre>
 * 
 * Query could be also constructed using a convenience
 * {@link NexusIndexer#constructQuery(String, String)} method that handles
 * creation of the wildcard queries. Also see {@link DefaultQueryCreator} for
 * more details on supported queries.
 * 
 * @see IndexingContext
 * @see IndexUpdater
 * @see DefaultQueryCreator
 * 
 * @author Jason van Zyl
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 */
public interface NexusIndexer
{
    String ROLE = NexusIndexer.class.getName();

    // ----------------------------------------------------------------------------
    // Contexts
    // ----------------------------------------------------------------------------

    /**
     * The minimal set of index creators.
     */
    public static final List<? extends IndexCreator> MINIMAL_INDEX =
        Arrays.asList( new MinimalArtifactInfoIndexCreator() );
        // Collections.singletonList(new MinimalArtifactInfoIndexCreator());
    /**
     * The full set of index creators.
     */
    public static final List<? extends IndexCreator> FULL_INDEX = Arrays.<IndexCreator>asList(
        new MinimalArtifactInfoIndexCreator(),
        new JarFileContentsIndexCreator() );
    /**
     * The "default" set of index creators. It adds Jar contents (classes) to minimal index.
     */
    public static final List<? extends IndexCreator> DEFAULT_INDEX = FULL_INDEX;
    
    /**
     * Adds an indexing context to Nexus indexer.
     * 
     * @param id the ID of the context.
     * @param repositoryId the ID of the repository that this context represents.
     * @param repository the location of the repository.
     * @param indexDirectory the location of the Lucene indexes.
     * @param repositoryUrl the location of the remote repository.
     * @param indexUpdateUrl the alternate location of the remote repository indexes (if they are not in default place).
     * @param indexers the set of indexers to apply to this context.
     * @return
     * @throws IOException in case of some serious IO problem.
     * @throws UnsupportedExistingLuceneIndexException if a Lucene index already exists where location is specified, but
     *         it has no Nexus descriptor record or it has, but the embedded repoId differs from the repoId specified
     *         from the supplied one.
     */
    IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * Adds an indexing context to Nexus indexer. It "forces" this operation, thus no
     * UnsupportedExistingLuceneIndexException is thrown. If it founds an existing lucene index, it will simply
     * stomp-over and rewrite (or add) the Nexus index descriptor.
     * 
     * @param id the ID of the context.
     * @param repositoryId the ID of the repository that this context represents.
     * @param repository the location of the repository.
     * @param indexDirectory the location of the Lucene indexes.
     * @param repositoryUrl the location of the remote repository.
     * @param indexUpdateUrl the alternate location of the remote repository indexes (if they are not in default place).
     * @param indexers the set of indexers to apply to this context.
     * @return
     * @throws IOException in case of some serious IO problem.
     */
    IndexingContext addIndexingContextForced( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException;

    /**
     * @deprecated use {@link #addIndexingContext(String, String, File, Directory, String, String, List)} 
     */
    IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * Adds an indexing context to Nexus indexer.
     * 
     * @param id the ID of the context.
     * @param repositoryId the ID of the repository that this context represents.
     * @param repository the location of the repository.
     * @param directory the location of the Lucene indexes.
     * @param repositoryUrl the location of the remote repository.
     * @param indexUpdateUrl the alternate location of the remote repository indexes (if they are not in default place).
     * @param indexers the set of indexers to apply to this context.
     * @return
     * @throws IOException in case of some serious IO problem.
     * @throws UnsupportedExistingLuceneIndexException if a Lucene index already exists where location is specified, but
     *         it has no Nexus descriptor record or it has, but the embedded repoId differs from the repoId specified
     *         from the supplied one.
     */
    IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * Adds an indexing context to Nexus indexer. It "forces" this operation, thus no
     * UnsupportedExistingLuceneIndexException is thrown. If it founds an existing lucene index, it will simply
     * stomp-over and rewrite (or add) the Nexus index descriptor.
     * 
     * @param id the ID of the context.
     * @param repositoryId the ID of the repository that this context represents.
     * @param repository the location of the repository.
     * @param directory the location of the Lucene indexes.
     * @param repositoryUrl the location of the remote repository.
     * @param indexUpdateUrl the alternate location of the remote repository indexes (if they are not in default place).
     * @param indexers the set of indexers to apply to this context.
     * @return
     * @throws IOException in case of some serious IO problem.
     */
    IndexingContext addIndexingContextForced( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers )
        throws IOException;

    /**
     * @deprecated use {@link #addIndexingContext(String, String, File, Directory, String, String, List)} instead
     */
    IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * Removes the indexing context from Nexus indexer, closes it and deletes (if specified) the index files.
     * 
     * @param context
     * @param deleteFiles
     * @throws IOException
     */
    void removeIndexingContext( IndexingContext context, boolean deleteFiles )
        throws IOException;

    /**
     * Returns the map of indexing contexts keyed by their ID.
     */
    Map<String, IndexingContext> getIndexingContexts();

    // ----------------------------------------------------------------------------
    // Scanning
    // ----------------------------------------------------------------------------

    /**
     * Performs full scan (reindex) for the local repository
     */
    void scan( IndexingContext context )
        throws IOException;

    /**
     * Performs full scan (reindex) for the local repository
     */
    void scan( IndexingContext context, ArtifactScanningListener listener )
        throws IOException;

    /**
     * Performs optionally incremental scan (reindex) for the local repository
     */
    void scan( IndexingContext context, ArtifactScanningListener listener, boolean update )
        throws IOException;

    void artifactDiscovered( ArtifactContext ac, IndexingContext context )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Modifying
    // ----------------------------------------------------------------------------

    public void addArtifactToIndex( ArtifactContext ac, IndexingContext context )
        throws IOException;

    public void deleteArtifactFromIndex( ArtifactContext ac, IndexingContext context )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    /**
     * Searches all searchable contexts know to Nexus indexer and merge the results. The default comparator will be
     * used (VersionComparator) to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead 
     */
    Collection<ArtifactInfo> searchFlat( Query query )
        throws IOException;

    /**
     * Will search the given context. The default comparator will be used (VersionComparator) to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */ 
    Collection<ArtifactInfo> searchFlat( Query query, IndexingContext context )
        throws IOException;

    /**
     * Searches all searchable contexts know to Nexus indexer and merge the results. The given comparator will be
     * used to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */ 
    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException;

    /**
     * Searches the given context. The given comparator will be used to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */ 
    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
        IndexingContext context )
        throws IOException;

    /**
     * Searches according the request parameters.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    FlatSearchResponse searchFlat( FlatSearchRequest request )
        throws IOException;

    /**
     * Searches all searchable contexts know to Nexus indexer and merge the results.
     * 
     * @param grouping
     * @param query
     * @return
     * @throws IOException
     * 
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)
     */
    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query )
        throws IOException;

    /**
     * Searches the given context.
     * 
     * @param grouping
     * @param query
     * @param context
     * @return
     * @throws IOException
     * 
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)
     */
    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query, IndexingContext context )
        throws IOException;

    /**
     * Searches all searchable contexts know to Nexus indexer and merge the results.
     * 
     * @param grouping
     * @param groupKeyComparator
     * @param query
     * @return
     * @throws IOException
     * 
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)
     */
    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator, Query query )
        throws IOException;

    /**
     * Searches the given context.
     * 
     * @param grouping
     * @param groupKeyComparator
     * @param query
     * @param context
     * @return
     * @throws IOException
     * 
     * @deprecated use {@link #searchGrouped(GroupedSearchRequest)
     */
    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query, IndexingContext context )
        throws IOException;

    /**
     * Searches according the request parameters.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    /**
     * A convenience method to construct Lucene query for given field name and query text.
     * 
     * @param field a field name, one of the fields declared in {@link ArtifactInfo}
     * @param query a query text
     * @see DefaultQueryCreator
     */
    Query constructQuery( String field, String query );

    // ----------------------------------------------------------------------------
    // Identification
    // ----------------------------------------------------------------------------

    ArtifactInfo identify( File artifact )
        throws IOException;

    ArtifactInfo identify( String field, String query )
        throws IOException;

    ArtifactInfo identify( Query query )
        throws IOException;

    ArtifactInfo identify( Query query, Collection<IndexingContext> contexts )
        throws IOException;

}
