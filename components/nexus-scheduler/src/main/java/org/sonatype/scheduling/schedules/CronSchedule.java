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
package org.sonatype.scheduling.schedules;

import java.text.ParseException;
import java.util.Date;

import org.sonatype.scheduling.iterators.CronIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.iterators.cron.CronExpression;

public class CronSchedule
    extends AbstractSchedule
{
    private final String cronString;

    private final CronExpression cronExpression;

    public CronSchedule( String cronExpression )
        throws ParseException
    {
        super( new Date(), null );

        this.cronString = cronExpression;

        this.cronExpression = new CronExpression( cronString );
    }

    public String getCronString()
    {
        return cronString;
    }

    protected SchedulerIterator createIterator()
    {
        return new CronIterator( cronExpression );
    }

}
