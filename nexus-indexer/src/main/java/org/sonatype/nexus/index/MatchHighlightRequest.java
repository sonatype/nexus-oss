package org.sonatype.nexus.index;

import org.apache.lucene.search.Query;

public class MatchHighlightRequest
{
    private final Field field;

    private final Query query;

    private final MatchHighlightMode highlightMode;
    
    private final String highlightSeparator;

    public MatchHighlightRequest( Field field, Query query, MatchHighlightMode highlightMode, String highlightSeparator )
    {
        this.field = field;

        this.query = query;

        this.highlightMode = highlightMode;
        
        this.highlightSeparator = highlightSeparator;
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

    public String getHighlightSeparator()
    {
        return highlightSeparator;
    }
}
