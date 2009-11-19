package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;

public class Nexus2692EvictHostedSnapshotTaskIT
    extends AbstractEvictTaskIt
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void testEvictSnapshotRepo()
        throws Exception
    {
        int days = 1;
        // run Task
        this.runTask( days, "repo_snapshots" );

        // check files
        SortedSet<String> resultStorageFiles = this.getFilePaths(  this.getStorageWorkDir() );
        SortedSet<String> resultAttributeFiles = this.getFilePaths( this.getAttributesWorkDir() );

        SortedSet<String> expectedResults = new TreeSet( this.getPathMap().keySet() );

        // unexpected deleted files
        SortedSet<String> storageDiff = new TreeSet( this.getPathMap().keySet() );
        storageDiff.removeAll( resultStorageFiles );

        SortedSet<String> attributeDiff = new TreeSet( this.getPathMap().keySet() );
        attributeDiff.removeAll( resultStorageFiles );

        Assert.assertTrue(
            "Files deleted that should not have been: " + this.prettyList( storageDiff ),
            expectedResults.equals( resultStorageFiles ) );
        Assert.assertTrue(
            "Files deleted that should not have been: " + this.prettyList( attributeDiff ),
            expectedResults.equals( resultAttributeFiles ) );
    }

}
