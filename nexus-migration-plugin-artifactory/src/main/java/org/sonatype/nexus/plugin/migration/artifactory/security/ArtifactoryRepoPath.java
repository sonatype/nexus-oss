package org.sonatype.nexus.plugin.migration.artifactory.security;

public class ArtifactoryRepoPath
{
    public static final String REPO_KEY_ANY = "ANY";

    public static final String PATH = "ANY";

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
    
    
}
