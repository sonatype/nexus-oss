package org.sonatype.scheduling.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CompositeSchedulerIterator
    extends AbstractSchedulerIterator
{
    private final List<SchedulerIterator> iterators;

    public CompositeSchedulerIterator( Collection<SchedulerIterator> its )
    {
        super( new Date(), null );

        this.iterators = new ArrayList<SchedulerIterator>( its.size() );

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

        for ( SchedulerIterator i : iterators )
        {
            result = result || i.isFinished();
        }

        return result;
    }

    protected SchedulerIterator getNextIterator()
    {
        Date currDate = null;

        Date nextDate = null;

        SchedulerIterator currIterator = null;

        SchedulerIterator nextIterator = null;

        for ( Iterator<SchedulerIterator> i = iterators.iterator(); i.hasNext(); )
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
