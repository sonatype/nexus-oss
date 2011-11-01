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
package org.sonatype.nexus.test.utils;

import static org.sonatype.nexus.test.utils.ResponseMatchers.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Util class to talk with nexus tasks
 */
public class TaskScheduleUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskScheduleUtil.class );

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
        return RequestFacade.doPostForStatus( serviceURI, representation );
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

    /**
     * @return only tasks visible from nexus UI
     */
    public static List<ScheduledServiceListResource> getTasks()
        throws IOException
    {
        return getTaskRequest( "service/local/schedules" );
    }

    /**
     * @return all tasks, even internal ones
     */
    public static List<ScheduledServiceListResource> getAllTasks()
        throws IOException
    {
        return getTaskRequest( "service/local/schedules?allTasks=true" );
    }

    private static List<ScheduledServiceListResource> getTaskRequest( String uri )
        throws IOException
    {
        try
        {
            String entityText = RequestFacade.doGetForText( uri, isSuccessful() );
            XStreamRepresentation representation =
                new XStreamRepresentation( xstream, entityText, MediaType.APPLICATION_XML );

            ScheduledServiceListResourceResponse scheduleResponse =
                (ScheduledServiceListResourceResponse) representation.getPayload( new ScheduledServiceListResourceResponse() );

            return scheduleResponse.getData();
        }
        catch ( AssertionError e )
        {
            // unsuccessful GET
            LOG.error( e.getMessage() , e );
            return Collections.emptyList();
        }

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

    /**
     * Holds execution until all tasks stop running
     */
    public static void waitForAllTasksToStop()
        throws Exception
    {
        waitForAllTasksToStop( 300 );
    }

    /**
     * Holds execution until all tasks of a given type stop running
     * 
     * @param taskType task type
     */
    public static void waitForAllTasksToStop( String taskType )
        throws Exception
    {
        waitForAllTasksToStop( 300, taskType );
    }

    /**
     * Holds execution until all tasks of a given type stop running
     * 
     * @param maxAttempts how many times check for tasks being stopped
     */
    public static void waitForAllTasksToStop( int maxAttempts )
        throws Exception
    {
        waitForAllTasksToStop( maxAttempts, null );
    }

    /**
     * Holds execution until all tasks of a given type stop running
     * 
     * @param taskClass task type
     */
    public static void waitForAllTasksToStop( Class<? extends NexusTask<?>> taskClass )
        throws Exception
    {
        waitForAllTasksToStop( taskClass.getSimpleName() );
    }

    /**
     * Holds execution until all tasks of a given type stop running
     * 
     * @param taskType task type
     * @param maxAttempts how many times check for tasks being stopped
     */
    public static void waitForAllTasksToStop( int maxAttempts, String taskType )
        throws Exception
    {
        String uri = "service/local/taskhelper?attempts=" + maxAttempts;
        if ( taskType != null )
        {
            uri += "&taskType=" + taskType;
        }

        final Status status = RequestFacade.doGetForStatus( uri );

        if ( !status.isSuccess() )
        {
            throw new IOException( "The taskhelper REST resource reported an error (" + status.toString()
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

        final Status status = RequestFacade.doGetForStatus( uri );

        if ( !status.isSuccess() )
        {
            throw new IOException( "The taskhelper REST resource reported an error (" + status.toString()
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
        return RequestFacade.doPutForStatus( serviceURI, representation, null );
    }

    public static Status deleteTask( String id )
        throws IOException
    {
        String serviceURI = "service/local/schedules/" + id;
        return RequestFacade.doDeleteForStatus( serviceURI, null );
    }

    public static Status run( String taskId )
        throws IOException
    {
        String serviceURI = "service/local/schedule_run/" + taskId;
        return RequestFacade.doGetForStatus( serviceURI );
    }

    public static Status cancel( String taskId )
        throws IOException
    {
        String serviceURI = "service/local/schedule_run/" + taskId;
        return RequestFacade.doDeleteForStatus( serviceURI, null );
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
