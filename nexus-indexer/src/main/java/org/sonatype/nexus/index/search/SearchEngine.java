/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Grouping;
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

    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        IndexingContext indexingContext, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    public Set<ArtifactInfo> searchFlatPaged( Comparator<ArtifactInfo> artifactInfoComparator,
        IndexingContext indexingContext, Query query, int from, int aiCount )
        throws IOException,
            IndexContextInInconsistentStateException;

    public Set<ArtifactInfo> searchFlatPaged( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query, int from, int aiCount )
        throws IOException,
            IndexContextInInconsistentStateException;

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        IndexingContext indexingContext, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException;
}
