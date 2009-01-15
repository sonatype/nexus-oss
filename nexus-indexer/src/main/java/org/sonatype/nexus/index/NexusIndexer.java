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
import java.util.Set;

import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;

/**
 * The Nexus indexer interface.
 * 
 * @author Jason van Zyl
 * @author cstamas
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
    public static final List<? extends IndexCreator> FULL_INDEX = Arrays.asList(
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

    void scan( IndexingContext context )
        throws IOException;

    void scan( IndexingContext context, ArtifactScanningListener listener )
        throws IOException;

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
     * Will search all searchable contexts know to Nexus indexer and merge the results. The default comparator will be
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
     * Will search all searchable contexts know to Nexus indexer and merge the results. The given comparator will be
     * used to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */ 
    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException;

    /**
     * Will search the given context. The given comparator will be used to sort the results.
     * 
     * @deprecated use {@link #searchFlat(FlatSearchRequest)} instead
     */ 
    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
        IndexingContext context )
        throws IOException;

    /**
     * Will search according the request parameters.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    FlatSearchResponse searchFlat( FlatSearchRequest request )
        throws IOException;

    /**
     * Will search all searchable contexts know to Nexus indexer and merge the results.
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
     * Will search the given context.
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
     * Will search all searchable contexts know to Nexus indexer and merge the results.
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
     * Will search the given context.
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
     * Will search according the request parameters.
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

    // ----------------------------------------------------------------------------
    // Root groups
    // ----------------------------------------------------------------------------

    Set<String> getRootGroups( IndexingContext context )
        throws IOException;

    void setRootGroups( IndexingContext context, Collection<String> groups )
        throws IOException;

    // ----------------------------------------------------------------------------
    // All groups
    // ----------------------------------------------------------------------------

    Set<String> getAllGroups( IndexingContext context )
        throws IOException;

    void setAllGroups( IndexingContext context, Collection<String> groups )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Groups utils
    // ----------------------------------------------------------------------------

    /**
     * Used to rebuild group information, for example on context which were merged, since merge() of contexts does sucks
     * only the Documents with UINFO record (Artifacts). This method may be used by IDE integrations also, since the
     * grouping feature is not used in Nexus Server.
     */
    void rebuildGroups( IndexingContext context )
        throws IOException;

}
