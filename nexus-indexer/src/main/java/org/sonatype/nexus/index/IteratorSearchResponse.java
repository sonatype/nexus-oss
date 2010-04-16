package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

public class IteratorSearchResponse
    extends AbstractSearchResponse
{
    private final IteratorResultSet results;

    public IteratorSearchResponse( Query query, int totalHits, IteratorResultSet results )
    {
        super( query, totalHits );

        this.results = results;
    }

    public IteratorResultSet getResults()
    {
        return results;
    }
}
