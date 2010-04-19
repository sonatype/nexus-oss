package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * ArtifactInfoPostprocessor is used to postprocess the ArtifactInfo, after it passed filtering and paging but before it
 * is handed over to client performing search. Use is typically like adding some "calculated field", or formatting, and
 * such.
 * 
 * @author cstamas
 */
public interface ArtifactInfoPostprocessor
{
    void postprocess( IndexingContext ctx, ArtifactInfo ai );
}
