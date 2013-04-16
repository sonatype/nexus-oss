package org.sonatype.nexus.maven.tasks;

/**
 * @since 2.5
 */
public class ReleaseRemovalRequest
{
    private final String repositoryId;

    private final int numberOfVersionsToKeep;

    public ReleaseRemovalRequest( final String repositoryId, final int numberOfVersionsToKeep )
    {
        this.repositoryId = repositoryId;
        this.numberOfVersionsToKeep = numberOfVersionsToKeep;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public int getNumberOfVersionsToKeep()
    {
        return numberOfVersionsToKeep;
    }
}
