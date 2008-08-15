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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.RepositoryDependentStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;

public class RepositoryStatusResourceHandler
    extends AbstractRepositoryResourceHandler
{

    public RepositoryStatusResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getRepositoryId()
    {
        return getRequest().getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    protected RepositoryStatusResourceResponse getRepositoryResourceResponse()
    {
        try
        {
            RepositoryStatusResource resource = new RepositoryStatusResource();

            try
            {
                CRepository model = getNexus().readRepository( getRepositoryId() );

                resource.setId( model.getId() );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setFormat( model.getType() );

                resource.setLocalStatus( getRestRepoLocalStatus( model ) );

                if ( REPO_TYPE_PROXIED.equals( resource.getRepoType() ) )
                {
                    resource.setRemoteStatus( getRestRepoRemoteStatus( model ) );

                    resource.setProxyMode( getRestRepoProxyMode( model ) );
                }

            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = getNexus().readRepositoryShadow( getRepositoryId() );

                resource.setId( model.getId() );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setLocalStatus( getRestRepoLocalStatus( model ) );
            }

            RepositoryStatusResourceResponse response = new RepositoryStatusResourceResponse();

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
        return serialize( variant, getRepositoryResourceResponse() );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a repository.
     */
    public void put( Representation representation )
    {
        RepositoryStatusResourceResponse response = (RepositoryStatusResourceResponse) deserialize( new RepositoryStatusResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            try
            {
                RepositoryStatusResource resource = response.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    CRepositoryShadow shadow = getNexus().readRepositoryShadow( getRepositoryId() );

                    shadow.setLocalStatus( resource.getLocalStatus() );

                    getNexus().updateRepositoryShadow( shadow );

                    response = getRepositoryResourceResponse();
                }
                else
                {
                    CRepository normal = getNexus().readRepository( getRepositoryId() );

                    normal.setLocalStatus( resource.getLocalStatus() );

                    if ( AbstractRepositoryResourceHandler.REPO_TYPE_PROXIED.equals( getRestRepoType( normal ) )
                        && resource.getProxyMode() != null )
                    {
                        normal.setProxyMode( resource.getProxyMode() );
                    }

                    // update dependant shadows too
                    for ( CRepositoryShadow shadow : getNexus().listRepositoryShadows() )
                    {
                        if ( normal.getId().equals( shadow.getShadowOf() ) )
                        {
                            shadow.setLocalStatus( resource.getLocalStatus() );

                            getNexus().updateRepositoryShadow( shadow );
                        }
                    }

                    getNexus().updateRepository( normal );

                    response = getRepositoryResourceResponse();

                    for ( CRepositoryShadow shadow : getNexus().listRepositoryShadows() )
                    {
                        if ( normal.getId().equals( shadow.getShadowOf() ) )
                        {
                            RepositoryDependentStatusResource dependent = new RepositoryDependentStatusResource();

                            dependent.setId( shadow.getId() );

                            dependent.setRepoType( getRestRepoType( shadow ) );

                            dependent.setFormat( shadow.getType() );

                            dependent.setLocalStatus( getRestRepoLocalStatus( shadow ) );

                            response.getData().addDependentRepo( dependent );
                        }
                    }
                }

                getResponse().setEntity( serialize( representation, response ) );

            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().log( Level.WARNING, "Repository not found, id=" + getRepositoryId() );

                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
            }
            catch ( ConfigurationException e )
            {
                getLogger().log( Level.WARNING, "Configuration unacceptable, repoId=" + getRepositoryId(), e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration unacceptable, repoId=" + getRepositoryId() + ": " + e.getMessage() );

                getResponse().setEntity( serialize( representation, getNexusErrorResponse( "*", e.getMessage() ) ) );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
        }
    }
}
