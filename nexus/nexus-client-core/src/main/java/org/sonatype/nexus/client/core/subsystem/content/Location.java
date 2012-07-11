package org.sonatype.nexus.client.core.subsystem.content;

import org.sonatype.nexus.client.internal.util.Check;

public class Location
{
    private final String repositoryId;

    private final String repositoryPath;

    public Location( final String repositoryId, final String repositoryPath )
    {
        this.repositoryId = Check.notBlank( repositoryId, "repositoryId" );
        String repoPath = Check.notBlank( repositoryPath, "repositoryPath" );
        while ( repoPath.startsWith( "/" ) )
        {
            repoPath = repoPath.substring( 1 );
        }
        this.repositoryPath = repoPath;
    }

    public String toContentPath()
    {
        return "repositories/" + repositoryId + "/" + repositoryPath;
    }

    // --

    @Override
    public String toString()
    {
        return toContentPath();
    }
}
