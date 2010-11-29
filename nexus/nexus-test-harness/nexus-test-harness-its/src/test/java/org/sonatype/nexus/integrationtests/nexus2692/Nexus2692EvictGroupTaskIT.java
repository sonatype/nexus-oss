package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2692EvictGroupTaskIT
    extends AbstractEvictTaskIt
{

    @Test
    public void testEvictPublicGroup()
        throws Exception
    {
        int days = 6;
        // run Task
        runTask( days, "group_public" );

        // check files
        SortedSet<String> resultStorageFiles = getFilePaths( getStorageWorkDir() );
        SortedSet<String> resultAttributeFiles = getAttributeFilePaths();

        // list of repos NOT in the public group
        List<String> nonPublicGroupMembers = new ArrayList<String>();
        nonPublicGroupMembers.add( "apache-snapshots" );

        SortedSet<String> expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );

        // calc the diff ( files that were deleted and should not have been )
        expectedResults.removeAll( resultStorageFiles );
        Assert.assertTrue( expectedResults.isEmpty(), "The following files where deleted and should not have been: "
                + prettyList( expectedResults ) );

        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        expectedResults.removeAll( resultAttributeFiles );
        Assert.assertTrue( expectedResults.isEmpty(), "The following attribute files where deleted and should not have been: "
                + prettyList( expectedResults ) );

        // now the other way
        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        resultStorageFiles.removeAll( expectedResults );
        Assert.assertTrue(
            resultStorageFiles.isEmpty(),
            "The following files should have been deleted: " + prettyList( resultStorageFiles ) );

        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        resultAttributeFiles.removeAll( expectedResults );
        Assert.assertTrue(
            resultAttributeFiles.isEmpty(),
            "The following files should have been deleted: " + prettyList( resultAttributeFiles ) );

        // make sure we don't have any empty directories
        checkForEmptyDirectories();
    }
}
