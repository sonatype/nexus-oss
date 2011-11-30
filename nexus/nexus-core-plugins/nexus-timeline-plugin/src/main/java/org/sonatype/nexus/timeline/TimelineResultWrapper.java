package org.sonatype.nexus.timeline;

import java.util.Iterator;
import javax.annotation.Nullable;

import org.sonatype.timeline.TimelineRecord;
import org.sonatype.timeline.TimelineResult;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * Wrapping TimelineResult into Entries.
 *
 * @author: cstamas
 */
public class TimelineResultWrapper
    implements Entries
{

    private final TimelineResult result;

    public TimelineResultWrapper( final TimelineResult result )
    {
        this.result = Preconditions.checkNotNull( result );
    }

    @Override
    public void release()
    {
        result.release();
    }

    @Override
    public Iterator<Entry> iterator()
    {
        return Iterators.transform( result.iterator(), new Function<TimelineRecord, Entry>()
        {
            @Override
            public Entry apply( @Nullable final TimelineRecord input )
            {
                return new TimelineRecordWrapper( input );
            }
        } );
    }
}
