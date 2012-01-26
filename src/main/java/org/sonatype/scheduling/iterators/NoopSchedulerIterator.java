package org.sonatype.scheduling.iterators;

import java.util.Date;

public class NoopSchedulerIterator
    extends AbstractSchedulerIterator
{
    public NoopSchedulerIterator()
    {
        super( new Date() );
    }

    public void resetFrom( Date from )
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected Date doPeekNext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void stepNext()
    {
        // TODO Auto-generated method stub

    }
}
