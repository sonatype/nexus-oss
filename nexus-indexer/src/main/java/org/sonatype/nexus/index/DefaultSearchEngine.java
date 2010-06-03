/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
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
            totalHits +=
                searchFlat( request, result, context, request.getQuery(), request.getStart(), request.getCount() );
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
                int hitCount =
                    searchFlat( request, result, ctx, request.getQuery(), request.getStart(), request.getCount() );

                if ( hitCount == AbstractSearchResponse.LIMIT_EXCEEDED )
                {
                    totalHits = hitCount;
                }
                else
                {
                    totalHits += hitCount;
                }
            }

            if ( request.isHitLimited() && ( totalHits > request.getResultHitLimit() )
                || totalHits == AbstractSearchResponse.LIMIT_EXCEEDED )
            {
                totalHits = AbstractSearchResponse.LIMIT_EXCEEDED;
                result = new TreeSet<ArtifactInfo>( request.getArtifactInfoComparator() );
                break;
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
                int hitCount = searchGrouped( request, result, request.getGrouping(), ctx, request.getQuery() );

                if ( hitCount == AbstractSearchResponse.LIMIT_EXCEEDED )
                {
                    totalHits = hitCount;
                }
                else
                {
                    totalHits += hitCount;
                }
            }

            if ( request.isHitLimited() && ( totalHits > request.getResultHitLimit() )
                || totalHits == AbstractSearchResponse.LIMIT_EXCEEDED )
            {
                totalHits = AbstractSearchResponse.LIMIT_EXCEEDED;
                result = new TreeMap<String, ArtifactInfoGroup>( request.getGroupKeyComparator() );
                break;
            }
        }

        return new GroupedSearchResponse( request.getQuery(), totalHits, result );
    }

    protected int searchFlat( AbstractSearchRequest req, Collection<ArtifactInfo> result, IndexingContext context,
                              Query query, int from, int aiCount )
        throws IOException
    {
        Hits hits =
            context.getIndexSearcher().search( query, new Sort( new SortField( ArtifactInfo.UINFO, SortField.STRING ) ) );

        if ( hits == null || hits.length() == 0 )
        {
            return 0;
        }

        if ( req.isHitLimited() && hits.length() > req.getResultHitLimit() )
        {
            return AbstractSearchResponse.LIMIT_EXCEEDED;
        }

        int hitCount = hits.length();

        int start = 0; // from == FlatSearchRequest.UNDEFINED ? 0 : from;

        // we have to pack the results as long: a) we have found aiCount ones b) we depleted hits
        for ( int i = start; i < hits.length(); i++ )
        {
            Document doc = hits.doc( i );

            ArtifactInfo artifactInfo = IndexUtils.constructArtifactInfo( doc, context );

            if ( artifactInfo != null )
            {
                artifactInfo.repository = context.getRepositoryId();

                artifactInfo.context = context.getId();

                result.add( artifactInfo );

                if ( req.isHitLimited() && result.size() > req.getResultHitLimit() )
                {
                    // we hit limit, back out now !!
                    return AbstractSearchResponse.LIMIT_EXCEEDED;
                }
            }
        }

        return hitCount;
    }

    protected int searchGrouped( AbstractSearchRequest req, Map<String, ArtifactInfoGroup> result, Grouping grouping,
                                 IndexingContext context, Query query )
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

            if ( req.isHitLimited() && hits.length() > req.getResultHitLimit() )
            {
                return AbstractSearchResponse.LIMIT_EXCEEDED;
            }

            return hitCount;
        }
        else
        {
            return 0;
        }
    }

    // == NG Search

    public IteratorSearchResponse searchIteratorPaged( IteratorSearchRequest request,
                                                       Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchIteratorPaged( request, indexingContexts, false );
    }

    public IteratorSearchResponse forceSearchIteratorPaged( IteratorSearchRequest request,
                                                            Collection<IndexingContext> indexingContexts )
        throws IOException
    {
        return searchIteratorPaged( request, indexingContexts, true );
    }

    private IteratorSearchResponse searchIteratorPaged( IteratorSearchRequest request,
                                                        Collection<IndexingContext> indexingContexts,
                                                        boolean ignoreContext )
        throws IOException
    {
        // manage defaults!
        if ( request.getStart() < 0 )
        {
            request.setStart( IteratorSearchRequest.UNDEFINED );
        }
        if ( request.getCount() < 0 )
        {
            request.setCount( IteratorSearchRequest.UNDEFINED );
        }

        ArrayList<IndexSearcher> contextsToSearch = new ArrayList<IndexSearcher>( indexingContexts.size() );

        for ( IndexingContext ctx : indexingContexts )
        {
            if ( ignoreContext || ctx.isSearchable() )
            {
                contextsToSearch.add( ctx.getReadOnlyIndexSearcher() );
            }
        }

        ParallelMultiSearcher multiSearcher =
            new ParallelMultiSearcher( contextsToSearch.toArray( new Searchable[contextsToSearch.size()] ) );

        // NEXUS-3482 made us to NOT use reverse ordering (it is a fix I wanted to implement, but user contributed patch
        // did come in faster! -- Thanks)
        Hits hits =
            multiSearcher.search( request.getQuery(), new Sort( new SortField[] { SortField.FIELD_SCORE,
                new SortField( null, SortField.DOC, false ) } ) );

        return new IteratorSearchResponse( request.getQuery(), hits.length(), new DefaultIteratorResultSet( request,
            multiSearcher, hits ) );
    }
}
