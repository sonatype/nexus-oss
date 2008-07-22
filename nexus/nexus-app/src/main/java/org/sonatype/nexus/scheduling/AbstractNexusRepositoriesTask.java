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
package org.sonatype.nexus.scheduling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

public abstract class AbstractNexusRepositoriesTask<T>
    extends AbstractNexusTask<T>
{
    public static final String REPOSITORY_OR_GROUP_ID_KEY = "repositoryOrGroupId";

    private static final String REPO_PREFIX = "repo_";

    private static final String GROUP_PREFIX = "group_";

    public String getRepositoryId()
    {
        String param = getParameters().get( REPOSITORY_OR_GROUP_ID_KEY );
        if ( param != null && param.startsWith( REPO_PREFIX ) )
        {
            return param.substring( REPO_PREFIX.length() );
        }

        return null;
    }

    public void setRepositoryId( String repositoryId )
    {
        if ( !StringUtils.isEmpty( repositoryId ) )
        {
            getParameters().put( REPOSITORY_OR_GROUP_ID_KEY, REPO_PREFIX + repositoryId );
        }
    }

    public String getRepositoryGroupId()
    {
        String param = getParameters().get( REPOSITORY_OR_GROUP_ID_KEY );
        if ( param != null && param.startsWith( GROUP_PREFIX ) )
        {
            return param.substring( GROUP_PREFIX.length() );
        }

        return null;
    }

    public void setRepositoryGroupId( String repositoryGroupId )
    {
        if ( !StringUtils.isEmpty( repositoryGroupId ) )
        {
            getParameters().put( REPOSITORY_OR_GROUP_ID_KEY, GROUP_PREFIX + repositoryGroupId );
        }
    }

    public boolean allowConcurrentExecution( Map<Class<?>, List<ScheduledTask<?>>> activeTasks )
    {
        return getSetOfIntersectingTasksThatRuns( activeTasks ).isEmpty();
    }

    protected Set<AbstractNexusRepositoriesTask<?>> getSetOfIntersectingTasksThatRuns(
        Map<Class<?>, List<ScheduledTask<?>>> activeTasks )
    {
        HashSet<AbstractNexusRepositoriesTask<?>> result = new HashSet<AbstractNexusRepositoriesTask<?>>();

        // get all activeTasks that runs and are descendants of AbstractNexusRepositoriesTask
        for ( Class<?> taskCls : activeTasks.keySet() )
        {
            if ( AbstractNexusRepositoriesTask.class.isAssignableFrom( taskCls ) )
            {
                List<ScheduledTask<?>> tasks = activeTasks.get( taskCls );

                for ( ScheduledTask<?> task : tasks )
                {
                    // check against RUNNING intersection
                    if ( TaskState.RUNNING.equals( task.getTaskState() )
                        && repositorySetIntersectionIsNotEmpty( (AbstractNexusRepositoriesTask<?>) task ) )
                    {
                        result.add( (AbstractNexusRepositoriesTask<?>) task );
                    }
                }
            }
        }

        return result;
    }

    protected boolean repositorySetIntersectionIsNotEmpty( AbstractNexusRepositoriesTask<?> otherTask )
    {
        // simplest cases, checking for repoId and groupId equality
        if ( getRepositoryId() != null && otherTask.getRepositoryId() != null
            && StringUtils.equals( getRepositoryId(), otherTask.getRepositoryId() ) )
        {
            return true;
        }

        if ( getRepositoryGroupId() != null && otherTask.getRepositoryGroupId() != null
            && StringUtils.equals( getRepositoryGroupId(), otherTask.getRepositoryGroupId() ) )
        {
            return true;
        }

        try
        {
            // complex case: repoA may be in both groupA and groupB as member
            // so we actually evaluate all tackled reposes for both task and have intersected those
            List<Repository> thisReposes = new ArrayList<Repository>();

            if ( getRepositoryId() != null )
            {
                thisReposes.add( getNexus().getRepository( getRepositoryId() ) );
            }
            else
            {
                thisReposes.addAll( getNexus().getRepositoryGroup( getRepositoryGroupId() ) );
            }

            List<Repository> otherReposes = new ArrayList<Repository>();

            if ( otherTask.getRepositoryId() != null )
            {
                otherReposes.add( getNexus().getRepository( otherTask.getRepositoryId() ) );
            }
            else
            {
                otherReposes.addAll( getNexus().getRepositoryGroup( otherTask.getRepositoryGroupId() ) );
            }

            HashSet<Repository> testSet = new HashSet<Repository>();
            testSet.addAll( thisReposes );
            testSet.addAll( otherReposes );

            // the set does not intersects
            return thisReposes.size() + otherReposes.size() != testSet.size();
        }
        catch ( NoSuchResourceStoreException e )
        {
            // in this case, one of the tasks will die anyway, let's say false
            return false;
        }
    }
}
