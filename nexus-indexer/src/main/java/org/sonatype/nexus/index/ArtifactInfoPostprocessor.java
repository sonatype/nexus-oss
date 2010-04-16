package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

public interface ArtifactInfoPostprocessor
{
    void postprocess( IndexingContext ctx, ArtifactInfo ai );
}
