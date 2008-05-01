/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
     * Returns public repository url.
     */
    String getRepositoryUrl();

    /**
     * Returns url for the index update
     */
    String getIndexUpdateUrl();

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
    ArtifactInfo constructArtifactInfo( IndexingContext ctx, Document doc )
        throws IndexContextInInconsistentStateException;

    /**
     * Sets current identifier in context.
     * 
     * @param id
     */
    // void setCurrentId( String id );
    /**
     * Shuts down this context.
     */
    void close( boolean deleteFiles )
        throws IOException;

    /**
     * Merges content of given Lucene directory with this context.
     * 
     * @param directory - the directory to merge
     */
    void merge( Directory directory )
        throws IOException;

    void replace( Directory directory )
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
