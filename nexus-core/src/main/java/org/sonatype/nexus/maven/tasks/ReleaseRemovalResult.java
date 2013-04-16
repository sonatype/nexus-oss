package org.sonatype.nexus.maven.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 2.5
 */
public class ReleaseRemovalResult
{
    private final String repoId;

    private int deletedFileCount;

    private boolean isSuccessful;

    private ReleaseRemovalResult result;

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

    public ReleaseRemovalResult getResult()
    {
        return result;
    }

    public void setResult( final ReleaseRemovalResult result )
    {
        this.result = result;
    }
}
