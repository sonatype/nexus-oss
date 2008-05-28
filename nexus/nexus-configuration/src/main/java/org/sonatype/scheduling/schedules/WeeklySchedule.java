package org.sonatype.scheduling.schedules;

import java.util.Date;
import java.util.Set;

import org.sonatype.scheduling.iterators.ScheduleIterator;
import org.sonatype.scheduling.iterators.WeeklyScheduleIterator;

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

    public ScheduleIterator getIterator()
    {
        return new WeeklyScheduleIterator( getStartDate(), getEndDate(), daysToRun );
    }

}
