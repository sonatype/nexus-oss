package org.sonatype.scheduling.schedules;

import java.util.Date;
import java.util.Set;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.iterators.WeeklySchedulerIterator;

public class WeeklySchedule
    extends AbstractSchedule
{
    private final Set<Integer> daysToRun;

    public WeeklySchedule( Date startDate, Date endDate, Set<Integer> daysToRun )
    {
        super( startDate, endDate );

        this.daysToRun = daysToRun;
    }

    public Set<Integer> getDaysToRun()
    {
        return daysToRun;
    }

    public SchedulerIterator getIterator()
    {
        return new WeeklySchedulerIterator( getStartDate(), getEndDate(), daysToRun );
    }
}
