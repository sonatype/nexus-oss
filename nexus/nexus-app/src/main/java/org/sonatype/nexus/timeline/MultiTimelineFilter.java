package org.sonatype.nexus.timeline;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiTimelineFilter
    implements TimelineFilter
{
    private final List<TimelineFilter> terms;

    public MultiTimelineFilter()
    {
        this.terms = new ArrayList<TimelineFilter>();
    }

    public MultiTimelineFilter( List<TimelineFilter> terms )
    {
        this.terms = terms;
    }

    public void addTerm( TimelineFilter filter )
    {
        terms.add( filter );
    }

    protected List<TimelineFilter> getTerms()
    {
        return terms;
    }
}
