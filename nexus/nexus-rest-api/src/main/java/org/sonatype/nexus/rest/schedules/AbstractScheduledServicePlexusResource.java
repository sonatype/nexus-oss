/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceHourlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.iterators.MonthlySchedulerIterator;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.HourlySchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

public abstract class AbstractScheduledServicePlexusResource
    extends AbstractNexusPlexusResource
{
    /** Schedule Type Off. */
    public static final String SCHEDULE_TYPE_MANUAL = "manual";

    /** Schedule type run now */
    public static final String SCHEDULE_TYPE_RUN_NOW = "internal";

    /** Schedule Type Once. */
    public static final String SCHEDULE_TYPE_ONCE = "once";

    /** Schedule type Hourly */
    public static final String SCHEDULE_TYPE_HOURLY = "hourly";

    /** Schedule Type Daily. */
    public static final String SCHEDULE_TYPE_DAILY = "daily";

    /** Schedule Type Weekly. */
    public static final String SCHEDULE_TYPE_WEEKLY = "weekly";

    /** Schedule Type Monthly. */
    public static final String SCHEDULE_TYPE_MONTHLY = "monthly";

    /** Schedule Type Advanced. */
    public static final String SCHEDULE_TYPE_ADVANCED = "advanced";

    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

    private DateFormat timeFormat = new SimpleDateFormat( "HH:mm" );

    protected String getScheduleShortName( Schedule schedule )
    {
        if ( ManualRunSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_MANUAL;
        }
        else if ( RunNowSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_RUN_NOW;
        }
        else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_ONCE;
        }
        else if ( HourlySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_HOURLY;
        }
        else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_DAILY;
        }
        else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_WEEKLY;
        }
        else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_MONTHLY;
        }
        else if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_ADVANCED;
        }
        else
        {
            return schedule.getClass().getName();
        }
    }

    protected String formatDate( Date date )
    {
        return Long.toString( date.getTime() );
    }

    protected String formatTime( Date date )
    {
        return timeFormat.format( date );
    }

    protected List<ScheduledServicePropertyResource> formatServiceProperties( Map<String, String> map )
    {
        List<ScheduledServicePropertyResource> list = new ArrayList<ScheduledServicePropertyResource>();

        for ( String key : map.keySet() )
        {
            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( key );
            prop.setValue( map.get( key ) );
            list.add( prop );
        }

        return list;
    }

    protected List<String> formatRecurringDayOfWeek( Set<Integer> days )
    {
        List<String> list = new ArrayList<String>();

        for ( Integer day : days )
        {
            switch ( day.intValue() )
            {
                case 1:
                {
                    list.add( "sunday" );
                    break;
                }
                case 2:
                {
                    list.add( "monday" );
                    break;
                }
                case 3:
                {
                    list.add( "tuesday" );
                    break;
                }
                case 4:
                {
                    list.add( "wednesday" );
                    break;
                }
                case 5:
                {
                    list.add( "thursday" );
                    break;
                }
                case 6:
                {
                    list.add( "friday" );
                    break;
                }
                case 7:
                {
                    list.add( "saturday" );
                    break;
                }
            }
        }

        return list;
    }

    protected Set<Integer> formatRecurringDayOfWeek( List<String> days )
    {
        Set<Integer> set = new HashSet<Integer>();

        for ( String day : days )
        {
            if ( "sunday".equals( day ) )
            {
                set.add( new Integer( 1 ) );
            }
            else if ( "monday".equals( day ) )
            {
                set.add( new Integer( 2 ) );
            }
            else if ( "tuesday".equals( day ) )
            {
                set.add( new Integer( 3 ) );
            }
            else if ( "wednesday".equals( day ) )
            {
                set.add( new Integer( 4 ) );
            }
            else if ( "thursday".equals( day ) )
            {
                set.add( new Integer( 5 ) );
            }
            else if ( "friday".equals( day ) )
            {
                set.add( new Integer( 6 ) );
            }
            else if ( "saturday".equals( day ) )
            {
                set.add( new Integer( 7 ) );
            }
        }

        return set;
    }

    protected List<String> formatRecurringDayOfMonth( Set<Integer> days )
    {
        List<String> list = new ArrayList<String>();

        for ( Integer day : days )
        {
            if ( MonthlySchedulerIterator.LAST_DAY_OF_MONTH.equals( day ) )
            {
                list.add( "last" );
            }
            else
            {
                list.add( String.valueOf( day ) );
            }
        }

        return list;
    }

    protected Set<Integer> formatRecurringDayOfMonth( List<String> days )
    {
        Set<Integer> set = new HashSet<Integer>();

        for ( String day : days )
        {
            if ( "last".equals( day ) )
            {
                set.add( MonthlySchedulerIterator.LAST_DAY_OF_MONTH );
            }
            else
            {
                set.add( Integer.valueOf( day ) );
            }
        }

        return set;
    }

    protected Date parseDate( String date, String time )
    {
        Calendar cal = Calendar.getInstance();
        Calendar timeCalendar = Calendar.getInstance();

        try
        {
            timeCalendar.setTime( timeFormat.parse( time ) );

            cal.setTime( new Date( Long.parseLong( date ) ) );
            cal.add( Calendar.HOUR_OF_DAY, timeCalendar.get( Calendar.HOUR_OF_DAY ) );
            cal.add( Calendar.MINUTE, timeCalendar.get( Calendar.MINUTE ) );
        }
        catch ( ParseException e )
        {
            cal = null;
        }

        return cal == null ? null : cal.getTime();
    }

    public String getModelName( ScheduledServiceBaseResource model )
    {
        return model.getName();
    }

    public NexusTask<?> getModelNexusTask( ScheduledServiceBaseResource model, Request request )
        throws IllegalArgumentException,
            ResourceException
    {
        String serviceType = model.getTypeId();

        NexusTask<?> task = getNexus().createTaskInstance( serviceType );

        for ( Iterator iter = model.getProperties().iterator(); iter.hasNext(); )
        {
            ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();
            task.addParameter( prop.getId(), prop.getValue() );
        }

        return task;
    }

    public void validateStartDate( String date )
        throws InvalidConfigurationException
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( Long.parseLong( date ) ) );

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime( new Date() );

        // This is checking just the year/month/day, time isn't of concern right now
        if ( cal.before( nowCal )
            && ( cal.get( Calendar.YEAR ) != nowCal.get( Calendar.YEAR )
                || cal.get( Calendar.MONTH ) != nowCal.get( Calendar.MONTH ) || cal.get( Calendar.DAY_OF_YEAR ) != nowCal
                .get( Calendar.DAY_OF_YEAR ) ) )
        {
            ValidationResponse vr = new ApplicationValidationResponse();
            ValidationMessage vm = new ValidationMessage( "startDate", "Date cannot be in the past." );
            vr.addValidationError( vm );
            throw new InvalidConfigurationException( vr );
        }
    }

    public void validateTime( String key, Date date )
        throws InvalidConfigurationException
    {
        if ( date.before( new Date() ) )
        {
            ValidationResponse vr = new ApplicationValidationResponse();
            ValidationMessage vm = new ValidationMessage( key, "Time cannot be in the past." );
            vr.addValidationError( vm );
            throw new InvalidConfigurationException( vr );
        }
    }

    public Schedule getModelSchedule( ScheduledServiceBaseResource model )
        throws ParseException,
            InvalidConfigurationException
    {
        Schedule schedule = null;

        if ( ScheduledServiceAdvancedResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new CronSchedule( ( (ScheduledServiceAdvancedResource) model ).getCronCommand() );
        }
        else if ( ScheduledServiceMonthlyResource.class.isAssignableFrom( model.getClass() ) )
        {
            Date date = parseDate(
                ( (ScheduledServiceMonthlyResource) model ).getStartDate(),
                ( (ScheduledServiceMonthlyResource) model ).getRecurringTime() );

            // validateStartDate( ( (ScheduledServiceMonthlyResource) model ).getStartDate() );

            // validateTime( "recurringTime", date );

            schedule = new MonthlySchedule(
                date,
                null,
                formatRecurringDayOfMonth( ( (ScheduledServiceMonthlyResource) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceWeeklyResource.class.isAssignableFrom( model.getClass() ) )
        {
            Date date = parseDate(
                ( (ScheduledServiceWeeklyResource) model ).getStartDate(),
                ( (ScheduledServiceWeeklyResource) model ).getRecurringTime() );

            // validateStartDate( ( (ScheduledServiceWeeklyResource) model ).getStartDate() );

            // validateTime( "recurringTime", date );

            schedule = new WeeklySchedule(
                date,
                null,
                formatRecurringDayOfWeek( ( (ScheduledServiceWeeklyResource) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceDailyResource.class.isAssignableFrom( model.getClass() ) )
        {
            Date date = parseDate(
                ( (ScheduledServiceDailyResource) model ).getStartDate(),
                ( (ScheduledServiceDailyResource) model ).getRecurringTime() );

            // validateStartDate( ( (ScheduledServiceDailyResource) model ).getStartDate() );

            // validateTime( "recurringTime", date );

            schedule = new DailySchedule( date, null );
        }
        else if ( ScheduledServiceHourlyResource.class.isAssignableFrom( model.getClass() ) )
        {
            Date date = parseDate(
                ( (ScheduledServiceHourlyResource) model ).getStartDate(),
                ( (ScheduledServiceHourlyResource) model ).getStartTime() );

            // validateStartDate( ( (ScheduledServiceHourlyResource) model ).getStartDate() );

            // validateTime( "startTime", date );

            schedule = new HourlySchedule( date, null );
        }
        else if ( ScheduledServiceOnceResource.class.isAssignableFrom( model.getClass() ) )
        {
            Date date = parseDate(
                ( (ScheduledServiceOnceResource) model ).getStartDate(),
                ( (ScheduledServiceOnceResource) model ).getStartTime() );

            validateStartDate( ( (ScheduledServiceOnceResource) model ).getStartDate() );

            validateTime( "startTime", date );

            schedule = new OnceSchedule( parseDate(
                ( (ScheduledServiceOnceResource) model ).getStartDate(),
                ( (ScheduledServiceOnceResource) model ).getStartTime() ) );
        }
        else
        {
            schedule = new ManualRunSchedule();
        }

        return schedule;
    }

    public <T> ScheduledServiceBaseResource getServiceRestModel( ScheduledTask<T> task )
    {
        ScheduledServiceBaseResource resource = null;

        if ( RunNowSchedule.class.isAssignableFrom( task.getSchedule().getClass() )
            || ManualRunSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceBaseResource();
        }
        else if ( OnceSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceOnceResource();

            OnceSchedule taskSchedule = (OnceSchedule) task.getSchedule();
            ScheduledServiceOnceResource res = (ScheduledServiceOnceResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setStartTime( formatTime( taskSchedule.getStartDate() ) );
        }
        else if ( HourlySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceHourlyResource();

            HourlySchedule taskSchedule = (HourlySchedule) task.getSchedule();
            ScheduledServiceHourlyResource res = (ScheduledServiceHourlyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setStartTime( formatTime( taskSchedule.getStartDate() ) );
        }
        else if ( DailySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceDailyResource();

            DailySchedule taskSchedule = (DailySchedule) task.getSchedule();
            ScheduledServiceDailyResource res = (ScheduledServiceDailyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
        }
        else if ( WeeklySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceWeeklyResource();

            WeeklySchedule taskSchedule = (WeeklySchedule) task.getSchedule();
            ScheduledServiceWeeklyResource res = (ScheduledServiceWeeklyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
            res.setRecurringDay( formatRecurringDayOfWeek( taskSchedule.getDaysToRun() ) );
        }
        else if ( MonthlySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceMonthlyResource();

            MonthlySchedule taskSchedule = (MonthlySchedule) task.getSchedule();
            ScheduledServiceMonthlyResource res = (ScheduledServiceMonthlyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
            res.setRecurringDay( formatRecurringDayOfMonth( taskSchedule.getDaysToRun() ) );
        }
        else if ( CronSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceAdvancedResource();

            CronSchedule taskSchedule = (CronSchedule) task.getSchedule();
            ScheduledServiceAdvancedResource res = (ScheduledServiceAdvancedResource) resource;

            res.setCronCommand( taskSchedule.getCronString() );
        }

        if ( resource != null )
        {
            resource.setId( task.getId() );
            resource.setEnabled( task.isEnabled() );
            resource.setName( task.getName() );
            resource.setSchedule( getScheduleShortName( task.getSchedule() ) );
            resource.setTypeId( task.getType() );
            resource.setProperties( formatServiceProperties( task.getTaskParams() ) );
        }

        return resource;
    }

    protected <T> String getNextRunTime( ScheduledTask<T> task )
    {
        String nextRunTime = "n/a";

        // Run now type tasks should never have a next run time
        if ( !task.getSchedule().getClass().isAssignableFrom( RunNowSchedule.class ) && task.getNextRun() != null )
        {
            nextRunTime = task.getNextRun().toString();
        }

        return nextRunTime;
    }
}
