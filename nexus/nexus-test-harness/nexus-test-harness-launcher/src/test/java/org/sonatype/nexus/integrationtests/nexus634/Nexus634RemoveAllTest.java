package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test SnapshotRemoverTask to remove all artifacts
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

        Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertTrue( "All artifacts should be deleted by SnapshotRemoverTask. Found: " + jars, jars.isEmpty() );
    }

}
