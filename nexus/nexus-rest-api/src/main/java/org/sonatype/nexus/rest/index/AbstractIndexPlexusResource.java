/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.nexus.tasks.ReindexTask;

public abstract class AbstractIndexPlexusResource
    extends AbstractRestorePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String query = form.getFirstValue( "q" );

        String className = form.getFirstValue( "cn" );

        String sha1 = form.getFirstValue( "sha1" );

        String g = form.getFirstValue( "g" );

        String a = form.getFirstValue( "a" );

        String v = form.getFirstValue( "v" );

        String p = form.getFirstValue( "p" );

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

        if ( !StringUtils.isEmpty( sha1 ) )
        {
            try
            {
                na = ai2Na( request, getNexus().identifyArtifact( ArtifactInfo.SHA1, sha1 ) );
            }
            catch ( IOException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "IOException during configuration retrieval!",
                    e );
            }
        }
        else if ( !StringUtils.isEmpty( query ) )
        {
            searchResult = getNexus().searchArtifactFlat(
                query,
                getRepositoryId( request ),
                getRepositoryGroupId( request ),
                from,
                count );
        }
        else if ( !StringUtils.isEmpty( className ) )
        {
            searchResult = getNexus().searchArtifactClassFlat(
                className,
                getRepositoryId( request ),
                getRepositoryGroupId( request ),
                from,
                count );
        }
        else if ( !StringUtils.isEmpty( g ) || !StringUtils.isEmpty( a ) || !StringUtils.isEmpty( v )
            || !StringUtils.isEmpty( p ) || !StringUtils.isEmpty( c ) )
        {
            searchResult = getNexus().searchArtifactFlat(
                g,
                a,
                v,
                p,
                c,
                getRepositoryId( request ),
                getRepositoryGroupId( request ),
                from,
                count );
        }
        else
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Search query not found in request! (q OR cn OR g,a,v,p,c)" );
        }

        SearchResponse result = new SearchResponse();

        if ( searchResult != null )
        {
            // non-identify search happened
            result.setTotalCount( searchResult.getTotalHits() );

            result.setFrom( from == null ? -1 : from.intValue() );

            result.setCount( count == null ? -1 : count.intValue() );

            result.setData( new ArrayList<NexusArtifact>( ai2NaColl( request, searchResult.getResults() ) ) );
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

        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        ReindexTask task = getNexus().createTaskInstance( ReindexTask.class );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        handleDelete( task, request );
    }
}
