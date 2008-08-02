/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.Grouping;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 * @author cstamas
 * @plexus.component
 */
public class DefaultSearchEngine
    extends AbstractLogEnabled
    implements SearchEngine
{
    // ====================================
    // Inner methods doing the work

    protected int searchFlat( Collection<ArtifactInfo> result, IndexingContext indexingContext, Query query, int from,
        int aiCount )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        Hits hits = indexingContext.getIndexSearcher().search(
            query,
            new Sort( new SortField( ArtifactInfo.UINFO, SortField.STRING ) ) );

        if ( hits != null && hits.length() != 0 )
        {
            int hitCount = hits.length();

            int start = from == UNDEFINED ? 0 : from;

            int found = 0;

            // we have to pack the results as long: a) we have found aiCount ones b) we depleted hits
            for ( int i = start; i < hits.length(); i++ )
            {
                Document doc = hits.doc( i );

                ArtifactInfo artifactInfo = indexingContext.constructArtifactInfo( doc );

                if ( artifactInfo != null )
                {
                    artifactInfo.repository = indexingContext.getRepositoryId();

                    artifactInfo.context = indexingContext.getId();

                    if ( result.add( artifactInfo ) )
                    {
                        // increase the founds
                        found++;
                    }
                    else
                    {
                        // fix the total hitCount accordingly
                        hitCount--;
                    }

                    if ( found == aiCount )
                    {
                        // escape then
                        break;
                    }
                }
            }
            
            return hitCount;
        }
        else
        {
            return 0;
        }
    }

    protected int searchGrouped( Map<String, ArtifactInfoGroup> result, Grouping grouping,
        IndexingContext indexingContext, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        Hits hits = indexingContext.getIndexSearcher().search(
            query,
            new Sort( new SortField( ArtifactInfo.UINFO, SortField.STRING ) ) );

        if ( hits != null && hits.length() != 0 )
        {
            int hitCount = hits.length();

            for ( int i = 0; i < hits.length(); i++ )
            {
                ArtifactInfo artifactInfo = indexingContext.constructArtifactInfo( hits.doc( i ) );

                if ( artifactInfo != null )
                {
                    artifactInfo.repository = indexingContext.getRepositoryId();

                    artifactInfo.context = indexingContext.getId();

                    if ( !grouping.addArtifactInfo( result, artifactInfo ) )
                    {
                        // fix the hitCount accordingly
                        hitCount--;
                    }
                }
            }
            return hitCount;
        }
        else
        {
            return 0;
        }
    }

    // ====================================
    // Public impls

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        IndexingContext indexingContext, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchFlatPaged( new FlatSearchRequest( query, artifactInfoComparator, indexingContext ) ).getResults();
    }

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        return searchFlatPaged( new FlatSearchRequest( query, artifactInfoComparator ), indexingContexts ).getResults();
    }

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        TreeSet<ArtifactInfo> result = new TreeSet<ArtifactInfo>( request.getArtifactInfoComparator() );

        int totalHits = searchFlat( result, request.getContext(), request.getQuery(), request.getStart(), request
            .getAiCount() );

        return new FlatSearchResponse( request.getQuery(), totalHits, result );
    }

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request, Collection<IndexingContext> indexingContexts )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        TreeSet<ArtifactInfo> result = new TreeSet<ArtifactInfo>( request.getArtifactInfoComparator() );

        int totalHits = 0;

        for ( IndexingContext ctx : indexingContexts )
        {
            if ( ctx.isSearchable() )
            {
                totalHits += searchFlat( result, ctx, request.getQuery(), request.getStart(), request.getAiCount() );
            }
        }

        return new FlatSearchResponse( request.getQuery(), totalHits, result );
    }

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        TreeMap<String, ArtifactInfoGroup> result = new TreeMap<String, ArtifactInfoGroup>( request
            .getGroupKeyComparator() );

        int totalHits = searchGrouped( result, request.getGrouping(), request.getContext(), request.getQuery() );

        return new GroupedSearchResponse( request.getQuery(), totalHits, result );
    }

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request,
        Collection<IndexingContext> indexingContexts )
        throws IOException,
            IndexContextInInconsistentStateException
    {
        TreeMap<String, ArtifactInfoGroup> result = new TreeMap<String, ArtifactInfoGroup>( request
            .getGroupKeyComparator() );

        int totalHits = 0;

        for ( IndexingContext ctx : indexingContexts )
        {
            if ( ctx.isSearchable() )
            {
                totalHits += searchGrouped( result, request.getGrouping(), ctx, request.getQuery() );
            }
        }

        return new GroupedSearchResponse( request.getQuery(), totalHits, result );
    }

}
