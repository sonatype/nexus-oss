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
import java.util.Collection;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;

public class RepositoryTargetListResourceHandler
    extends AbstractRepositoryTargetResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryTargetListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of targets by getting the from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        RepositoryTargetListResourceResponse response = new RepositoryTargetListResourceResponse();

        Collection<CRepositoryTarget> targets = getNexus().listRepositoryTargets();

        RepositoryTargetListResource res = null;

        for ( CRepositoryTarget target : targets )
        {
            res = new RepositoryTargetListResource();

            res.setId( target.getId() );

            res.setName( target.getName() );

            res.setContentClass( target.getContentClass() );

            res.setResourceURI( calculateSubReference( target.getId() ).toString() );

            response.addData( res );
        }

        return serialize( variant, response );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation representation )
    {
        RepositoryTargetResourceResponse request = (RepositoryTargetResourceResponse) deserialize( new RepositoryTargetResourceResponse() );

        if ( request == null )
        {
            return;
        }
        else
        {
            RepositoryTargetResource resource = request.getData();

            if ( validate( true, resource, representation ) )
            {
                try
                {
                    CRepositoryTarget target = getRestToNexusResource( resource );

                    // create
                    getNexus().createRepositoryTarget( target );

                    // response
                    RepositoryTargetResourceResponse response = new RepositoryTargetResourceResponse();

                    response.setData( request.getData() );

                    getResponse().setEntity( serialize( representation, response ) );
                    
                    getResponse().setStatus( Status.SUCCESS_CREATED );
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
    }
}
