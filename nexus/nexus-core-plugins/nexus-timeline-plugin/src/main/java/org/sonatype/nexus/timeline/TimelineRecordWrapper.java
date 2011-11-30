package org.sonatype.nexus.timeline;

import java.util.Map;

import org.sonatype.timeline.TimelineRecord;
import com.google.common.base.Preconditions;

/**
 * Wrapping timeline record into Entry.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class TimelineRecordWrapper
    implements Entry
{

    private final TimelineRecord record;

    public TimelineRecordWrapper( final TimelineRecord record )
    {
        this.record = Preconditions.checkNotNull( record );
    }

    @Override
    public long getTimestamp()
    {
        return record.getTimestamp();
    }

    @Override
    public String getType()
    {
        return record.getType();
    }

    @Override
    public String getSubType()
    {
        return record.getSubType();
    }

    @Override
    public Map<String, String> getData()
    {
        return record.getData();
    }
}
