package org.sonatype.nexus.timeline;

import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineRecord;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Wrapping predicate into TimelineFilter.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class PredicateTimelineFilter
    implements TimelineFilter
{

    private final Predicate<Entry> predicate;

    public PredicateTimelineFilter( final Predicate<Entry> predicate )
    {
        this.predicate = Preconditions.checkNotNull( predicate );
    }

    @Override
    public boolean accept( final TimelineRecord timelineRecord )
    {
        return predicate.apply( new TimelineRecordWrapper( timelineRecord ) );
    }
}
