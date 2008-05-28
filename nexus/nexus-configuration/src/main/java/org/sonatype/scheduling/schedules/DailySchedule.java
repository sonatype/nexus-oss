package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.DailyScheduleIterator;
import org.sonatype.scheduling.iterators.ScheduleIterator;

public class DailySchedule
    extends AbstractSchedule
{
    public DailySchedule( Date startDate, Date endDate )
    {
        super( startDate, endDate );
    }

    public ScheduleIterator getIterator()
    {
        return new DailyScheduleIterator( getStartDate(), getEndDate() );
    }
}
