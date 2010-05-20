package org.sonatype.nexus.index;

import java.util.List;

public class MatchHighlight
{
    private final Field field;

    private final List<String> highlightedMatch;

    public MatchHighlight( Field field, List<String> highlightedMatch )
    {
        this.field = field;

        this.highlightedMatch = highlightedMatch;
    }

    public Field getField()
    {
        return field;
    }

    public List<String> getHighlightedMatch()
    {
        return highlightedMatch;
    }
}
