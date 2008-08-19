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
import java.util.Collections;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.restore.AbstractRestoreResourceHandler;
import org.sonatype.nexus.tasks.ReindexTask;

/**
 * @author cstamas
 */
public class IndexResourceHandler
    extends AbstractRestoreResourceHandler
{
    public IndexResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
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

        String className = form.getFirstValue( "cn" );

        String sha1 = form.getFirstValue( "sha1" );

        String g = form.getFirstValue( "g" );

        String a = form.getFirstValue( "a" );

        String v = form.getFirstValue( "v" );

        String c = form.getFirstValue( "c" );

        Integer from = null;

        Integer count = null;

        if ( form.getFirstValue( "from" ) != null )
        {
            try
            {
                from = Integer.valueOf( form.getFirstValue( "from" ) );
            }
            catch ( NumberFormatException e )
            {
                from = null;
            }
        }

        if ( form.getFirstValue( "count" ) != null )
        {
            try
            {
                count = Integer.valueOf( form.getFirstValue( "count" ) );
            }
            catch ( NumberFormatException e )
            {
                count = null;
            }
        }

        FlatSearchResponse searchResult = null;

        NexusArtifact na = null;

        if ( sha1 != null )
        {
            na = ai2Na( getNexus().identifyArtifact( ArtifactInfo.SHA1, sha1 ), false );
        }
        else if ( query != null )
        {
            searchResult = getNexus()
                .searchArtifactFlat( query, getRepositoryId(), getRepositoryGroupId(), from, count );
        }
        else if ( className != null )
        {
            searchResult = getNexus().searchArtifactClassFlat(
                className,
                getRepositoryId(),
                getRepositoryGroupId(),
                from,
                count );
        }
        else if ( g != null || a != null || v != null || c != null )
        {
            searchResult = getNexus().searchArtifactFlat(
                g,
                a,
                v,
                c,
                getRepositoryId(),
                getRepositoryGroupId(),
                from,
                count );
        }
        else
        {
            getResponse().setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Search query not found in request! (q OR cn OR g,a,v,c)" );

            return null;
        }

        SearchResponse result = new SearchResponse();

        if ( searchResult != null )
        {
            // non-identify search happened
            result.setTotalCount( searchResult.getTotalHits() );

            result.setFrom( from == null ? -1 : from.intValue() );

            result.setCount( count == null ? -1 : count.intValue() );

            result.setData( new ArrayList<NexusArtifact>( ai2NaColl( searchResult.getResults(), false ) ) );
        }
        else if ( na != null )
        {
            // searhcResult is null and na is not, it is identify
            result.setTotalCount( 1 );

            result.setFrom( -1 );

            result.setCount( 1 );

            result.setData( new ArrayList<NexusArtifact>( Collections.singleton( na ) ) );
        }
        else
        {
            // otherwise, we have no results (unsuccesful identify returns null!)
            result.setTotalCount( 0 );

            result.setFrom( -1 );

            result.setCount( 1 );

            result.setData( new ArrayList<NexusArtifact>() );
        }
        // filtering


        return serialize( variant, result );
    }

    public void handleDelete()
    {
        ReindexTask task = (ReindexTask) getNexus().createTaskInstance( ReindexTask.class );

        task.setRepositoryId( getRepositoryId() );

        task.setRepositoryGroupId( getRepositoryGroupId() );

        task.setResourceStorePath( getResourceStorePath() );

        super.handleDelete( task );
    }

}
