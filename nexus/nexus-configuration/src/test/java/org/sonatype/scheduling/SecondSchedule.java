package org.sonatype.scheduling;

import java.util.Date;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.AbstractSchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class SecondSchedule
    extends AbstractSchedule
    implements Schedule
{
    public SecondSchedule( Date startDate, Date endDate )
    {
        super( startDate, endDate );
    }
    public SchedulerIterator getIterator()
    {
        return new SecondScheduleIterator( getStartDate(), getEndDate() );
    }

}
