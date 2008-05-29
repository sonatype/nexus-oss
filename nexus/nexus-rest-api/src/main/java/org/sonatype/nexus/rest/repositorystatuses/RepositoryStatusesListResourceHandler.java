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
package org.sonatype.nexus.rest.repositorystatuses;

import java.util.Collection;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.rest.model.RepositoryStatusListResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryResourceHandler;

/**
 * A resource list for Repository list.
 * 
 * @author cstamas
 */
public class RepositoryStatusesListResourceHandler
    extends AbstractRepositoryResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryStatusesListResourceHandler( Context context, Request request, Response response )
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
        RepositoryStatusListResourceResponse response = new RepositoryStatusListResourceResponse();

        RepositoryStatusListResource repoRes;

        Collection<CRepository> repositories = getNexus().listRepositories();

        for ( CRepository repository : repositories )
        {
            repoRes = new RepositoryStatusListResource();

            repoRes.setResourceURI( calculateSubReference( repository.getId() ).toString() );
            
            repoRes.setId( repository.getId() );

            repoRes.setName( repository.getName() );

            repoRes.setRepoType( getRestRepoType( repository ) );
            
            repoRes.setRepoPolicy( getRestRepoPolicy( repository ) );
            
            repoRes.setFormat( repository.getType() );
            
            repoRes.setStatus( new RepositoryStatusResource() );

            repoRes.getStatus().setLocalStatus( getRestRepoLocalStatus( repository ) );

            if ( REPO_TYPE_PROXIED.equals( getRestRepoType( repository ) ) )
            {
                repoRes.getStatus().setRemoteStatus( getRestRepoRemoteStatus( repository ) );

                repoRes.getStatus().setProxyMode( getRestRepoProxyMode( repository ) );
            }

            response.addData( repoRes );
        }

        Collection<CRepositoryShadow> shadows = getNexus().listRepositoryShadows();

        for ( CRepositoryShadow shadow : shadows )
        {
            repoRes = new RepositoryStatusListResource();

            repoRes.setResourceURI( calculateSubReference( shadow.getId() ).toString() );

            repoRes.setRepoType( getRestRepoType( shadow ) );

            repoRes.setName( shadow.getName() );

            repoRes.setStatus( new RepositoryStatusResource() );

            repoRes.getStatus().setLocalStatus( getRestRepoLocalStatus( shadow ) );

            response.addData( repoRes );
        }

        return serialize( variant, response );
    }

}
