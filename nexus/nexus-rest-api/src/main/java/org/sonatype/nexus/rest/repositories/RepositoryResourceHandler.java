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
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

/**
 * Resource handler for Repository resource.
 * 
 * @author cstamas
 */
public class RepositoryResourceHandler
    extends AbstractRepositoryResourceHandler
{
    /** The repository ID */
    private String repositoryId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.repositoryId = getRequest().getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    protected String getRepositoryId()
    {
        return this.repositoryId;
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
        try
        {
            RepositoryBaseResource resource = null;

            try
            {
                CRepository model = getNexus().readRepository( getRepositoryId() );

                resource = getRepositoryRestModel( model );
            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = getNexus().readRepositoryShadow( getRepositoryId() );

                resource = getRepositoryShadowRestModel( model );
            }

            RepositoryResourceResponse response = new RepositoryResourceResponse();

            response.setData( resource );

            return serialize( variant, response );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.WARNING, "Repository not found, id=" + getRepositoryId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );

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
     * Update a repository.
     */
    public void put( Representation representation )
    {
        RepositoryResourceResponse response = (RepositoryResourceResponse) deserialize( new RepositoryResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            try
            {
                RepositoryBaseResource resource = response.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    try
                    {
                        CRepositoryShadow shadow = getNexus().readRepositoryShadow( getRepositoryId() );

                        shadow = getRepositoryShadowAppModel( (RepositoryShadowResource) resource, shadow );

                        getNexus().updateRepositoryShadow( shadow );

                        return;
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        getLogger().log( Level.WARNING, "Virtual repository not found, id=" + getRepositoryId() );

                        getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Virtual repository Not Found" );

                        return;
                    }
                }
                else
                {
                    try
                    {
                        CRepository normal = getNexus().readRepository( getRepositoryId() );

                        normal = getRepositoryAppModel( (RepositoryResource) resource, normal );

                        getNexus().updateRepository( normal );

                        return;
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        getLogger().log( Level.WARNING, "Repository not found, id=" + getRepositoryId() );

                        getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );

                        return;
                    }
                }
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e, representation );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
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
     * Delete a repository.
     */
    public void delete()
    {
        try
        {
            try
            {
                getNexus().deleteRepository( getRepositoryId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                getNexus().deleteRepositoryShadow( getRepositoryId() );
            }
        }
        catch ( ConfigurationException e )
        {
            getLogger().log( Level.WARNING, "Repository not deletable, it has dependants, id=" + getRepositoryId() );

            getResponse()
                .setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Repository is not deletable, it has dependants." );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.WARNING, "Repository not found, id=" + getRepositoryId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );
        }
    }

    // ===================================================================================
}
