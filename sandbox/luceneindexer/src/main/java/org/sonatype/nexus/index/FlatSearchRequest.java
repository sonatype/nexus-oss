/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A flat search request.
 * 
 * @see NexusIndexer#searchFlat(FlatSearchRequest)
 */
public class FlatSearchRequest
    extends AbstractSearchRequest
{
    private Comparator<ArtifactInfo> artifactInfoComparator;

    public FlatSearchRequest( Query query )
    {
        this( query, ArtifactInfo.VERSION_COMPARATOR );
    }

    public FlatSearchRequest( Query query, Comparator<ArtifactInfo> artifactInfoComparator )
    {
        this( query, artifactInfoComparator, null );
    }

    public FlatSearchRequest( Query query, IndexingContext context )
    {
        this( query, ArtifactInfo.VERSION_COMPARATOR, context );
    }

    public FlatSearchRequest( Query query, Comparator<ArtifactInfo> artifactInfoComparator, IndexingContext context )
    {
        super( query, context != null ? Arrays.asList( new IndexingContext[] { context } ) : null );

        this.artifactInfoComparator = artifactInfoComparator;
    }

    public Comparator<ArtifactInfo> getArtifactInfoComparator()
    {
        return artifactInfoComparator;
    }

    public void setArtifactInfoComparator( Comparator<ArtifactInfo> artifactInfoComparator )
    {
        this.artifactInfoComparator = artifactInfoComparator;
    }
}
