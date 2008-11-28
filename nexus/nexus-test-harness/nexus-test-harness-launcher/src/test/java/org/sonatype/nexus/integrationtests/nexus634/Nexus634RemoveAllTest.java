package org.sonatype.nexus.integrationtests.nexus634;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test SnapshotRemoverTask to remove all artifacts
 * 
 * @author marvin
 */
public class Nexus634RemoveAllTest
    extends AbstractSnapshotRemoverTest
{

    @Test
    public void removeAllSnapshots()
        throws Exception
    {
        // This is THE important part
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 0, 0, true );

        // this IT is wrong: nexus will remove the parent folder too, if the GAV folder is emptied completely
        // Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        // Assert.assertTrue( "All artifacts should be deleted by SnapshotRemoverTask. Found: " + jars, jars.isEmpty()
        // );

        // looking at the IT resources, there is only one artifact in there, hence, the dir should be removed
        Assert.assertFalse(
            "The folder should be removed since all artifacts should be gone, instead there are files left!",
            artifactFolder.exists() );
    }

}
