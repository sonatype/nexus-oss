package org.sonatype.nexus.index;

public class MatchHighlight
{
    private final Field field;

    private final String highlightedMatch;

    public MatchHighlight( Field field, String highlightedMatch )
    {
        this.field = field;

        this.highlightedMatch = highlightedMatch;
    }

    public Field getField()
    {
        return field;
    }

    public String getHighlightedMatch()
    {
        return highlightedMatch;
    }
}
