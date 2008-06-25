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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CAdvancedSchedule;
import org.sonatype.nexus.configuration.model.CDailySchedule;
import org.sonatype.nexus.configuration.model.CMonthlySchedule;
import org.sonatype.nexus.configuration.model.COnceSchedule;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CSchedule;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.CWeeklySchedule;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DefaultTaskConfigManagerTest
    extends AbstractNexusTestCase
{
    private DefaultScheduler defaultScheduler;

    private DefaultTaskConfigManager defaultManager;

    private File configurationFile;

    private static final String PROPERTY_KEY_START_DATE = "startDate";

    private static final String PROPERTY_KEY_END_DATE = "endDate";

    private static final String PROPERTY_KEY_CRON_EXPRESSION = "cronExpression";

    private static final String SCHEDULE_TYPE_ONCE = "once";

    private static final String SCHEDULE_TYPE_DAILY = "daily";

    private static final String SCHEDULE_TYPE_WEEKLY = "weekly";

    private static final String SCHEDULE_TYPE_MONTHLY = "monthly";

    private static final String SCHEDULE_TYPE_ADVANCED = "advanced";

    private static final String TASK_NAME = "test";

    private static final String CRON_EXPRESSION = "0 0/5 14,18,3-9,2 ? JAN,MAR,SEP MON-FRI 2002-2010";

    private static final HashMap<String, Class> typeClassMap;

    static
    {
        typeClassMap = new HashMap<String, Class>();
        typeClassMap.put( SCHEDULE_TYPE_ONCE, COnceSchedule.class );
        typeClassMap.put( SCHEDULE_TYPE_DAILY, CDailySchedule.class );
        typeClassMap.put( SCHEDULE_TYPE_WEEKLY, CWeeklySchedule.class );
        typeClassMap.put( SCHEDULE_TYPE_MONTHLY, CMonthlySchedule.class );
        typeClassMap.put( SCHEDULE_TYPE_ADVANCED, CAdvancedSchedule.class );
    }

    /**
     * NOTE: this is only populated after call to loadConfig()
     */
    private CTaskConfiguration configuration;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );
        defaultScheduler.startService();

        defaultManager = (DefaultTaskConfigManager) lookup( TaskConfigManager.class.getName() );
        configurationFile = new File( PLEXUS_HOME + "/nexus/conf/tasks.xml" );

    }

    public void tearDown()
        throws Exception
    {
        defaultScheduler.stopService();

        super.tearDown();
    }

    public void testStoreOnceSchedule()
        throws Exception
    {
        Date date = new Date();
        HashMap<String, Object> scheduleProperties = new HashMap<String, Object>();
        scheduleProperties.put( PROPERTY_KEY_START_DATE, date );
        genericTestStore( SCHEDULE_TYPE_ONCE, scheduleProperties );
    }

    public void testStoreDailySchedule()
        throws Exception
    {
        Date startDate = new Date();
        Date endDate = new Date();
        HashMap<String, Object> scheduleProperties = new HashMap<String, Object>();
        scheduleProperties.put( PROPERTY_KEY_START_DATE, startDate );
        scheduleProperties.put( PROPERTY_KEY_END_DATE, endDate );
        genericTestStore( SCHEDULE_TYPE_DAILY, scheduleProperties );
    }

    public void testStoreWeeklySchedule()
        throws Exception
    {
        Date startDate = new Date();
        Date endDate = new Date();
        HashMap<String, Object> scheduleProperties = new HashMap<String, Object>();
        scheduleProperties.put( PROPERTY_KEY_START_DATE, startDate );
        scheduleProperties.put( PROPERTY_KEY_END_DATE, endDate );
        genericTestStore( SCHEDULE_TYPE_WEEKLY, scheduleProperties );
    }

    public void testStoreMonthlySchedule()
        throws Exception
    {
        Date startDate = new Date();
        Date endDate = new Date();
        HashMap<String, Object> scheduleProperties = new HashMap<String, Object>();
        scheduleProperties.put( PROPERTY_KEY_START_DATE, startDate );
        scheduleProperties.put( PROPERTY_KEY_END_DATE, endDate );
        genericTestStore( SCHEDULE_TYPE_MONTHLY, scheduleProperties );
    }

    public void testStoreAdvancedSchedule()
        throws Exception
    {
        HashMap<String, Object> scheduleProperties = new HashMap<String, Object>();
        scheduleProperties.put( PROPERTY_KEY_CRON_EXPRESSION, CRON_EXPRESSION );
        genericTestStore( SCHEDULE_TYPE_ADVANCED, scheduleProperties );
    }

    public void genericTestStore( String scheduleType, HashMap<String, Object> scheduleProperties )
        throws ParseException
    {
        ScheduledTask<Integer> task = null;
        try
        {
            task = createScheduledTask( createSchedule( scheduleType, scheduleProperties ) );
            defaultManager.addTask( task );
            loadConfig();
            assertTrue( configuration.getTasks().size() == 1 );
            assertTrue( TaskState.SUBMITTED.equals( TaskState.valueOf( ( (CScheduledTask) configuration.getTasks().get(
                0 ) ).getStatus() ) ) );
            assertTrue( TASK_NAME.equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
            assertTrue( typeClassMap.get( scheduleType ).isAssignableFrom(
                ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
            defaultManager.removeTask( task );
            loadConfig();
            assertTrue( configuration.getTasks().size() == 0 );
        }
        finally
        {
            if ( task != null )
            {
                task.cancel();
                defaultManager.removeTask( task );
            }
        }
    }

    private Schedule createSchedule( String type, HashMap<String, Object> properties )
        throws ParseException
    {
        if ( SCHEDULE_TYPE_ONCE.equals( type ) )
        {
            return new OnceSchedule( (Date) properties.get( PROPERTY_KEY_START_DATE ) );
        }
        else if ( SCHEDULE_TYPE_DAILY.equals( type ) )
        {
            return new DailySchedule( (Date) properties.get( PROPERTY_KEY_START_DATE ), (Date) properties
                .get( PROPERTY_KEY_END_DATE ) );
        }
        else if ( SCHEDULE_TYPE_WEEKLY.equals( type ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            daysToRun.add( new Integer( 0 ) );
            return new WeeklySchedule( (Date) properties.get( PROPERTY_KEY_START_DATE ), (Date) properties
                .get( PROPERTY_KEY_END_DATE ), daysToRun );
        }
        else if ( SCHEDULE_TYPE_MONTHLY.equals( type ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            daysToRun.add( new Integer( 1 ) );
            return new MonthlySchedule( (Date) properties.get( PROPERTY_KEY_START_DATE ), (Date) properties
                .get( PROPERTY_KEY_END_DATE ), daysToRun );
        }
        else if ( SCHEDULE_TYPE_ADVANCED.equals( type ) )
        {
            return new CronSchedule( (String) properties.get( PROPERTY_KEY_CRON_EXPRESSION ) );
        }

        return null;
    }

    private ScheduledTask<Integer> createScheduledTask( Schedule schedule )
    {
        TestCallable callable = new TestCallable();
        return new DefaultScheduledTask<Integer>(
            TASK_NAME,
            callable.getClass(),
            defaultScheduler,
            callable,
            schedule,
            null );
    }

    private void loadConfig()
    {
        XStream xstream = configureXStream( new XStream( new DomDriver() ) );

        configuration = new CTaskConfiguration();

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( configurationFile );
            xstream.fromXML( fis, configuration );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch ( IOException e )
                {
                }
            }
        }
    }

    public class TestCallable
        implements Callable<Integer>
    {
        private int runCount = 0;

        public Integer call()
            throws Exception
        {
            return runCount++;
        }

        public int getRunCount()
        {
            return runCount;
        }
    }

    private XStream configureXStream( XStream xstream )
    {
        xstream.omitField( CTaskConfiguration.class, "modelEncoding" );
        xstream.omitField( CScheduledTask.class, "modelEncoding" );
        xstream.omitField( CSchedule.class, "modelEncoding" );
        xstream.omitField( CAdvancedSchedule.class, "modelEncoding" );
        xstream.omitField( CDailySchedule.class, "modelEncoding" );
        xstream.omitField( CMonthlySchedule.class, "modelEncoding" );
        xstream.omitField( COnceSchedule.class, "modelEncoding" );
        xstream.omitField( CWeeklySchedule.class, "modelEncoding" );
        xstream.omitField( CProps.class, "modelEncoding" );

        return xstream;
    }
}
