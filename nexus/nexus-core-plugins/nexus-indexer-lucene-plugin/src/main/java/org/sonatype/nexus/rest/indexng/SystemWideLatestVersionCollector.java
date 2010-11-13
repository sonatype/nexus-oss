package org.sonatype.nexus.rest.indexng;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A special filter that actually does not filter, but collects the latest and release version for every GA. After
 * iteratorSearchResponse has been processed, this collector will hold all the needed versions of the processed artifact
 * infos.
 * 
 * @author cstamas
 */
public class SystemWideLatestVersionCollector
    extends AbstractLatestVersionCollector
{
    @Override
    public LatestVersionHolder createLVH( IndexingContext ctx, ArtifactInfo ai )
    {
        return new LatestVersionHolder( ai );
    }

    @Override
    public String getKeyFromAi( IndexingContext ctx, ArtifactInfo ai )
    {
        return getKey( ai.groupId, ai.artifactId );
    }

    public String getKey( String groupId, String artifactId )
    {
        return groupId + ":" + artifactId;
    }
}
