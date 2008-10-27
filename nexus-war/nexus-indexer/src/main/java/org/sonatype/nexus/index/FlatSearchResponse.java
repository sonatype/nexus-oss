package org.sonatype.nexus.index;

import java.util.Set;

import org.apache.lucene.search.Query;

public class FlatSearchResponse
{
    private final Query query;

    private final int totalHits;

    private final Set<ArtifactInfo> results;

    public FlatSearchResponse( Query query, int totalHits, Set<ArtifactInfo> results )
    {
        super();

        this.query = query;

        this.totalHits = totalHits;

        this.results = results;
    }

    public Query getQuery()
    {
        return query;
    }

    public int getTotalHits()
    {
        return totalHits;
    }

    public Set<ArtifactInfo> getResults()
    {
        return results;
    }

}
