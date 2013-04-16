package org.sonatype.nexus.maven.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple struct for consolidating results of ReleaseRemoval task.
 *
 * @since 2.5
 */
public class ReleaseRemovalResult
{
    private final String repoId;

    private int deletedFileCount;

    private boolean isSuccessful = false;

    public ReleaseRemovalResult( final String repoId )
    {
        this.repoId = checkNotNull(repoId);
    }

    public String getRepoId()
    {
        return repoId;
    }

    public int getDeletedFileCount()
    {
        return deletedFileCount;
    }

    public void setDeletedFileCount( final int deletedFileCount )
    {
        this.deletedFileCount = deletedFileCount;
    }

    public boolean isSuccessful()
    {
        return isSuccessful;
    }

    public void setSuccessful( final boolean successful )
    {
        isSuccessful = successful;
    }

    @Override
    public String toString()
    {
        return "ReleaseRemovalResult{" +
            "repoId='" + repoId + '\'' +
            ", deletedFileCount=" + deletedFileCount +
            ", isSuccessful=" + isSuccessful +
            '}';
    }
}
