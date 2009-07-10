/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A default search engine implementation
 *
 * @author Eugene Kuleshov
 * @author Tamas Cservenak
 */
@Component( role = SearchEngine.class )
public class DefaultSearchEngine
    extends AbstractLogEnabled
    implements SearchEngine
{
    private static final int MAX_HITS = 500;
    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
                                         IndexingContext indexingContext, Query query )
        throws IOException
    {
        return searchFlatPaged( new FlatSearchRequest( query, artifactInfoComparator, indexingContext ) ).getResults();
    }

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
                                         Collection<IndexingContext> indexingContexts, Query query )
        throws IOException
    {
        return searchFlatPaged( new FlatSearchRequest( query, artifactInfoComparator ), indexingContexts ).getResults();
    }

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request )
        throws IOException
    {
        TreeSet<ArtifactInfo> result = new TreeSet<ArtifactInfo>( request.getArtifactInfoComparator() );

        int totalHits = 0;
        for ( IndexingContext context : request.getContexts() )
        {
            totalHits += searchFlat( result, context, request.getQuery(), request.getStart(), request.getAiCount() );
        }

        return new FlatSearchResponse( request.getQuery(), totalHits, result );
    }

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request, Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchFlatPaged( request, indexingContexts, false );
    }

    public FlatSearchResponse forceSearchFlatPaged( FlatSearchRequest request,
                                                    Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchFlatPaged( request, indexingContexts, true );
    }

    private FlatSearchResponse searchFlatPaged( FlatSearchRequest request,
                                                Collection<IndexingContext> indexingContexts, boolean ignoreContext )
        throws IOException
    {
        TreeSet<ArtifactInfo> result = new TreeSet<ArtifactInfo>( request.getArtifactInfoComparator() );

        int totalHits = 0;

        for ( IndexingContext ctx : indexingContexts )
        {
            if ( ignoreContext || ctx.isSearchable() )
            {
                totalHits += searchFlat( result, ctx, request.getQuery(), request.getStart(), request.getAiCount() );
            }
        }

        return new FlatSearchResponse( request.getQuery(), totalHits, result );
    }

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request,
                                                Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchGrouped( request, indexingContexts, false );
    }

    public GroupedSearchResponse forceSearchGrouped( GroupedSearchRequest request,
                                                     Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchGrouped( request, indexingContexts, true );
    }

    private GroupedSearchResponse searchGrouped( GroupedSearchRequest request,
                                                 Collection<IndexingContext> indexingContexts, boolean ignoreContext )
        throws IOException
    {
        TreeMap<String, ArtifactInfoGroup> result =
            new TreeMap<String, ArtifactInfoGroup>( request.getGroupKeyComparator() );

        int totalHits = 0;

        for ( IndexingContext ctx : indexingContexts )
        {
            if ( ignoreContext || ctx.isSearchable() )
            {
                totalHits += searchGrouped( result, request.getGrouping(), ctx, request.getQuery() );
            }
        }

        return new GroupedSearchResponse( request.getQuery(), totalHits, result );
    }

    protected int searchFlat( Collection<ArtifactInfo> result, IndexingContext context, Query query, int from,
                              int aiCount )
        throws IOException
    {
        Hits hits =
            context.getIndexSearcher().search( query, new Sort( new SortField( ArtifactInfo.UINFO, SortField.STRING ) ) );

        if ( hits == null || hits.length() == 0 )
        {
            return 0;
        }
        
        if ( hits.length() > MAX_HITS )
        {
            return -1;
        }

        int hitCount = hits.length();

        int start = 0; //from == FlatSearchRequest.UNDEFINED ? 0 : from;

        int found = 0;

        // we have to pack the results as long: a) we have found aiCount ones b) we depleted hits
        for ( int i = start; i < hits.length(); i++ )
        {
            Document doc = hits.doc( i );

            ArtifactInfo artifactInfo = IndexUtils.constructArtifactInfo( doc, context );

            if ( artifactInfo != null )
            {
                artifactInfo.repository = context.getRepositoryId();

                artifactInfo.context = context.getId();

                if ( result.add( artifactInfo ) )
                {
                    // increase the founds
                    found++;
                }

                if ( found >= MAX_HITS )
                {
                    // escape then
                    break;
                }
            }
        }

        return hitCount;
    }

    protected int searchGrouped( Map<String, ArtifactInfoGroup> result, Grouping grouping, IndexingContext context,
                                 Query query )
        throws IOException
    {
        Hits hits =
            context.getIndexSearcher().search( query, new Sort( new SortField( ArtifactInfo.UINFO, SortField.STRING ) ) );

        if ( hits != null && hits.length() != 0 )
        {
            int hitCount = hits.length();

            for ( int i = 0; i < hits.length(); i++ )
            {
                ArtifactInfo artifactInfo = IndexUtils.constructArtifactInfo( hits.doc( i ), context );

                if ( artifactInfo != null )
                {
                    artifactInfo.repository = context.getRepositoryId();

                    artifactInfo.context = context.getId();

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

}
