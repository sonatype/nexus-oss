/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index;

import java.util.Comparator;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

public class GroupedSearchRequest
{
    private final Query query;

    private final Grouping grouping;

    private Comparator<String> groupKeyComparator;

    private IndexingContext context;

    public GroupedSearchRequest( Query query, Grouping grouping )
    {
        super();

        this.query = query;

        this.grouping = grouping;

        this.groupKeyComparator = String.CASE_INSENSITIVE_ORDER;

        this.context = null;
    }

    public GroupedSearchRequest( Query query, Grouping grouping, Comparator<String> groupKeyComparator )
    {
        this( query, grouping );

        this.groupKeyComparator = groupKeyComparator;
    }

    public GroupedSearchRequest( Query query, Grouping grouping, IndexingContext context )
    {
        this( query, grouping );

        this.context = context;
    }

    public GroupedSearchRequest( Query query, Grouping grouping, Comparator<String> groupKeyComparator,
        IndexingContext context )
    {
        this( query, grouping, groupKeyComparator );

        this.context = context;
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

    public IndexingContext getContext()
    {
        return context;
    }
}
