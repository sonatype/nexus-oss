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
package org.sonatype.nexus.rest.repotargets;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;

public class RepositoryTargetResourceHandler
    extends AbstractRepositoryTargetResourceHandler
{
    private String repoTargetId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryTargetResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.repoTargetId = getRequest().getAttributes().get( REPO_TARGET_ID_KEY ).toString();
    }

    protected String getRepoTargetId()
    {
        return this.repoTargetId;
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Role resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        RepositoryTargetResourceResponse response = new RepositoryTargetResourceResponse();

        RepositoryTargetResource resource = getNexusToRestResource();

        response.setData( resource );

        return serialize( variant, response );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a user.
     */
    public void put( Representation representation )
    {
        RepositoryTargetResourceResponse request = (RepositoryTargetResourceResponse) deserialize( new RepositoryTargetResourceResponse() );

        if ( request == null )
        {
            return;
        }
        else
        {
            RepositoryTargetResource resource = request.getData();

            if ( validate( resource, representation ) )
            {
                // TODO: actually store the data here

                RepositoryTargetResourceResponse response = new RepositoryTargetResourceResponse();

                response.setData( request.getData() );

                getResponse().setEntity( serialize( representation, response ) );
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
     * Delete a target.
     */
    public void delete()
    {
        // TODO: Delete the target
    }

    // ==

    protected boolean validate( RepositoryTargetResource resource, Representation representation )
    {
        return true;
    }

    protected RepositoryTargetResource getNexusToRestResource()
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setId( getRepoTargetId() );

        resource.setName( getRepoTargetId() );

        resource.setResourceURI( getRequest().getResourceRef().getPath() );
        
        resource.addPattern( "pathA" );
        
        resource.addPattern( "pathB" );

        return resource;
    }

}
