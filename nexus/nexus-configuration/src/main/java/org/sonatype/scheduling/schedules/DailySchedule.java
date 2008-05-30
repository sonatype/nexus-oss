package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.DailySchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class DailySchedule
    extends AbstractSchedule
{
    public DailySchedule( Date startDate, Date endDate )
    {
        super( startDate, endDate );
    }

    public SchedulerIterator getIterator()
    {
        return new DailySchedulerIterator( getStartDate(), getEndDate() );
    }
}
