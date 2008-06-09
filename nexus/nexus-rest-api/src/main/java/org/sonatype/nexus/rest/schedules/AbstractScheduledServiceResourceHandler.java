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
package org.sonatype.nexus.rest.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.PublishIndexesTask;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

public class AbstractScheduledServiceResourceHandler
    extends AbstractNexusResourceHandler
{
    /** Schedule Type Off. */
    public static final String SCHEDULE_TYPE_NONE = "none";

    /** Schedule Type Once. */
    public static final String SCHEDULE_TYPE_ONCE = "once";

    /** Schedule Type Daily. */
    public static final String SCHEDULE_TYPE_DAILY = "daily";

    /** Schedule Type Weekly. */
    public static final String SCHEDULE_TYPE_WEEKLY = "weekly";

    /** Schedule Type Monthly. */
    public static final String SCHEDULE_TYPE_MONTHLY = "monthly";

    /** Schedule Type Advanced. */
    public static final String SCHEDULE_TYPE_ADVANCED = "advanced";

    /**
     * Type property resource: string
     */
    public static final String PROPERTY_TYPE_STRING = "string";

    /**
     * Type property resource: number
     */
    public static final String PROPERTY_TYPE_NUMBER = "number";

    /**
     * Type property resource: date
     */
    public static final String PROPERTY_TYPE_DATE = "date";

    /**
     * Type property resource: repository
     */
    public static final String PROPERTY_TYPE_REPO = "repository";

    /**
     * Type property resource: repositoryGroup
     */
    public static final String PROPERTY_TYPE_REPO_GROUP = "repositoryGroup";
    
    private DateFormat dateFormat = new SimpleDateFormat( "yyyy.MM.dd" );

    private DateFormat timeFormat = new SimpleDateFormat( "HH:mm" );

    protected Map<String, String> serviceNames = new HashMap<String, String>();
    {
        serviceNames.put( PublishIndexesTask.class.getName(), "Publish Indexes" );
        serviceNames.put( ReindexTask.class.getName(), "Reindex Repositories" );
        serviceNames.put( RebuildAttributesTask.class.getName(), "Rebuild Repository Atributes" );
        serviceNames.put( ClearCacheTask.class.getName(), "Clear Repository Caches" );
        serviceNames.put( SnapshotRemoverTask.class.getName(), "Remove Snapshots From Repository" );
    }

    /**
     * Standard constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getServiceTypeName( String serviceTypeId )
    {
        if ( serviceNames.containsKey( serviceTypeId ) )
        {
            return serviceNames.get( serviceTypeId );
        }
        else
        {
            return serviceTypeId;
        }
    }

    protected String getScheduleShortName( Schedule schedule )
    {
        if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_ONCE;
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
    
    public NexusTask<Object> getModelNexusTask( ScheduledServiceBaseResource model )
    {
        String serviceType = model.getServiceType();
        
        NexusTask<Object> task = (NexusTask<Object>) lookup( model.getServiceType() );
        
        for ( Iterator iter = model.getServiceProperties().iterator(); iter.hasNext(); )
        {
            ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();
            task.addParameter( prop.getId(), prop.getValue() );
        }
        
        return task;        
    }
    
    public Schedule getModelSchedule( ScheduledServiceBaseResource model )
    {
        Schedule schedule = null;
        
        if ( ScheduledServiceAdvancedResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new CronSchedule( ( ( ScheduledServiceAdvancedResource ) model ).getCronCommand() );
        }
        else if ( ScheduledServiceMonthlyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new MonthlySchedule( 
                parseDate( ( ( ScheduledServiceMonthlyResource ) model ).getStartDate(), 
                           ( ( ScheduledServiceMonthlyResource ) model ).getRecurringTime() ),
                null,
                translateFrom( ( ( ScheduledServiceMonthlyResource ) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceWeeklyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new WeeklySchedule( 
                 parseDate( ( ( ScheduledServiceWeeklyResource ) model ).getStartDate(), 
                            ( ( ScheduledServiceWeeklyResource ) model ).getRecurringTime() ),
                 null,
                 translateFrom( ( ( ScheduledServiceWeeklyResource ) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceDailyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new DailySchedule( 
                parseDate( ( ( ScheduledServiceDailyResource ) model ).getStartDate(), 
                           ( ( ScheduledServiceDailyResource ) model ).getRecurringTime() ),
                null );
        }
        else if ( ScheduledServiceOnceResource.class.isAssignableFrom( model.getClass() ) )
        {            
            schedule = new OnceSchedule( 
                parseDate( ( ( ScheduledServiceOnceResource ) model ).getStartDate(), 
                           ( ( ScheduledServiceOnceResource ) model ).getStartTime() ) );
        }
        
        return schedule;
    }
    
    private Set<Integer> translateFrom( List list )
    {
        Set<Integer> set = new HashSet<Integer>();
        
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            String next = (String) iter.next();
            try
            {
                
                set.add( Integer.valueOf( next ) );
            }
            catch ( NumberFormatException nfe )
            {
                getLogger().log( Level.SEVERE, "Invalid day being added to schedule - " + next + " - skipping.");
            }
        }
        
        return set;
    }
    
    protected <T> boolean compareSchedules( ScheduledTask<T> task, ScheduledServiceBaseResource resource)
    {
        boolean result = false;
        
        if ( getModelSchedule( resource ).equals( task.getSchedule() ) )
        {
            result = true;
        }
        
        return result;
    }
}
