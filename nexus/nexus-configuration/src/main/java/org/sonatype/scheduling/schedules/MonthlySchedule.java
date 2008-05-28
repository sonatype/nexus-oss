package org.sonatype.scheduling.schedules;

import java.util.Date;
import java.util.Set;

import org.sonatype.scheduling.iterators.MonthlyScheduleIterator;
import org.sonatype.scheduling.iterators.ScheduleIterator;

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

    public ScheduleIterator getIterator()
    {
        return new MonthlyScheduleIterator( getStartDate(), getEndDate(), daysToRun );
    }

}
