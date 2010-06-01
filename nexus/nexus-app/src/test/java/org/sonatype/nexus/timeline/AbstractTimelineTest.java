package org.sonatype.nexus.timeline;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.timeline.TimelineRecord;
import org.sonatype.timeline.TimelineResult;

public abstract class AbstractTimelineTest
    extends AbstractNexusTestCase
{
    /**
     * Handy method that does what was done before: keeps all in memory, but this is usable for small amount of data,
     * like these in UT. This should NOT be used in production code, unless you want app that kills itself with OOM.
     * 
     * @param result
     * @return
     */
    protected List<TimelineRecord> asList( TimelineResult result )
    {
        ArrayList<TimelineRecord> records = new ArrayList<TimelineRecord>();

        for ( TimelineRecord rec : result )
        {
            records.add( rec );
        }

        return records;
    }
}
