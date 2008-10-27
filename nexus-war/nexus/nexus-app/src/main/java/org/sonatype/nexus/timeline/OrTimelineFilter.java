package org.sonatype.nexus.timeline;

import java.util.List;
import java.util.Map;

public class OrTimelineFilter
    extends MultiTimelineFilter
{
    public OrTimelineFilter()
    {
        super();
    }

    public OrTimelineFilter( List<TimelineFilter> terms )
    {
        super( terms );
    }

    public boolean accept( Map<String, String> hit )
    {
        for ( TimelineFilter term : getTerms() )
        {
            if ( term.accept( hit ) )
            {
                return true;
            }
        }

        return false;
    }

}
