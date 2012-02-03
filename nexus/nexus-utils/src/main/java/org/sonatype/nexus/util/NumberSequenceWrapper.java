package org.sonatype.nexus.util;

import com.google.common.base.Preconditions;

/**
 * Simple handy class to subclass when you want to wrap another {@link NumberSequence}.
 * 
 * @author cstamas
 * @since 2.0
 */
public abstract class NumberSequenceWrapper
    implements NumberSequence
{
    private final NumberSequence numberSequence;

    public NumberSequenceWrapper( final NumberSequence numberSequence )
    {
        this.numberSequence = Preconditions.checkNotNull( numberSequence );
    }
    
    protected NumberSequence getWrappedNumberSequence()
    {
        return numberSequence;
    }

    @Override
    public long next()
    {
        return numberSequence.next();
    }

    @Override
    public long prev()
    {
        return numberSequence.prev();
    }

    @Override
    public long peek()
    {
        return numberSequence.peek();
    }

    @Override
    public void reset()
    {
        numberSequence.reset();
    }
}
