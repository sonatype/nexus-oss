package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class MonthlyScheduleIterator
    extends AbstractCalendarBasedScheduleIterator
{
    private final Set<Integer> monthdaysToRun;

    public MonthlyScheduleIterator( Date startingDate )
    {
        super( startingDate );

        this.monthdaysToRun = null;
    }

    public MonthlyScheduleIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = null;
    }

    public MonthlyScheduleIterator( Date startingDate, Date endingDate, Set<Integer> monthdaysToRun )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = monthdaysToRun;
    }

    public void stepNext()
    {
        if ( monthdaysToRun == null || monthdaysToRun.isEmpty() )
        {
            getCalendar().add( Calendar.MONTH, 1 );
        }
        else
        {
            getCalendar().add( Calendar.DAY_OF_MONTH, 1 );

            while ( monthdaysToRun.contains( getCalendar().get( Calendar.DAY_OF_MONTH ) ) )
            {
                getCalendar().add( Calendar.DAY_OF_MONTH, 1 );
            }
        }
    }
}
