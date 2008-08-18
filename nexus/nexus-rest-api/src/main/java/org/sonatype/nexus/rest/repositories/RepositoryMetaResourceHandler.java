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
package org.sonatype.nexus.rest.repositories;

import java.util.logging.Level;

import org.codehaus.plexus.util.FileUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.cache.CacheStatistics;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;

public class RepositoryMetaResourceHandler
    extends AbstractRepositoryResourceHandler
{

    public RepositoryMetaResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getRepositoryId()
    {
        return getRequest().getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    protected RepositoryMetaResourceResponse getRepositoryMetaResourceResponse()
    {
        try
        {
            RepositoryMetaResource resource = new RepositoryMetaResource();

            Repository repository = getNexus().getRepository( getRepositoryId() );

            String localPath = repository.getLocalUrl().substring( repository.getLocalUrl().indexOf( "file:" ) + 1 );

            // TODO: clean this up, ot at least centralize somewhere!
            // a stupid trick here
            try
            {
                CRepository model = getNexus().readRepository( getRepositoryId() );

                resource.setRepoType( getRestRepoType( model ) );
                
                resource.setFormat( model.getType() );
            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = getNexus().readRepositoryShadow( getRepositoryId() );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setFormat( model.getType() );
            }

            resource.setId( getRepositoryId() );
            
            try
            {
                resource.setSizeOnDisk( FileUtils.sizeOfDirectory( localPath ) );

                resource.setFileCountInRepository( org.sonatype.nexus.util.FileUtils.filesInDirectory( localPath ) );
            }
            catch ( IllegalArgumentException e )
            {
                // the repo is maybe virgin, so the dir is not created until some request needs it
            }

            // mustang is able to get this with File.getUsableFreeSpace();
            resource.setFreeSpaceOnDisk( -1 );

            CacheStatistics stats = repository.getNotFoundCache().getStatistics();

            resource.setNotFoundCacheSize( stats.getSize() );

            resource.setNotFoundCacheHits( stats.getHits() );

            resource.setNotFoundCacheMisses( stats.getMisses() );

            resource.setLocalStorageErrorsCount( 0 );

            resource.setRemoteStorageErrorsCount( 0 );

            RepositoryMetaResourceResponse response = new RepositoryMetaResourceResponse();

            response.setData( resource );

            return response;
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.WARNING, "Repository not found, id=" + getRepositoryId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );

            return null;
        }
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        return serialize( variant, getRepositoryMetaResourceResponse() );
    }

}
