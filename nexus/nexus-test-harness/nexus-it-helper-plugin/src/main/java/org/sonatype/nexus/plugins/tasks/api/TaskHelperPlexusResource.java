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
package org.sonatype.nexus.plugins.tasks.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.RunNowSchedule;

@Component( role = PlexusResource.class, hint = "TaskHelperResource" )
public class TaskHelperPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    private NexusScheduler nexusScheduler;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return "/taskhelper";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();
        String name = form.getFirstValue( "name" );
        String taskType = form.getFirstValue( "taskType" );
        String attemptsParam = form.getFirstValue( "attempts" );
        int attempts = 300;

        if ( attemptsParam != null )
        {
            try
            {
                attempts = Integer.parseInt( attemptsParam );
            }
            catch ( NumberFormatException e )
            {
                // ignore, will use default of 300
            }
        }

        ScheduledTask<?> namedTask = null;

        if ( name != null )
        {
            namedTask = getTaskByName( name );

            if ( namedTask == null )
            {
                // task wasn't found, so bounce on outta here
                response.setStatus( Status.SUCCESS_OK );
                return "OK";
            }
        }

        for ( int i = 0; i < attempts; i++ )
        {
            try
            {
                Thread.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
            }

            if ( namedTask != null )
            {
                if ( isTaskCompleted( namedTask ) )
                {
                    response.setStatus( Status.SUCCESS_OK );
                    return "OK";
                }
            }
            else
            {
                Set<ScheduledTask<?>> tasks = getTasks( taskType );

                boolean running = false;

                for ( ScheduledTask<?> task : tasks )
                {
                    if ( !isTaskCompleted( task ) )
                    {
                        running = true;
                        break;
                    }
                }

                if ( !running )
                {
                    response.setStatus( Status.SUCCESS_OK );
                    return "OK";
                }
            }
        }

        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return "Tasks Not Finished";
    }

    private boolean isTaskCompleted( ScheduledTask<?> task )
    {
        return TaskState.WAITING.equals( task.getTaskState() )
            || TaskState.FINISHED.equals( task.getTaskState() )
            || TaskState.BROKEN.equals( task.getTaskState() )
            || TaskState.CANCELLED.equals( task.getTaskState() );
    }

    private ScheduledTask<?> getTaskByName( String name )
    {
        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getAllTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                if ( task.getName().equals( name ) )
                {
                    return task;
                }
            }
        }

        return null;
    }

    private Set<ScheduledTask<?>> getTasks( String taskType )
    {
        Set<ScheduledTask<?>> tasks = new HashSet<ScheduledTask<?>>();

        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getAllTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                if ( taskType == null || task.getType().equals( taskType ) )
                {
                    tasks.add( task );
                }
            }
        }

        return tasks;
    }
}
