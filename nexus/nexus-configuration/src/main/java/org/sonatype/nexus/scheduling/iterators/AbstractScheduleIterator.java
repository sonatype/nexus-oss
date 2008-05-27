package org.sonatype.nexus.scheduling.iterators;

import java.util.Date;

import org.sonatype.nexus.scheduling.ScheduleIterator;

public abstract class AbstractScheduleIterator
    implements ScheduleIterator
{
    private final Date startingDate;

    private final Date endingDate;

    private boolean paused;

    public AbstractScheduleIterator( Date startingDate )
    {
        this( startingDate, null );
    }

    public AbstractScheduleIterator( Date startingDate, Date endingDate )
    {
        super();

        if ( startingDate == null )
        {
            throw new NullPointerException( "Starting Date of " + this.getClass().getName() + " cannot be null!" );
        }

        this.startingDate = startingDate;

        this.endingDate = endingDate;
    }

    public Date getStartingDate()
    {
        return startingDate;
    }

    public Date getEndingDate()
    {
        return endingDate;
    }

    public final Date peekNext()
    {
        Date current = doPeekNext();

        if ( current == null || ( getEndingDate() != null && current.after( getEndingDate() ) ) )
        {
            return null;
        }
        else
        {
            return current;
        }
    }

    public final Date next()
    {
        Date result = peekNext();

        if ( !isPaused() )
        {
            stepNext();
        }

        return result;
    }

    public boolean isFinished()
    {
        return peekNext() == null;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public void setPaused( boolean paused )
    {
        this.paused = paused;
    }

    protected abstract Date doPeekNext();

    protected abstract void stepNext();

}
