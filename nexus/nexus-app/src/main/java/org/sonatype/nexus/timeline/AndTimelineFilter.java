package org.sonatype.nexus.timeline;

import java.util.List;
import java.util.Map;

public class AndTimelineFilter
    extends MultiTimelineFilter
{
    public AndTimelineFilter()
    {
        super();
    }

    public AndTimelineFilter( List<TimelineFilter> terms )
    {
        super( terms );
    }

    public boolean accept( Map<String, String> hit )
    {
        for ( TimelineFilter term : getTerms() )
        {
            if ( !term.accept( hit ) )
            {
                return false;
            }
        }

        return true;
    }

}
