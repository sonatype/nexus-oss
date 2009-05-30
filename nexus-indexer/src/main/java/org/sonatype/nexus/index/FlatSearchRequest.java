/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Comparator;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A flat search request.
 *
 * @see NexusIndexer#searchFlat(FlatSearchRequest)
 */
public class FlatSearchRequest
{
    public static int UNDEFINED = -1;

    private Query query;

    private Comparator<ArtifactInfo> artifactInfoComparator;

    private IndexingContext[] contexts;

    private int start;

    private int aiCount;

    public FlatSearchRequest( Query query )
    {
        super();

        this.query = query;

        this.artifactInfoComparator = ArtifactInfo.VERSION_COMPARATOR;

        this.contexts = null;

        this.start = UNDEFINED;

        this.aiCount = UNDEFINED;
    }

    public FlatSearchRequest( Query query, Comparator<ArtifactInfo> artifactInfoComparator )
    {
        this( query );

        this.artifactInfoComparator = artifactInfoComparator;
    }

    public FlatSearchRequest( Query query, IndexingContext... context )
    {
        this( query );

        this.contexts = context;
    }

    public FlatSearchRequest( Query query, Comparator<ArtifactInfo> artifactInfoComparator, IndexingContext... context )
    {
        this( query, artifactInfoComparator );

        this.contexts = context;
    }

    public Query getQuery()
    {
        return query;
    }

    public Comparator<ArtifactInfo> getArtifactInfoComparator()
    {
        return artifactInfoComparator;
    }

    public IndexingContext[] getContexts()
    {
        return contexts;
    }

    public int getStart()
    {
        return start;
    }

    public int getAiCount()
    {
        return aiCount;
    }

    public void setStart( int start )
    {
        this.start = start;
    }

    public void setAiCount( int aiCount )
    {
        this.aiCount = aiCount;
    }

}
