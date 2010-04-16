package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

public interface ArtifactInfoFilter
{
    boolean accepts( IndexingContext ctx, ArtifactInfo ai );
}
