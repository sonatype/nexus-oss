package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2692EvictHostedSnapshotTaskIT
    extends AbstractEvictTaskIt
{
    @Test
    public void testEvictSnapshotRepo()
        throws Exception
    {
        int days = 1;
        // run Task
        runTask( days, "repo_snapshots" );

        // check files
        SortedSet<String> resultStorageFiles = getItemFilePaths();
        SortedSet<String> resultAttributeFiles = getAttributeFilePaths();

        // unexpected deleted files
        SortedSet<String> storageDiff = new TreeSet<String>( getPathMap().keySet() );
        storageDiff.removeAll( resultStorageFiles );

        SortedSet<String> attributeDiff = new TreeSet<String>( getPathMap().keySet() );
        attributeDiff.removeAll( resultAttributeFiles );

        Assert.assertTrue( storageDiff.isEmpty(), "Files deleted that should not have been: "
            + prettyList( storageDiff ) );
        Assert.assertTrue( attributeDiff.isEmpty(), "Files deleted that should not have been: "
            + prettyList( attributeDiff ) );
    }

}
