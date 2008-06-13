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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

public class ScheduledServiceResourceHandler
    extends AbstractScheduledServiceResourceHandler
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

    /** The scheduledService ID */
    private String scheduledServiceId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.scheduledServiceId = getRequest().getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }

    protected String getScheduledServiceId()
    {
        return this.scheduledServiceId;
    }

    /**
     * We are handling HTTP GET's.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository route representation.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            ScheduledServiceResourceResponse response = new ScheduledServiceResourceResponse();

            ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );

            ScheduledServiceBaseResource resource = null;

            if ( OnceSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
            {
                resource = new ScheduledServiceOnceResource();

                OnceSchedule taskSchedule = (OnceSchedule) task.getSchedule();
                ScheduledServiceOnceResource res = (ScheduledServiceOnceResource) resource;

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

                res.setCronCommand( taskSchedule.getCronExpression() );
            }

            if ( resource != null )
            {
                resource.setId( task.getId() );
                resource.setEnabled( task.isEnabled() );
                resource.setName( task.getName() );
                resource.setSchedule( getScheduleShortName( task.getSchedule() ) );
                resource.setTypeId( task.getType() );
                resource.setProperties( formatServiceProperties( task.getTaskParams() ) );

                response.setData( resource );

                return serialize( variant, response );
            }

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Invalid schedule, can't load task." );

            return null;
        }
        catch ( NoSuchTaskException e )
        {
            getResponse().setStatus(
                Status.CLIENT_ERROR_NOT_FOUND,
                "There is no task with ID=" + getScheduledServiceId() );

            return null;
        }
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a repository route.
     */
    public void put( Representation representation )
    {
        ScheduledServiceResourceResponse response = (ScheduledServiceResourceResponse) deserialize( new ScheduledServiceResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            ScheduledServiceBaseResource resource = response.getData();

            try
            {
                // currently we allow editing of:
                // task name
                // task schedule (even to another type)
                // task params
                ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );
                
                task.setEnabled( resource.isEnabled() );

                task.setName( getModelName( resource ) );

                task.setSchedule( getModelSchedule( resource ) );

                for ( Iterator iter = resource.getProperties().iterator(); iter.hasNext(); )
                {
                    ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();

                    task.getTaskParams().put( prop.getId(), prop.getValue() );
                }

                task.reset();
            }
            catch ( NoSuchTaskException e )
            {
                getLogger().log( Level.SEVERE, "Unable to locate task id:" + resource.getId(), e );
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
            }
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a task.
     */
    public void delete()
    {
        try
        {
            getNexus().getTaskById( getScheduledServiceId() ).cancel();
        }
        catch ( NoSuchTaskException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }
    }
}
