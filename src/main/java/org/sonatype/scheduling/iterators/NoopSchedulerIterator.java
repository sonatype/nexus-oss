package org.sonatype.scheduling.iterators;

import java.util.Date;

/**
 * NOOP impl that will be used when no schedule is available, to save some null checks
 * 
 * @author dbradicich
 * @since 1.4.3
 *
 */
public class NoopSchedulerIterator
    extends AbstractSchedulerIterator
{
    public NoopSchedulerIterator()
    {
        super( new Date() );
    }

    public void resetFrom( Date from )
    {
    }

    @Override
    protected Date doPeekNext()
    {
        return null;
    }

    @Override
    protected void stepNext()
    {
    }
}
