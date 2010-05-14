package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * ArtifactInfoFilter is used to filter out components before they are handed over to searcher (and well before paging
 * is implemented). One can use it for: permission-based filtering, collapsing result sets, etc.
 * 
 * @author cstamas
 * @see AndMultiArtifactInfoFilter
 */
public interface ArtifactInfoFilter
{
    boolean accepts( IndexingContext ctx, ArtifactInfo ai );
}
