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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
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

        CRepositoryTarget target = getNexus().readRepositoryTarget( getRepoTargetId() );

        if ( target != null )
        {
            RepositoryTargetResource resource = getNexusToRestResource( target );

            response.setData( resource );

            return serialize( variant, response );
        }
        else
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );

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
     * Update a target.
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

            CRepositoryTarget target = getNexus().readRepositoryTarget( getRepoTargetId() );

            if ( target != null )
            {
                if ( validate( false, resource, representation ) )
                {
                    try
                    {
                        target = getRestToNexusResource( resource );

                        // update
                        getNexus().updateRepositoryTarget( target );

                        // response
                        RepositoryTargetResourceResponse response = new RepositoryTargetResourceResponse();

                        response.setData( request.getData() );

                        getResponse().setEntity( serialize( representation, response ) );
                    }
                    catch ( ConfigurationException e )
                    {
                        handleConfigurationException( e, representation );

                        return;
                    }
                    catch ( IOException e )
                    {
                        getLogger().log( Level.WARNING, "Got IOException during creation of repository target!", e );

                        getResponse().setStatus(
                            Status.SERVER_ERROR_INTERNAL,
                            "Got IOException during creation of repository target!" );

                        return;
                    }
                }
            }
            else
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );
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
        CRepositoryTarget target = getNexus().readRepositoryTarget( getRepoTargetId() );

        if ( target != null )
        {
            try
            {
                getNexus().deleteRepositoryTarget( getRepoTargetId() );
            }
            catch ( IOException e )
            {
                getLogger().log( Level.WARNING, "Got IOException during removal of repository target!", e );

                getResponse().setStatus(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got IOException during removal of repository target!" );
            }
        }
        else
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );
        }
    }

}
