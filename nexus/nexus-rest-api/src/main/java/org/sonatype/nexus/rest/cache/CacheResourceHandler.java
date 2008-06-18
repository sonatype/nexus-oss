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
package org.sonatype.nexus.rest.cache;

import java.io.IOException;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.restore.AbstractRestoreResourceHandler;
import org.sonatype.nexus.tasks.ClearCacheTask;

/**
 * @author cstamas
 */
public class CacheResourceHandler
    extends AbstractRestoreResourceHandler
{
    protected final String resourceStorePath;

    public CacheResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

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

    public boolean allowGet()
    {
        return true;
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            NFCResource resource = new NFCResource();

            // check reposes
            if ( getRepositoryGroupId() != null )
            {
                for ( Repository repository : getNexus().getRepositoryGroup( getRepositoryGroupId() ) )
                {
                    NFCRepositoryResource repoNfc = new NFCRepositoryResource();

                    repoNfc.setRepositoryId( repository.getId() );

                    repoNfc.getNfcPaths().addAll( repository.getNotFoundCache().listKeysInCache() );

                    resource.addNfcContent( repoNfc );
                }
            }
            else if ( getRepositoryId() != null )
            {
                Repository repository = getNexus().getRepository( getRepositoryId() );

                NFCRepositoryResource repoNfc = new NFCRepositoryResource();

                repoNfc.setRepositoryId( repository.getId() );

                repoNfc.getNfcPaths().addAll( repository.getNotFoundCache().listKeysInCache() );

                resource.addNfcContent( repoNfc );
            }

            NFCResourceResponse response = new NFCResourceResponse();

            response.setData( resource );

            return serialize( variant, response );
        }
        catch ( NoSuchRepositoryException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

            return null;
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

            return null;
        }
    }

    public boolean allowDelete()
    {
        return true;
    }

    public void delete()
    {
        ClearCacheTask task = (ClearCacheTask) getNexus().createTaskInstance( ClearCacheTask.class );

        task.setRepositoryId( getRepositoryId() );

        task.setRepositoryGroupId( getRepositoryGroupId() );

        task.setResourceStorePath( resourceStorePath );

        super.handleDelete( task );
    }

}
