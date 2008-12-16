package org.sonatype.nexus.plugin.migration.artifactory.security;

public class ArtifactoryRepoPath
{
    public static final String REPO_KEY_ANY = "ANY";

    public static final String PATH_ANY = "ANY";

    private String repoKey;

    private String path;

    public ArtifactoryRepoPath( String repoKey, String path )
    {
        this.repoKey = repoKey;

        this.path = path;
    }

    public String getRepoKey()
    {
        return repoKey;
    }

    public String getPath()
    {
        return path;
    }
    
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ArtifactoryRepoPath ) )
        {
            return false;
        }

        ArtifactoryRepoPath repoPath = (ArtifactoryRepoPath) obj;

        return this.repoKey.equals( repoPath.repoKey ) && this.path.equals( repoPath.path );
    }
    
    @Override
    public int hashCode()
    {
        return repoKey.hashCode() * 13 + path.hashCode();
    }
}
