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
import java.util.Collection;
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
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

/**
 * A resource list for Repository list.
 * 
 * @author cstamas
 */
public class RepositoryListResourceHandler
    extends AbstractRepositoryResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Repositories by getting the from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        RepositoryListResourceResponse response = new RepositoryListResourceResponse();

        RepositoryListResource repoRes;

        Collection<CRepository> repositories = getNexus().listRepositories();

        for ( CRepository repository : repositories )
        {
            repoRes = new RepositoryListResource();

            repoRes.setResourceURI( calculateSubReference( repository.getId() ).getPath() );

            repoRes.setRepoType( getRestRepoType( repository ) );

            repoRes.setName( repository.getName() );

            repoRes.setEffectiveLocalStorageUrl( repository.getLocalStorage() != null
                && repository.getLocalStorage().getUrl() != null
                ? repository.getLocalStorage().getUrl()
                : repository.defaultLocalStorageUrl );

            repoRes.setRepoPolicy( getRestRepoPolicy( repository ) );

            if ( REPO_TYPE_PROXIED.equals( repoRes.getRepoType() ) )
            {
                if ( repository.getRemoteStorage() != null)
                {
                    repoRes.setRemoteUri( repository.getRemoteStorage().getUrl() );
                }
            }

            response.addData( repoRes );
        }

        Collection<CRepositoryShadow> shadows = getNexus().listRepositoryShadows();

        for ( CRepositoryShadow shadow : shadows )
        {
            repoRes = new RepositoryListResource();

            repoRes.setResourceURI( calculateSubReference( shadow.getId() ).getPath() );

            repoRes.setRepoType( getRestRepoType( shadow ) );

            repoRes.setName( shadow.getName() );

            repoRes.setEffectiveLocalStorageUrl( shadow.defaultLocalStorageUrl );

            response.addData( repoRes );
        }

        return serialize( variant, response );
    }

    /**
     * This resource allows POST.
     */
    public boolean allowPost()
    {
        return true;
    }

    /**
     * The create repo handler.
     */
    public void post( Representation representation )
    {
        RepositoryResourceResponse response = (RepositoryResourceResponse) deserialize( new RepositoryResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            RepositoryBaseResource resource = response.getData();

            try
            {
                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    try
                    {
                        CRepositoryShadow shadow = getNexus().readRepositoryShadow( resource.getId() );

                        if ( shadow != null )
                        {
                            getLogger().log(
                                Level.INFO,
                                "Virtual repository with ID=" + resource.getId() + " already exists!" );

                            getResponse().setStatus(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Virtual repository with id=" + resource.getId() + " already exists!" );

                            getResponse().setEntity(
                                serialize( representation, getNexusErrorResponse(
                                    "id",
                                    "Virtual repository with id=" + resource.getId() + " already exists!" ) ) );

                            return;
                        }
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        CRepositoryShadow shadow = getRepositoryShadowAppModel(
                            (RepositoryShadowResource) resource,
                            null );

                        getNexus().createRepositoryShadow( shadow );

                        return;
                    }
                }
                else
                {
                    try
                    {
                        CRepository normal = getNexus().readRepository( resource.getId() );

                        if ( normal != null )
                        {
                            getLogger().log(
                                Level.INFO,
                                "Repository with ID=" + resource.getId() + " already exists!" );

                            getResponse().setStatus(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Repository with id=" + resource.getId() + " already exists!" );

                            getResponse().setEntity(
                                serialize( representation, getNexusErrorResponse(
                                    "id",
                                    "Repository with id=" + resource.getId() + " already exists!" ) ) );

                            return;
                        }
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        CRepository normal = getRepositoryAppModel( (RepositoryResource) resource, null );

                        getNexus().createRepository( normal );

                        return;
                    }
                }
            }
            catch ( ConfigurationException e )
            {
                getLogger().log( Level.INFO, "Configuration unacceptable, repoId=" + resource.getId(), e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration unacceptable, repoId=" + resource.getId() );

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
