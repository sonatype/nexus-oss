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
package org.sonatype.nexus.integrationtests.nexus169;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

/**
 * Adds metadata in a snapshot repo, then checks to see if it was not changed ( future version of nexus may clean metadata on the fly.) 
 */
public class Nexus169ReleaseMetaDataInSnapshotRepoTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    private static final String TEST_GROUP = "nexus-test-harness-snapshot-group";

    public Nexus169ReleaseMetaDataInSnapshotRepoTest()
    {
        super( TEST_SNAPSHOT_REPO );
    }

    @Test
    public void releaseMetaDataInSnapshotRepo()
        throws IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "simple-artifact", "1.0.4", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // try to download it
        boolean fileWasDownloaded = true;
        try
        {
            // download it
            downloadArtifactFromRepository( TEST_SNAPSHOT_REPO, gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasDownloaded = false;
        }

        Assert.assertFalse( "The file was downloaded and it should not have been.", fileWasDownloaded );

        fileWasDownloaded = true;
        try
        {
            // download it
            downloadArtifactFromGroup( TEST_GROUP, gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasDownloaded = false;
        }

        Assert.assertFalse( "The file was downloaded and it should not have been.", fileWasDownloaded );
    }

    @Test
    public void metadataCleaningTest()
        throws IOException, XmlPullParserException
    {
        // now we are going to grab the maven-metadata.xml, and take a look at that, the release version should have
        // been stripped out.

        URL snapshotRepoMetaDataURL =
            new URL( this.getNexusTestRepoUrl() + this.getTestId().replace( '.', '/' )
                + "/simple-artifact/maven-metadata.xml" );
        URL groupMetaDataURL =
            new URL( this.getBaseNexusUrl() + GROUP_REPOSITORY_RELATIVE_URL + TEST_GROUP + "/"
                + this.getTestId().replace( '.', '/' ) + "/simple-artifact/maven-metadata.xml" );

        log.debug( "snapshotRepoMetaDataURL: " + snapshotRepoMetaDataURL );
        log.debug( "groupMetaDataURL: " + groupMetaDataURL );

        // // download the two meta data files
        // File snapshotRepoMetaDataFile = this.downloadFile( snapshotRepoMetaDataURL,
        // "./target/downloaded-jars/snapshotRepoMetaDataURL.xml" );
        // File groupMetaDataFile = this.downloadFile( groupMetaDataURL,
        // "./target/downloaded-jars/groupMetaDataFile.xml" );
        // these files should be the same
        // FIXME: add check

        // check the versions of the file
        MetadataXpp3Reader r = new MetadataXpp3Reader();

        File snapShotMetaDataFile = this.downloadFile( snapshotRepoMetaDataURL, "./target/downloads/snapshotMetaData.xml" );
        
        InputStream is = new FileInputStream(snapShotMetaDataFile);
        Metadata snapshotRepoMetaData = r.read( is );
        is.close();

        File groupMetaDataFile = this.downloadFile( snapshotRepoMetaDataURL, "./target/downloads/groupMetaData.xml" );
        is = new FileInputStream(groupMetaDataFile);
        Metadata groupMetaData = r.read( is );
        is.close();

        Assert.assertTrue(
                           "Metadata from snapshot repo does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test.",
                           snapshotRepoMetaData.getVersioning().getVersions().size() == 2 );
        Assert.assertTrue(
                           "Metadata from group does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test.",
                           groupMetaData.getVersioning().getVersions().size() == 2 );

    }

}
