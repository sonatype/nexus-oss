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
package org.sonatype.nexus.rest.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.restore.AbstractRestoreResourceHandler;

/**
 * @author cstamas
 */
public class IndexResourceHandler
    extends AbstractRestoreResourceHandler
{

    public IndexResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        repositoryId = null;

        repositoryGroupId = null;

        if ( getRequest().getAttributes().containsKey( DOMAIN ) && getRequest().getAttributes().containsKey( TARGET_ID ) )
        {
            if ( DOMAIN_REPOSITORIES.equals( getRequest().getAttributes().get( DOMAIN ) ) )
            {
                repositoryId = getRequest().getAttributes().get( TARGET_ID ).toString();
            }
            else if ( DOMAIN_REPO_GROUPS.equals( getRequest().getAttributes().get( DOMAIN ) ) )
            {
                repositoryGroupId = getRequest().getAttributes().get( TARGET_ID ).toString();
            }
        }
    }

    public boolean allowGet()
    {
        return true;
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        String query = form.getFirstValue( "q" );

        if ( query == null )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Search query not found in request!" );

            return null;
        }

        Collection<NexusArtifact> ais = ai2NaColl( getNexus().searchArtifactFlat(
            query,
            repositoryId,
            repositoryGroupId ) );

        SearchResponse result = new SearchResponse();

        result.setTotalCount( ais.size() );

        result.setData( new ArrayList<NexusArtifact>( ais ) );

        return serialize( variant, result );
    }

    public void handleDelete()
    {
        super.handleDelete( new ReindexTask( getNexus(), repositoryId, repositoryGroupId ) );
    }

}
