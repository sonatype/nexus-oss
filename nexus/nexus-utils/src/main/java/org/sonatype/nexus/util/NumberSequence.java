package org.sonatype.nexus.util;

/**
 * A simple interface that gives you a sequence of numbers. That might be simple natural numbers sequence, but anything
 * else too.
 * 
 * @author cstamas
 */
public interface NumberSequence
{
    /**
     * Returns the next number in sequence and advances the sequence.
     * 
     * @return
     */
    long next();

    /**
     * Returns the next number in sequence without advancing the sequence. This method will return always the same
     * number unless method {@code next()} is called.
     * 
     * @return
     */
    long peek();

    /**
     * Resets the sequence.
     */
    void reset();
}
