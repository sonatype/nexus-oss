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
import java.util.Set;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A search engine used to perform searches trough repository indexes.
 *  
 * @author Eugene Kuleshov
 * @author Jason van Zyl
 * @author Tamas Cservenak
 */
public interface SearchEngine
{
    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        IndexingContext indexingContext, Query query )
        throws IOException;

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException;

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request )
        throws IOException;

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request, Collection<IndexingContext> indexingContexts )
        throws IOException;

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException;

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request,
        Collection<IndexingContext> indexingContexts )
        throws IOException;
}
