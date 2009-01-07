/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 * @author Jason van Zyl
 * @author cstamas
 */
public interface SearchEngine
{
    int UNDEFINED = -1;

    // ----------------------------------------------------------------------------
    // Searching
    // ----------------------------------------------------------------------------

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        IndexingContext indexingContext, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    @Deprecated
    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException;

    public FlatSearchResponse searchFlatPaged( FlatSearchRequest request, Collection<IndexingContext> indexingContexts )
        throws IOException,
            IndexContextInInconsistentStateException;

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request )
        throws IOException,
            IndexContextInInconsistentStateException;

    public GroupedSearchResponse searchGrouped( GroupedSearchRequest request,
        Collection<IndexingContext> indexingContexts )
        throws IOException,
            IndexContextInInconsistentStateException;
}
