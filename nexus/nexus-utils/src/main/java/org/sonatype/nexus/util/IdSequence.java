package org.sonatype.nexus.util;

import java.util.Random;

/**
 * This is a simple utility class, just to throw out IDs not for human consumption, but for unique keying.
 * 
 * @author cstamas
 */
public class IdSequence
{
    private Random random = new Random( System.currentTimeMillis() );

    public String generateId()
    {
        return Long.toHexString( System.nanoTime() + random.nextInt( 1975 ) );
    }
}
