package org.sonatype.nexus.index;

/**
 * A filter for filtering ArtifactInfo's.
 * 
 * @author cstamas
 */
public interface ArtifactInfoFilter
{
    boolean accept( ArtifactInfo ai );
}
