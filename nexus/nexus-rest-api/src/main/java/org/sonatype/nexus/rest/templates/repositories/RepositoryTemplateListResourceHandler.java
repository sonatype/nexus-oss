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
package org.sonatype.nexus.rest.templates.repositories;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
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
public class RepositoryTemplateListResourceHandler
    extends AbstractRepositoryTemplateResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryTemplateListResourceHandler( Context context, Request request, Response response )
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
        try
        {
            RepositoryListResourceResponse response = new RepositoryListResourceResponse();

            RepositoryListResource repoRes;

            Collection<CRepository> repositories = getNexus().listRepositoryTemplates();

            for ( CRepository repository : repositories )
            {
                repoRes = new RepositoryListResource();

                repoRes.setResourceURI( calculateSubReference( repository.getId() ).toString() );

                if ( repository.getRemoteStorage() != null && repository.getRemoteStorage().getUrl() != null )
                {
                    repoRes.setRepoType( "proxy" );
                }
                else
                {
                    repoRes.setRepoType( "hosted" );
                }

                repoRes.setName( repository.getName() );

                repoRes.setEffectiveLocalStorageUrl( repository.getLocalStorage() != null
                    && repository.getLocalStorage().getUrl() != null
                    ? repository.getLocalStorage().getUrl()
                    : repository.defaultLocalStorageUrl );

                repoRes.setRepoPolicy( repository.getRepositoryPolicy() );

                response.addData( repoRes );
            }

            Collection<CRepositoryShadow> shadows = getNexus().listRepositoryShadowTemplates();

            for ( CRepositoryShadow shadow : shadows )
            {
                repoRes = new RepositoryListResource();

                repoRes.setResourceURI( calculateSubReference( shadow.getId() ).toString() );

                repoRes.setRepoType( "virtual" );

                repoRes.setName( shadow.getName() );

                repoRes.setEffectiveLocalStorageUrl( shadow.defaultLocalStorageUrl );

                response.addData( repoRes );
            }

            return serialize( variant, response );
        }
        catch ( IOException e )
        {
            getLogger().log( Level.SEVERE, "Got IO exception while listing repository templates!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL, e.getMessage() );

            return null;
        }
    }

    /**
     * This resource allows POST.
     */
    public boolean allowPost()
    {
        return true;
    }

    /**
     * Create template.
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
            try
            {
                RepositoryBaseResource resource = response.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    CRepositoryShadow shadow = getNexus().readRepositoryShadowTemplate( resource.getId() );

                    if ( shadow == null )
                    {
                        shadow = getRepositoryShadowAppModel( (RepositoryShadowResource) resource, null );

                        getNexus().createRepositoryShadowTemplate( shadow );

                        return;
                    }
                    else
                    {
                        getLogger().log(
                            Level.WARNING,
                            "Virtual repository template with ID=" + resource.getId() + " already exists!" );

                        getResponse().setStatus(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            "Virtual repository template with ID=" + resource.getId() + " already exists!" );

                        getResponse().setEntity(
                            serialize( representation, getNexusErrorResponse(
                                "id",
                                "Virtual repository with id=" + resource.getId() + " already exists!" ) ) );

                        return;
                    }
                }
                else
                {
                    CRepository normal = getNexus().readRepositoryTemplate( resource.getId() );

                    if ( normal == null )
                    {
                        normal = getRepositoryAppModel( (RepositoryResource) resource, null );

                        getNexus().createRepositoryTemplate( normal );

                        return;
                    }
                    else
                    {
                        getLogger().log(
                            Level.WARNING,
                            "Repository template with ID=" + resource.getId() + " already exists!" );

                        getResponse().setStatus(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            "Repository template with ID=" + resource.getId() + " already exists!" );

                        getResponse().setEntity(
                            serialize( representation, getNexusErrorResponse(
                                "id",
                                "Repository with id=" + resource.getId() + " already exists!" ) ) );

                        return;
                    }
                }
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
        }
    }

}
