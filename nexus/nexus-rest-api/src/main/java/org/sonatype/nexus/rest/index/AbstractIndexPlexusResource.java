/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.nexus.tasks.ReindexTask;

public abstract class AbstractIndexPlexusResource
    extends AbstractRestorePlexusResource
{
    private static final int HIT_LIMIT = 500;

    @Requirement
    private IndexerManager indexerManager;

    @Requirement( role = Searcher.class )
    private List<Searcher> m_searchers;

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

        final Map<String, String> terms = new HashMap<String, String>();
        for ( Parameter parameter : form )
        {
            terms.put( parameter.getName(), parameter.getValue() );
        }

        String sha1 = form.getFirstValue( "sha1" );

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

        try
        {
            if ( !StringUtils.isEmpty( sha1 ) )
            {
                try
                {
                    na = ai2Na( request, indexerManager.identifyArtifact( ArtifactInfo.SHA1, sha1 ) );
                }
                catch ( IOException e )
                {
                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL,
                        "IOException during configuration retrieval!", e );
                }
            }
            else
            {
                searchResult = searchByTerms( terms, getRepositoryId( request ), from, count );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID='"
                + getRepositoryId( request ) + "' does not exists!", e );
        }

        SearchResponse result = new SearchResponse();

        if ( searchResult != null )
        {
            // non-identify search happened
            boolean tooManyResults = searchResult.isHitLimitExceeded();

            result.setTooManyResults( tooManyResults );

            result.setTotalCount( searchResult.getTotalHits() );

            result.setFrom( from == null ? -1 : from.intValue() );

            result.setCount( count == null ? -1 : count );

            if ( tooManyResults )
            {
                result.setData( new ArrayList<NexusArtifact>() );
            }
            else
            {
                result.setData( new ArrayList<NexusArtifact>( ai2NaColl( request, searchResult.getResults() ) ) );
            }
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

    private FlatSearchResponse searchByTerms( final Map<String, String> terms, final String repositoryId,
                                              final Integer from, final Integer count )
        throws NoSuchRepositoryException, ResourceException
    {
        FlatSearchResponse searchResult;
        int totalHits = 0;
        Set<ArtifactInfo> artifacts = null;
        for ( Searcher searcher : m_searchers )
        {
            if ( searcher.canHandle( terms ) )
            {
                final FlatSearchResponse searchResponse =
                    searcher.flatSearch( terms, repositoryId, from, count, HIT_LIMIT );
                if ( searchResponse != null )
                {
                    if ( searchResponse.isHitLimitExceeded() )
                    {
                        return new FlatSearchResponse( null, -1, Collections.<ArtifactInfo> emptySet() );
                    }

                    // Note that we are reusing the tree set from the indexer, otherwise using a new set
                    // we will have to redefine the sorting as defined in indexer
                    if ( artifacts == null )
                    {
                        artifacts = searchResponse.getResults();
                    }
                    else
                    {
                        artifacts.addAll( searchResponse.getResults() );
                    }

                    totalHits += searchResponse.getTotalHits();
                }
            }
        }
        if ( artifacts == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Requested search query is not supported" );
        }
        searchResult = new FlatSearchResponse( null, totalHits, artifacts );
        return searchResult;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        ReindexTask task = getNexusScheduler().createTaskInstance( ReindexTask.class );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        task.setFullReindex( getIsFullReindex() );

        handleDelete( task, request );
    }

    protected abstract boolean getIsFullReindex();

}
