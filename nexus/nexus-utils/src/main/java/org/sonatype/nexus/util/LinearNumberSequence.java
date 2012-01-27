package org.sonatype.nexus.util;

/**
 * A simple linear number sequence (linear equation).
 * 
 * @author cstamas
 */
public class LinearNumberSequence
    implements NumberSequence
{
    private final long start;

    private final long step;

    private final long multiplier;

    private final long shift;

    private long current;

    public LinearNumberSequence( final long start, final long step, final long multiplier, final long shift )
    {
        this.start = start;
        this.step = step;
        this.multiplier = multiplier;
        this.shift = shift;
    }

    @Override
    public long next()
    {
        current = current + step;
        return peek();
    }

    @Override
    public long prev()
    {
        current = current - step;
        return peek();
    }

    @Override
    public long peek()
    {
        return ( current * multiplier ) + shift;
    }

    @Override
    public void reset()
    {
        this.current = start;
    }
}
