/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.sonatype.scheduling.iterators.WeeklySchedulerIterator;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link WeeklySchedulerIterator}.
 */
public class WeeklyIteratorTest
    extends TestSupport
{
    public void testWeeklyIterator()
        throws Exception
    {
        Calendar nearFuture = Calendar.getInstance();
        nearFuture.add( Calendar.MINUTE, 15 );

        HashSet<Integer> days = new HashSet<Integer>();

        days.add( 1 );
        days.add( 2 );
        days.add( 3 );
        days.add( 4 );
        days.add( 5 );
        days.add( 6 );
        days.add( 7 );

        WeeklySchedulerIterator iter = new WeeklySchedulerIterator( nearFuture.getTime(), null, days );

        Date nextDate = iter.next();

        assertTrue( nearFuture.getTime().equals( nextDate ) );

        // Just validate the next 20 days in a row
        for ( int i = 0; i < 20; i++ )
        {
            nextDate = iter.next();

            nearFuture.add( Calendar.DAY_OF_YEAR, 1 );

            assertTrue( nearFuture.getTime().equals( nextDate ) );
        }
    }
}
