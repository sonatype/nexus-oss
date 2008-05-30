package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class WeeklySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    private final Set<Integer> weekdaysToRun;

    public WeeklySchedulerIterator( Date startingDate )
    {
        super( startingDate );

        this.weekdaysToRun = null;
    }

    public WeeklySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.weekdaysToRun = null;
    }

    public WeeklySchedulerIterator( Date startingDate, Date endingDate, Set<Integer> weekdaysToRun )
    {
        super( startingDate, endingDate );

        this.weekdaysToRun = weekdaysToRun;
    }

    public void stepNext()
    {
        if ( weekdaysToRun == null || weekdaysToRun.isEmpty() )
        {
            getCalendar().add( Calendar.WEEK_OF_YEAR, 1 );
        }
        else
        {
            getCalendar().add( Calendar.DAY_OF_WEEK, 1 );

            while ( weekdaysToRun.contains( getCalendar().get( Calendar.DAY_OF_WEEK ) ) )
            {
                getCalendar().add( Calendar.DAY_OF_WEEK, 1 );
            }
        }
    }
}
