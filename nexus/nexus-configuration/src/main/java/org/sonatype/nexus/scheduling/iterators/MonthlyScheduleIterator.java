package org.sonatype.nexus.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public class MonthlyScheduleIterator
    extends AbstractCalendarBasedScheduleIterator
{
    public MonthlyScheduleIterator( Date startingDate )
    {
        super( startingDate );
    }

    public MonthlyScheduleIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    public void stepNext()
    {
        getCalendar().add( Calendar.MONTH, 1 );
    }
}
