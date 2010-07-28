package org.sonatype.nexus.rest.indexng;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A special filter that actually does not filter, but collects the latest and release version for every RGA. After
 * iteratorSearchResponse has been processed, this collector will hold all the needed versions of the processed artifact
 * infos.
 * 
 * @author cstamas
 */
public class RepositoryWideLatestVersionCollector
    extends AbstractLatestVersionCollector
{
    @Override
    public LatestVersionHolder createLVH( IndexingContext ctx, ArtifactInfo ai )
    {
        return new LatestECVersionHolder( ai );
    }

    @Override
    public String getKeyFromAi( IndexingContext ctx, ArtifactInfo ai )
    {
        return getKey( ai.repository, ai.groupId, ai.artifactId );
    }

    public String getKey( String repositoryId, String groupId, String artifactId )
    {
        return repositoryId + ":" + groupId + ":" + artifactId;
    }
}
