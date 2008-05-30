package org.sonatype.scheduling.schedules;

import java.util.Date;
import java.util.Set;

import org.sonatype.scheduling.iterators.MonthlySchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class MonthlySchedule
    extends AbstractSchedule
{
    private final Set<Integer> daysToRun;

    public MonthlySchedule( Date startDate, Date endDate, Set<Integer> daysToRun )
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
        return new MonthlySchedulerIterator( getStartDate(), getEndDate(), daysToRun );
    }

}
