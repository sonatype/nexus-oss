/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.scheduling.iterators.WeeklySchedulerIterator;

public class WeeklyIteratorTest
    extends AbstractNexusTestCase
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
        for ( int i = 0 ; i < 20 ; i++ )
        {
            nextDate = iter.next();
            
            nearFuture.add( Calendar.DAY_OF_YEAR, 1 );
            
            assertTrue( nearFuture.getTime().equals( nextDate ) );
        }
    }
}
