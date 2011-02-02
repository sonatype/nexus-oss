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
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class TaskScheduleUtil
{
    private static final Logger LOG = Logger.getLogger( TaskScheduleUtil.class );

    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
    }

    public static Status create( ScheduledServiceBaseResource task )
        throws IOException
    {
        ScheduledServiceResourceResponse request = new ScheduledServiceResourceResponse();
        request.setData( task );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        String serviceURI = "service/local/schedules";
        Response response = RequestFacade.sendMessage( serviceURI, Method.POST, representation );

        return response.getStatus();
    }

    public static ScheduledServiceListResource getTask( String name )
        throws Exception
    {
        List<ScheduledServiceListResource> list = getTasks();
        for ( ScheduledServiceListResource task : list )
        {
            if ( name.equals( task.getName() ) )
            {
                return task;
            }
        }

        return null;
    }

    public static List<ScheduledServiceListResource> getTasks()
        throws IOException
    {
        return getTaskRequest( "service/local/schedules" );
    }

    public static List<ScheduledServiceListResource> getAllTasks()
        throws IOException
    {
        return getTaskRequest( "service/local/schedules?allTasks=true" );
    }

    private static List<ScheduledServiceListResource> getTaskRequest( String uri )
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( uri );

        if ( response.getStatus().isError() )
        {
            LOG.error( response.getStatus().toString() );
            return Collections.emptyList();
        }

        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );

        ScheduledServiceListResourceResponse scheduleResponse =
            (ScheduledServiceListResourceResponse) representation.getPayload( new ScheduledServiceListResourceResponse() );

        return scheduleResponse.getData();
    }

    public static String getStatus( String name )
        throws Exception
    {
        ScheduledServiceListResource task = getTask( name );
        return task.getLastRunResult();
    }

    public static void deleteAllTasks()
        throws Exception
    {
        List<ScheduledServiceListResource> tasks = getAllTasks();

        for ( ScheduledServiceListResource task : tasks )
        {
            deleteTask( task.getId() );
        }
    }

    public static void waitForAllTasksToStop()
        throws Exception
    {
        waitForAllTasksToStop( 300 );
    }

    public static void waitForAllTasksToStop( String taskType )
        throws Exception
    {
        waitForAllTasksToStop( 300, taskType );
    }

    public static void waitForAllTasksToStop( int maxAttempts )
        throws Exception
    {
        waitForAllTasksToStop( maxAttempts, null );
    }

    public static void waitForAllTasksToStop( Class<? extends NexusTask<?>> taskClass )
        throws Exception
    {
        waitForAllTasksToStop( taskClass.getSimpleName() );
    }

    public static void waitForAllTasksToStop( int maxAttempts, String taskType )
        throws Exception
    {
        String uri = "service/local/taskhelper?attempts=" + maxAttempts;
        if ( taskType != null )
        {
            uri += "&taskType=" + taskType;
        }

        final Response response = RequestFacade.doGetRequest( uri );

        if ( response.getStatus().getCode() != Status.SUCCESS_OK.getCode() )
        {
            throw new IOException( "The taskhelper REST resource reported an error (" + response.getStatus().toString()
                + "), bailing out!" );
        }
    }

    /**
     * Blocks while waiting for a task to finish.
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public static void waitForTask( String name, int maxAttempts )
        throws Exception
    {

        if ( maxAttempts == 0 )
        {
            return;
        }

        String uri = "service/local/taskhelper?attempts=" + maxAttempts;

        if ( name != null )
        {
            uri += "&name=" + name;
        }

        final Response response = RequestFacade.doGetRequest( uri );

        if ( response.getStatus().getCode() != Status.SUCCESS_OK.getCode() )
        {
            throw new IOException( "The taskhelper REST resource reported an error (" + response.getStatus().toString()
                + "), bailing out!" );
        }
    }

    public static Status update( ScheduledServiceBaseResource task )
        throws IOException
    {
        ScheduledServiceResourceResponse request = new ScheduledServiceResourceResponse();
        request.setData( task );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        String serviceURI = "service/local/schedules/" + task.getId();
        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );

        return response.getStatus();
    }

    public static Status deleteTask( String id )
        throws IOException
    {
        String serviceURI = "service/local/schedules/" + id;
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );

        return response.getStatus();
    }

    public static Status run( String taskId )
        throws IOException
    {
        String serviceURI = "service/local/schedule_run/" + taskId;
        Response response = RequestFacade.doGetRequest( serviceURI );

        return response.getStatus();
    }

    public static void runTask( String typeId, ScheduledServicePropertyResource... properties )
        throws Exception
    {
        runTask( typeId, typeId, properties );
    }

    public static void runTask( String taskName, String typeId, ScheduledServicePropertyResource... properties )
        throws Exception
    {
        runTask( taskName, typeId, 300, properties );
    }

    public static void runTask( String taskName, String typeId, int maxAttempts,
                                ScheduledServicePropertyResource... properties )
        throws Exception
    {
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( taskName );
        scheduledTask.setTypeId( typeId );
        scheduledTask.setSchedule( "manual" );

        for ( ScheduledServicePropertyResource property : properties )
        {
            scheduledTask.addProperty( property );
        }

        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertTrue( status.isSuccess(), "Unable to create task:" + scheduledTask.getTypeId() );

        String taskId = TaskScheduleUtil.getTask( scheduledTask.getName() ).getId();
        status = TaskScheduleUtil.run( taskId );
        Assert.assertTrue( status.isSuccess(), "Unable to run task:" + scheduledTask.getTypeId() );

        waitForTask( taskName, maxAttempts );
    }

    public static ScheduledServicePropertyResource newProperty( String name, String value )
    {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( name );
        prop.setValue( value );
        return prop;
    }
}
