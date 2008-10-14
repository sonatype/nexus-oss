package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.HourlySchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class HourlySchedule
extends AbstractSchedule
{
    public HourlySchedule( Date startDate, Date endDate )
    {
        super( startDate, endDate );
    }

    protected SchedulerIterator createIterator()
    {
        return new HourlySchedulerIterator( getStartDate(), getEndDate() );
    }
}
