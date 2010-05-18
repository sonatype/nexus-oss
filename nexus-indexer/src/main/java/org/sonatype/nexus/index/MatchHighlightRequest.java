package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

public class MatchHighlightRequest
{
    private final Field field;

    private final Query query;

    private final MatchHighlightMode highlightMode;

    public MatchHighlightRequest( Field field, Query query, MatchHighlightMode highlightMode )
    {
        this.field = field;

        this.query = query;

        this.highlightMode = highlightMode;
    }

    public Field getField()
    {
        return field;
    }

    public Query getQuery()
    {
        return query;
    }

    public MatchHighlightMode getHighlightMode()
    {
        return highlightMode;
    }
}
