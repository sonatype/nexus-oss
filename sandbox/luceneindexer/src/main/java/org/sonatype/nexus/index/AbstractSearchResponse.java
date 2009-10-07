package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

public class AbstractSearchResponse
{
    public static int LIMIT_EXCEEDED = -1;

    private final Query query;

    private final int totalHits;

    public AbstractSearchResponse( Query query, int totalHits )
    {
        this.query = query;

        this.totalHits = totalHits;
    }

    public Query getQuery()
    {
        return query;
    }

    public int getTotalHits()
    {
        return totalHits;
    }

    public boolean isHitLimitExceeded()
    {
        return getTotalHits() == LIMIT_EXCEEDED;
    }
}
