/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
