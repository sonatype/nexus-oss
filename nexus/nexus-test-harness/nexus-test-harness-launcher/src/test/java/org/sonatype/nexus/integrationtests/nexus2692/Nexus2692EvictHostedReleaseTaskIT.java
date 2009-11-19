package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

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
        SortedSet<String> resultStorageFiles = new TreeSet<String>( FileUtils.getFileNames(
            this.getStorageWorkDir(),
            null,
            null,
            false,
            true ) );
        SortedSet<String> resultAttributeFiles = new TreeSet<String>( FileUtils.getFileNames( this
            .getAttributesWorkDir(), null, null, false, true ) );

        SortedSet<String> expectedResults = new TreeSet( this.getPathMap().keySet() );
        // nothing should have been removed

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
