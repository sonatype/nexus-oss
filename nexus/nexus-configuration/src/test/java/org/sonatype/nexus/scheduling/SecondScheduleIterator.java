package org.sonatype.nexus.scheduling;

import java.util.Calendar;
import java.util.Date;

import org.sonatype.nexus.scheduling.iterators.AbstractCalendarBasedScheduleIterator;

public class SecondScheduleIterator
    extends AbstractCalendarBasedScheduleIterator
{

    public SecondScheduleIterator( Date startingDate )
    {
        super( startingDate );
    }

    public SecondScheduleIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    @Override
    protected void stepNext()
    {
        getCalendar().add( Calendar.SECOND, 1 );
    }

}
