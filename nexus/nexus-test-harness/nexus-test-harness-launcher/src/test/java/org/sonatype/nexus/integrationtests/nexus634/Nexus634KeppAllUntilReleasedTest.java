/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.GavUtil;

/**
 * Test SnapshotRemoverTask to remove all artifacts
 * 
 * @author marvin
 */
public class Nexus634KeppAllUntilReleasedTest
    extends AbstractSnapshotRemoverTest
{

    @Test
    public void keepAllSnapshots()
        throws Exception
    {
        // This is THE important part
        runSnapshotRemover( REPO_TEST_HARNESS_SNAPSHOT_REPO, -1, 0, true );

        Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertEquals( 2, jars.size() );

        runSnapshotRemover( REPO_TEST_HARNESS_SNAPSHOT_REPO, 0, -1, true );

        jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertEquals( 2, jars.size() );

        releaseArtifact();
        runSnapshotRemover( REPO_TEST_HARNESS_SNAPSHOT_REPO, 0, -1, true );

        Assert.assertFalse( artifactFolder.exists() );
    }

    private void releaseArtifact()
        throws Exception
    {
        DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, GavUtil.newGav( "nexus634", "artifact", "1.0" ),
                                            getTestFile( "artifact-1.jar" ) );
        DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, GavUtil.newGav( "nexus634", "artifact", "1.0",
                                                                                    "pom" ),
                                            getTestFile( "artifact-1.pom" ) );
    }

}
