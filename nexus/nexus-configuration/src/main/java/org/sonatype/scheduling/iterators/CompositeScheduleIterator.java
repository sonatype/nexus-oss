package org.sonatype.scheduling.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CompositeScheduleIterator
    extends AbstractScheduleIterator
{
    private final List<ScheduleIterator> iterators;

    public CompositeScheduleIterator( Collection<ScheduleIterator> its )
    {
        super( new Date(), null );

        this.iterators = new ArrayList<ScheduleIterator>( its.size() );

        this.iterators.addAll( its );
    }

    @Override
    protected Date doPeekNext()
    {
        // get the "smallest" date and return it's peekNext();
        return getNextIterator().peekNext();
    }

    @Override
    public void stepNext()
    {
        // get the "smallest" date and return it's next();
        getNextIterator().next();
    }

    @Override
    public boolean isFinished()
    {
        // it is finished if all iterators are finished
        boolean result = false;

        for ( ScheduleIterator i : iterators )
        {
            result = result || i.isFinished();
        }

        return result;
    }

    protected ScheduleIterator getNextIterator()
    {
        Date currDate = null;

        Date nextDate = null;

        ScheduleIterator currIterator = null;

        ScheduleIterator nextIterator = null;

        for ( Iterator<ScheduleIterator> i = iterators.iterator(); i.hasNext(); )
        {
            currIterator = i.next();

            currDate = currIterator.peekNext();

            if ( currDate == null )
            {
                i.remove();
            }
            else
            {
                if ( nextDate == null || currDate.before( nextDate ) )
                {
                    nextDate = currDate;

                    nextIterator = currIterator;
                }
            }
        }
        return nextIterator;
    }
}
