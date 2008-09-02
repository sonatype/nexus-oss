package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test SnapshotRemoverTask to remove old artifacts but keep updated artifacts
 * @author marvin
 */
public class Nexus634KeepTwoSnapshotsTest
    extends AbstractSnapshotRemoverTest
{

    @Test
    public void keepTwoSnapshots()
        throws Exception
    {

        // This is THE important part
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 2, 0, true );

        Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertEquals( "SnapshotRemoverTask should remove only old artifacts", 2, jars.size() );
    }

}
