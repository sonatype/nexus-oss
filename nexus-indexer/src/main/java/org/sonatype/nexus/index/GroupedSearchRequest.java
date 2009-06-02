/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A grouped search request.
 * 
 * @see NexusIndexer#searchGrouped(GroupedSearchRequest)
 */
public class GroupedSearchRequest
{
    private final Query query;

    private final Grouping grouping;

    private Comparator<String> groupKeyComparator;

    private List<IndexingContext> contexts;

    public GroupedSearchRequest( Query query, Grouping grouping )
    {
        super();

        this.query = query;

        this.grouping = grouping;

        this.groupKeyComparator = String.CASE_INSENSITIVE_ORDER;

        this.contexts = null;
    }

    public GroupedSearchRequest( Query query, Grouping grouping, Comparator<String> groupKeyComparator )
    {
        this( query, grouping );

        this.groupKeyComparator = groupKeyComparator;
    }

    public GroupedSearchRequest( Query query, Grouping grouping, IndexingContext context )
    {
        this( query, grouping );
        
        getContexts().add( context );
    }

    public GroupedSearchRequest( Query query, Grouping grouping, Comparator<String> groupKeyComparator,
                                 IndexingContext context )
    {
        this( query, grouping, groupKeyComparator );

        getContexts().add( context );
    }

    public Query getQuery()
    {
        return query;
    }

    public Grouping getGrouping()
    {
        return grouping;
    }

    public Comparator<String> getGroupKeyComparator()
    {
        return groupKeyComparator;
    }

    public List<IndexingContext> getContexts()
    {
        if ( contexts == null )
        {
            contexts = new ArrayList<IndexingContext>();
        }

        return contexts;
    }
}
