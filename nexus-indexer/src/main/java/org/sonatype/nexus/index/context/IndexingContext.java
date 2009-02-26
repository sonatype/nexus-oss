/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.sonatype.nexus.artifact.GavCalculator;

/**
 * An indexing context is representing artifact repository for indexing and searching.
 * Indexing context is a statefull component, it keeps state of index readers 
 * and writers. 
 * 
 * @author Jason van Zyl
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 */
public interface IndexingContext
{
    /**
     * Standard name of the full repository index that is used when clients requesting index information have nothing to
     * start with.
     */
    public static final String INDEX_FILE = "nexus-maven-repository-index";

    /**
     * A prefix used for all index property names
     */
    public static final String INDEX_PROPERTY_PREFIX = "nexus.index.";

    /**
     * A property name used to specify index id
     */
    public static final String INDEX_ID = INDEX_PROPERTY_PREFIX + "id";

    /**
     * A property name used to specify index timestam (the last update time)
     */
    public static final String INDEX_TIMESTAMP = INDEX_PROPERTY_PREFIX + "time";

    /**
     * A prefix used to specify an incremental update chunk name
     */
    public static final String INDEX_CHUNK_PREFIX = INDEX_PROPERTY_PREFIX + "update-";

    /**
     * A date format used for index timestamp
     */
    public static final String INDEX_TIME_FORMAT = "yyyyMMddHHmmss.SSS Z";

    /**
     * A date format used for incremental update chunk names
     */
    public static final String INDEX_TIME_DAY_FORMAT = "yyyyMMdd";

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

    void updateTimestamp( boolean save )
        throws IOException;

    void updateTimestamp( boolean save, Date date )
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
     * Optimizes index
     */
    void optimize()
        throws CorruptIndexException,
            IOException;

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

    Directory getIndexDirectory();

    File getIndexDirectoryFile();

    /**
     * Returns the GavCalculator for this Context. Implies repository layout.
     */
    GavCalculator getGavCalculator();

    /**
     * Sets all group names stored in the current indexing context
     */
    void setAllGroups( Collection<String> groups )
        throws IOException;

    /**
     * Gets all group names stored in the current indexing context
     */
    Set<String> getAllGroups()
        throws IOException;

    /**
     * Sets root group names stored in the current indexing context
     */
    void setRootGroups( Collection<String> groups )
        throws IOException;

    /**
     * Gets root group names stored in the current indexing context
     */
    Set<String> getRootGroups()
        throws IOException;

    /**
     * Rebuilds stored group names from the index
     */
    void rebuildGroups()
        throws IOException;

}
