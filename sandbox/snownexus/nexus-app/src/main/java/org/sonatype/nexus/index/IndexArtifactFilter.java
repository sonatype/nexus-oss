package org.sonatype.nexus.index;

import java.util.Collection;

public interface IndexArtifactFilter
{
    Collection<ArtifactInfo> filterArtifactInfos( Collection<ArtifactInfo> artifactInfos );
    
    boolean filterArtifactInfo( ArtifactInfo artifactInfo );
}
