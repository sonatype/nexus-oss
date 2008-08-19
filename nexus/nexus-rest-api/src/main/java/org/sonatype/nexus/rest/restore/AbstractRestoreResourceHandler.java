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
package org.sonatype.nexus.rest.restore;

import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.scheduling.SchedulerTask;

public abstract class AbstractRestoreResourceHandler
    extends AbstractNexusResourceHandler
{
    public static final String DOMAIN = "domain";

    public static final String DOMAIN_REPOSITORIES = "repositories";

    public static final String DOMAIN_REPO_GROUPS = "repo_groups";

    public static final String TARGET_ID = "target";

    private final String repositoryId;

    private final String repositoryGroupId;

    private final String resourceStorePath;

    public AbstractRestoreResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        String repoId = null;

        String groupId = null;

        if ( getRequest().getAttributes().containsKey( DOMAIN ) && getRequest().getAttributes().containsKey( TARGET_ID ) )
        {
            if ( DOMAIN_REPOSITORIES.equals( getRequest().getAttributes().get( DOMAIN ) ) )
            {
                repoId = getRequest().getAttributes().get( TARGET_ID ).toString();
            }
            else if ( DOMAIN_REPO_GROUPS.equals( getRequest().getAttributes().get( DOMAIN ) ) )
            {
                groupId = getRequest().getAttributes().get( TARGET_ID ).toString();
            }
        }

        repositoryId = repoId;

        repositoryGroupId = groupId;

        String path = null;

        if ( getRepositoryId() != null || getRepositoryGroupId() != null )
        {
            path = getRequest().getResourceRef().getRemainingPart();

            // get rid of query part
            if ( path.contains( "?" ) )
            {
                path = path.substring( 0, path.indexOf( '?' ) );
            }

            // get rid of reference part
            if ( path.contains( "#" ) )
            {
                path = path.substring( 0, path.indexOf( '#' ) );
            }

            if ( StringUtils.isEmpty( path ) )
            {
                path = "/";
            }
        }

        this.resourceStorePath = path;
    }

    protected String getRepositoryId()
    {
        return repositoryId;
    }

    protected String getRepositoryGroupId()
    {
        return repositoryGroupId;
    }

    protected String getResourceStorePath()
    {
        return resourceStorePath;
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    public void handleDelete( SchedulerTask<?> task )
    {
        try
        {
            // check reposes
            if ( repositoryGroupId != null )
            {
                getNexus().readRepositoryGroup( repositoryGroupId );
            }
            else if ( repositoryId != null )
            {
                try
                {
                    getNexus().readRepository( repositoryId );
                }
                catch ( NoSuchRepositoryException e )
                {
                    getNexus().readRepositoryShadow( repositoryId );
                }
            }

            getNexus().submit( "Internal", task );
            
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( RejectedExecutionException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
        }
        catch ( NoSuchRepositoryException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }
}
