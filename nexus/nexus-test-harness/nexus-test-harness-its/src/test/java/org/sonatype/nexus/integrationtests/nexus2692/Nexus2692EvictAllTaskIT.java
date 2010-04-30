package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.Test;

public class Nexus2692EvictAllTaskIT extends AbstractEvictTaskIt
{
    
    @Test
    public void testEvictAllRepos()
        throws Exception
    {
        int days = 6;
        // run Task
        this.runTask( days, "all_repo" );

        // check files
        SortedSet<String> resultStorageFiles = this.getFilePaths( this.getStorageWorkDir() );
        SortedSet<String> resultAttributeFiles = this.getFilePaths( this.getAttributesWorkDir() );

        SortedSet<String> expectedResults = this.buildListOfExpectedFilesForAllRepos( days );

        // calc the diff ( files that were deleted and should not have been )
        expectedResults.removeAll( resultStorageFiles );
        Assert.assertTrue(
            "The following files where deleted and should not have been: " + expectedResults,
            expectedResults.isEmpty() );

        expectedResults = this.buildListOfExpectedFilesForAllRepos( days );
        expectedResults.removeAll( resultAttributeFiles );
        Assert.assertTrue(
            "The following attribute files where deleted and should not have been: " + expectedResults,
            expectedResults.isEmpty() );

        // now the other way
        expectedResults = this.buildListOfExpectedFilesForAllRepos( days );
        resultStorageFiles.removeAll( expectedResults );
        Assert.assertTrue( "The following files should have been deleted: " + resultStorageFiles, resultStorageFiles
            .isEmpty() );

        expectedResults = this.buildListOfExpectedFilesForAllRepos( days );
        resultAttributeFiles.removeAll( expectedResults );
        Assert.assertTrue(
            "The following files should have been deleted: " + resultAttributeFiles,
            resultAttributeFiles.isEmpty() );

        // make sure we don't have any empty directories
        this.checkForEmptyDirectories();
    }
}
