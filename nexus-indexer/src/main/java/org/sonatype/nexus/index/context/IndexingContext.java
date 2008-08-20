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
package org.sonatype.nexus.index.context;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.DocumentFilter;
import org.sonatype.nexus.index.creator.IndexCreator;

/**
 * This is the indexing context.
 * 
 * @author Jason van Zyl
 * @author cstamas
 * @author Eugene Kuleshov
 */
public interface IndexingContext
{
    /**
     * Standard name of the full repository index that is used when clients requesting index information have nothing to
     * start with.
     */
    public static final String INDEX_FILE = "nexus-maven-repository-index";

    public static final String INDEX_ID = "nexus.index.id";

    public static final String INDEX_TIMESTAMP = "nexus.index.time";

    public static final String INDEX_TIME_FORMAT = "yyyyMMddHHmmss.SSS Z";

    /**
     * Returns this indexing context id.
     */
    String getId();

    /**
     * Returns repository id.
     */
    String getRepositoryId();

    /**
     * Returns location for the local repository.
     */
    File getRepository();

    /**
     * Sets the location of the local repository.
     * 
     * @param repository
     */
    void setRepository( File repository );

    /**
     * Returns public repository url.
     */
    String getRepositoryUrl();

    /**
     * Returns url for the index update
     */
    String getIndexUpdateUrl();

    /**
     * Is the context searchable when doing "non-targeted" searches? Ie. Should it take a part when searching without
     * specifying context?
     * 
     * @return
     */
    boolean isSearchable();

    /**
     * Sets is the context searchable when doing "non-targeted" searches.
     * 
     * @param searchable
     */
    void setSearchable( boolean searchable );

    /**
     * Returns index update time
     */
    Date getTimestamp();

    void updateTimestamp()
        throws IOException;

    /**
     * Returns the Lucene IndexReader of this context.
     * 
     * @return reader
     * @throws IOException
     */
    IndexReader getIndexReader()
        throws IOException;

    /**
     * Returns the Lucene IndexSearcher of this context.
     * 
     * @return searcher
     * @throws IOException
     */
    IndexSearcher getIndexSearcher()
        throws IOException;

    /**
     * Returns the Lucene IndexWriter of this context.
     * 
     * @return indexWriter
     * @throws IOException
     */
    IndexWriter getIndexWriter()
        throws IOException;

    /**
     * List of IndexCreators used in this context.
     * 
     * @return list of index creators.
     */
    List<IndexCreator> getIndexCreators();

    /**
     * Returns the Lucene Analyzer of this context used for by IndexWriter and IndexSearcher.
     * 
     * @return
     */
    Analyzer getAnalyzer();

    /**
     * Constructs an artifacts infos for a Lucene document, probably that came from Hits as a search result.
     * 
     * @param doc
     * @return
     */
    @Deprecated
    ArtifactInfo constructArtifactInfo( IndexingContext ctx, Document doc )
        throws IndexContextInInconsistentStateException;

    /**
     * Constructs an artifacts infos for a Lucene document, probably that came from Hits as a search result.
     * 
     * @param doc
     * @return
     */
    ArtifactInfo constructArtifactInfo( Document doc )
        throws IndexContextInInconsistentStateException;

    /**
     * Shuts down this context.
     */
    void close( boolean deleteFiles )
        throws IOException;

    /**
     * Purge (cleans) the context, deletes/empties the index and restores the context to new/empty state.
     * 
     * @throws IOException
     */
    void purge()
        throws IOException;

    /**
     * Merges content of given Lucene directory with this context.
     * 
     * @param directory - the directory to merge
     */
    void merge( Directory directory )
        throws IOException;

    /**
     * Merges content of given Lucene directory with this context, but filters out the unwanted ones.
     * 
     * @param directory - the directory to merge
     */
    void merge( Directory directory, DocumentFilter filter )
        throws IOException;

    /**
     * Replaces the Lucene index with the one from supplied directory.
     * 
     * @param directory
     * @throws IOException
     */
    void replace( Directory directory )
        throws IOException;

    /**
     * Filter the context with supplied ArtifactInfoFilter.
     * 
     * @throws IOException
     */
    void filter( ArtifactInfoFilter filter )
        throws IOException;

    Directory getIndexDirectory();

    File getIndexDirectoryFile();

    /**
     * Returns current identifier in context.
     * 
     * @return id
     */
    // String getCurrentId();
    /**
     * Map for contextual data.
     * 
     * @return data
     */
    // Map<String, Object> getData();
}
