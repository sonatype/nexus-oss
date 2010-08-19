package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2692EvictHostedReleaseTaskIT
    extends AbstractEvictTaskIt
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void testEvictReleaseRepo()
        throws Exception
    {
        int days = 1;
        // run Task
        this.runTask( days, "repo_releases" );

        // check files
        SortedSet<String> resultStorageFiles = this.getFilePaths( this.getStorageWorkDir() );
        SortedSet<String> resultAttributeFiles = this.getFilePaths( this.getAttributesWorkDir() );

        // unexpected deleted files
        SortedSet<String> storageDiff = new TreeSet( this.getPathMap().keySet() );
        storageDiff.removeAll( resultStorageFiles );

        SortedSet<String> attributeDiff = new TreeSet( this.getPathMap().keySet() );
        attributeDiff.removeAll( resultAttributeFiles );

        Assert.assertTrue( storageDiff.isEmpty(),
                           "Files deleted that should not have been: " + this.prettyList( storageDiff ) );
        Assert.assertTrue( attributeDiff.isEmpty(),
                           "Files deleted that should not have been: " + this.prettyList( attributeDiff ) );
    }
}
