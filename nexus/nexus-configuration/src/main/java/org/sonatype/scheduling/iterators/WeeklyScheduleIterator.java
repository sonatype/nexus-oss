package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public class WeeklyScheduleIterator
    extends AbstractCalendarBasedScheduleIterator
{
    public WeeklyScheduleIterator( Date startingDate )
    {
        super( startingDate );
    }

    public WeeklyScheduleIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    public void stepNext()
    {
        getCalendar().add( Calendar.WEEK_OF_YEAR, 1 );
    }
}
