package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.OnceSchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class OnceSchedule
    extends AbstractSchedule
{
    public OnceSchedule( Date date )
    {
        super( date, date );
    }

    public SchedulerIterator getIterator()
    {
        return new OnceSchedulerIterator( getStartDate() );
    }
}
