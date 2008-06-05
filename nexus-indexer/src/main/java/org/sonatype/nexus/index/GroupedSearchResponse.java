package org.sonatype.nexus.index;

import java.util.Map;

import org.apache.lucene.search.Query;

public class GroupedSearchResponse
{
    private final Query query;

    private final int totalHits;

    private final Map<String, ArtifactInfoGroup> results;

    public GroupedSearchResponse( Query query, int totalHits, Map<String, ArtifactInfoGroup> results )
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

    public Map<String, ArtifactInfoGroup> getResults()
    {
        return results;
    }

}
