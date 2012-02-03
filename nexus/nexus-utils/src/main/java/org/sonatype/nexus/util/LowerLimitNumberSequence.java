package org.sonatype.nexus.util;

/**
 * A NumberSequence implementation that wraps another sequence and imposes a lower limit to it.
 * 
 * @author cstamas
 * @since 2.0
 */
public class LowerLimitNumberSequence
    extends NumberSequenceWrapper
{
    private long lowerLimit;

    public LowerLimitNumberSequence( final NumberSequence numberSequence, final long lowerLimit )
    {
        super( numberSequence );
        this.lowerLimit = lowerLimit;
    }

    public long getLowerLimit()
    {
        return lowerLimit;
    }

    public void setLowerLimit( long lowerLimit )
    {
        this.lowerLimit = lowerLimit;
    }

    @Override
    public long prev()
    {
        // we allow only "one step" under the limit
        // when we reach that, we do not want state change on wrapped sequence
        // (next will be "reset" anyway)
        final long wrapped = super.peek();

        if ( wrapped < lowerLimit )
        {
            // we are already "one step" below
            return lowerLimit;
        }
        else
        {
            // perform the state change
            final long wrappedPrev = super.prev();
            
            if ( wrappedPrev < lowerLimit )
            {
                // this one step made it
                return lowerLimit;
            }
            else
            {
                // we are still good
                return wrappedPrev;
            }
        }
    }

    @Override
    public long peek()
    {
        final long wrapped = super.peek();

        if ( wrapped < lowerLimit )
        {
            return lowerLimit;
        }
        else
        {
            return wrapped;
        }
    }
}
