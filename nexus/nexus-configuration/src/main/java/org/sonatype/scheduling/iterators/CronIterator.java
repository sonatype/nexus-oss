package org.sonatype.scheduling.iterators;

import java.util.Date;

public class CronIterator
    extends AbstractSchedulerIterator
{
    private final String cronExpression;

    public CronIterator( String cronExpression )
    {
        super( new Date() );

        this.cronExpression = cronExpression;
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
