/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
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

    public static final List<? extends IndexCreator> MINIMAL_INDEX =
    // Collections.singletonList(new MinimalArtifactInfoIndexCreator());
    Arrays.asList( new MinimalArtifactInfoIndexCreator() );

    public static final List<? extends IndexCreator> DEFAULT_INDEX = Arrays.asList(
        new MinimalArtifactInfoIndexCreator(),
        new JarFileContentsIndexCreator() );

    public static final List<? extends IndexCreator> FULL_INDEX = Arrays.asList(
        new MinimalArtifactInfoIndexCreator(),
        new JarFileContentsIndexCreator() );

    IndexingContext addIndexingContext( String id, String repositoryId, File repository, File indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    IndexingContext addIndexingContext( String id, String repositoryId, File repository, Directory directory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexers,
        boolean reclaimIndexOwnership )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    void removeIndexingContext( IndexingContext context, boolean deleteFiles )
        throws IOException;

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

    // public void addArtifactToIndex( File pom, IndexingContext context )
    // throws IOException;

    public void addArtifactToIndex( ArtifactContext ac, IndexingContext context )
        throws IOException;

    // public void deleteArtifactFromIndex( File pom, IndexingContext context )
    // throws IOException;

    public void deleteArtifactFromIndex( ArtifactContext ac, IndexingContext context )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    Collection<ArtifactInfo> searchFlat( Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    Collection<ArtifactInfo> searchFlat( Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException;

    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    Collection<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator, Query query,
        IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException;

    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException;

    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Query query, IndexingContext context )
        throws IOException,
            IndexContextInInconsistentStateException;

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    Query constructQuery( String field, String query );

    // ----------------------------------------------------------------------------
    // Identification
    // ----------------------------------------------------------------------------

    ArtifactInfo identify( File artifact )
        throws IOException,
            IndexContextInInconsistentStateException;

    ArtifactInfo identify( String field, String query )
        throws IOException,
            IndexContextInInconsistentStateException;

    ArtifactInfo identify( Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    ArtifactInfo identify( Query query, Collection<IndexingContext> contexts )
        throws IOException,
            IndexContextInInconsistentStateException;

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

}
