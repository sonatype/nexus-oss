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

/**
 * Class that calculates "fibonacci-like" sequence. For those uninformed, there is only one Fibonacci sequence out
 * there, all others are just alike. Fibonacci sequence per-definition starts with 0, 1, 1... It does not start with 10.
 * Or, the sequence that starts with 10 is NOT a Fibonacci sequence. This implementation avoids the "artificial" leading
 * 0 in the sequence.
 * 
 * @author cstamas
 */
public class FibonacciNumberSequence
    implements NumberSequence
{
    private final long start;

    private long a;

    private long b;

    public FibonacciNumberSequence()
    {
        this( 1 );
    }

    public FibonacciNumberSequence( long start )
    {
        this.start = start;

        reset();
    }

    public long next()
    {
        long res = a;
        
        a = b;

        b = res + b;

        return res;
    }

    public long peek()
    {
        return a;
    }

    public void reset()
    {
        a = start;

        b = start;
    }

}
