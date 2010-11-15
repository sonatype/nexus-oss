package org.sonatype.nexus.index;

import java.util.Collection;

import org.apache.maven.index.ArtifactInfo;

/**
 * Filters the search result by various conditions.
 * 
 * @author cstamas
 */
public interface IndexArtifactFilter
{
    Collection<ArtifactInfo> filterArtifactInfos( Collection<ArtifactInfo> artifactInfos );

    boolean filterArtifactInfo( ArtifactInfo artifactInfo );
}
