package org.sonatype.nexus.plugins.mac;

import org.apache.maven.index.ArtifactInfoFilter;

/**
 * A request carrying needed information to construct response Archetype Catalog properly.
 * 
 * @author cstamas
 */
public class MacRequest
{
    private final String repositoryId;

    private final String repositoryUrl;

    private final ArtifactInfoFilter artifactInfoFilter;

    public MacRequest( String repositoryId )
    {
        this( repositoryId, null, null );
    }

    public MacRequest( final String repositoryId, final String repositoryUrl,
                       final ArtifactInfoFilter artifactInfoFilter )
    {
        this.repositoryId = repositoryId;
        this.repositoryUrl = repositoryUrl;
        this.artifactInfoFilter = artifactInfoFilter;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public ArtifactInfoFilter getArtifactInfoFilter()
    {
        return artifactInfoFilter;
    }
}
