package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.jmock.core.stub.ThrowStub;
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
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class TaskScheduleUtil
{

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

    @SuppressWarnings( "unchecked" )
    public static List<ScheduledServiceListResource> getTasks()
        throws IOException
    {
        String serviceURI = "service/local/schedules";
        Response response = RequestFacade.doGetRequest( serviceURI );
        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            response.getEntity().getText(),
            MediaType.APPLICATION_XML );

        ScheduledServiceListResourceResponse scheduleResponse = (ScheduledServiceListResourceResponse) representation
            .getPayload( new ScheduledServiceListResourceResponse() );

        return scheduleResponse.getData();
    }

    public static String getStatus( String name )
        throws Exception
    {
        ScheduledServiceListResource task = getTask( name );
        return task.getLastRunResult();
    }

    /**
     * Blocks while waiting for a task to finish.
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public static ScheduledServiceListResource waitForTask( String name )
        throws Exception
    {
        int maxAttempts = 20;
        long sleep = 200;

        for ( int attempt = 0; attempt < maxAttempts; attempt++ )
        {
            ScheduledServiceListResource task = getTask( name );
            String status = task.getStatus();
            
            if ( !status.equals( "RUNNING" ) )
            {
                return task;
            }
            else
            {
                Thread.sleep( sleep );
            }
        }
        return null;
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

    public static ScheduledServiceListResource runTask( String typeId, ScheduledServicePropertyResource... properties )
        throws Exception
    {
        return runTask( typeId, typeId, properties );
    }

    public static ScheduledServiceListResource runTask( String taskName, String typeId,
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
        Assert.assertTrue( "Unable to create task:" + scheduledTask.getTypeId(), status.isSuccess() );

        String taskId = TaskScheduleUtil.getTask( scheduledTask.getName() ).getId();
        status = TaskScheduleUtil.run( taskId );
        Assert.assertTrue( "Unable to run task:" + scheduledTask.getTypeId(), status.isSuccess() );

        return waitForTask( taskName );
    }

}
