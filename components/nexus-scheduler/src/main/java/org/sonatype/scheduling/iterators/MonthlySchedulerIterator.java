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
package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class MonthlySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    public static final Integer LAST_DAY_OF_MONTH = new Integer( 999 );
    
    private final Set<Integer> monthdaysToRun;

    public MonthlySchedulerIterator( Date startingDate )
    {
        super( startingDate );

        this.monthdaysToRun = null;
    }

    public MonthlySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = null;
    }

    public MonthlySchedulerIterator( Date startingDate, Date endingDate, Set<Integer> monthdaysToRun )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = monthdaysToRun;
    }

    public void stepNext()
    {
        if ( monthdaysToRun == null || monthdaysToRun.isEmpty() )
        {
            getCalendar().add( Calendar.MONTH, 1 );
        }
        else
        {
            getCalendar().add( Calendar.DAY_OF_MONTH, 1 );
            
            // step over the days not in when to run
            while ( !monthdaysToRun.contains( getCalendar().get( Calendar.DAY_OF_MONTH ) ) )
            {
                // first check to see if we are on the last day of the month
                if ( monthdaysToRun.contains( LAST_DAY_OF_MONTH ) )
                {
                    Calendar cal = ( Calendar ) getCalendar().clone();
                    
                    cal.add( Calendar.DAY_OF_MONTH, 1 );
                    
                    if ( cal.get( Calendar.DAY_OF_MONTH ) == 1 )
                    {
                        break;
                    }
                }
                
                getCalendar().add( Calendar.DAY_OF_MONTH, 1 );
            }
        }
    }
}
