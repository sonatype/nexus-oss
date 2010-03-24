package org.sonatype.nexus.util;

import junit.framework.TestCase;

public class NumberSequenceTest
    extends TestCase
{
    public void testConstantSequence()
    {
        long startValue = 10;

        ConstantNumberSequence cs = new ConstantNumberSequence( startValue );

        for ( int i = 0; i < 20; i++ )
        {
            assertEquals( startValue, cs.next() );
        }

        cs.reset();

        for ( int i = 0; i < 20; i++ )
        {
            assertEquals( startValue, cs.next() );
        }
    }

    public void testFibonacciSequence()
    {
        int[] fibonacciNumbers = new int[] { 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233 };

        FibonacciNumberSequence fs = new FibonacciNumberSequence();

        for ( int f : fibonacciNumbers )
        {
            assertEquals( f, fs.next() );
        }

        fs.reset();

        for ( int f : fibonacciNumbers )
        {
            assertEquals( f, fs.next() );
        }
    }

    public void testFoxiedFibonacciSequence()
    {
        int[] fibonacciNumbers = new int[] { 10, 10, 20, 30, 50, 80, 130, 210, 340, 550, 890, 1440, 2330 };

        FibonacciNumberSequence fs = new FibonacciNumberSequence( 10 );

        for ( int f : fibonacciNumbers )
        {
            assertEquals( f, fs.next() );
        }

        fs.reset();

        for ( int f : fibonacciNumbers )
        {
            assertEquals( f, fs.next() );
        }
    }
}
