/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus3996;

import com.google.common.collect.Lists;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EmptyTrashTaskDescriptor;
import org.sonatype.nexus.test.utils.NexusRequestMatchers;
import org.sonatype.nexus.test.utils.ResponseMatchers;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Nexus would not create tasks for the same day if the client is in a timezone with a larger offset from UTC than
 * the server's timezone.
 * <p/>
 * The date is sent separate from the time as a timestamp for midnight of the selected day. The midnight is calculated
 * on the client based on the local timezone.
 * <p/>
 * The server does not know which timezone the client is in and validates the date based on his timezone, rejecting
 * every midnight that is earlier than local time and not on the same day of the year.
 * <p/>
 * This test simulates a client whose timezone offset is two hours more than the local timezone.
 * <p/>
 * Related issues with timezone problems in the scheduled tasks:
 * <ul>
 * <li> https://issues.sonatype.org/browse/NEXUS-4617 </li>
 * <li> https://issues.sonatype.org/browse/NEXUS-4616 </li>
 * </ul>
 *
 * @since 1.10.0
 */
public class Nexus3996ScheduledTasksTimezoneDifferentDayValidationIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void createTask()
        throws IOException
    {

        final ScheduledServiceOnceResource task = new ScheduledServiceOnceResource();
        task.setName( "name" );
        task.setSchedule( "once" );
        task.setTypeId( EmptyTrashTaskDescriptor.ID );
        ScheduledServicePropertyResource property = new ScheduledServicePropertyResource();
        property.setKey( EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID );
        task.setProperties( Lists.newArrayList( property ) );

        Calendar cal = Calendar.getInstance();

        // simulating client timezone here: calculate offset from current timezone

        // client is tz+2 -> 3 hours ahead for local tz is one hour ahead for client tz
        cal.add( Calendar.HOUR_OF_DAY, 3 );
        task.setStartTime( new SimpleDateFormat( "HH:mm" ).format( cal.getTime() ) );

        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );

        // client is tz+2 -> midnight for client happens 2 hours before servers midnight
        cal.add( Calendar.HOUR_OF_DAY, -2 );
        task.setStartDate( String.valueOf( cal.getTimeInMillis() ) );

        log.debug( "request dates:\nmidnight: {}\ntime offset: {}", cal.getTime(), task.getStartTime() );

        assertThat( TaskScheduleUtil.create( task ), NexusRequestMatchers.hasStatusCode( 201 ) );
    }

}
