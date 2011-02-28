/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus169;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Date;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Adds metadata in a snapshot repo, then checks to see if it was not changed ( future version of nexus may clean metadata on the fly.)
 */
public class Nexus169ReleaseMetaDataInSnapshotRepoIT
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    private static final String TEST_GROUP = "nexus-test-harness-snapshot-group";

    public Nexus169ReleaseMetaDataInSnapshotRepoIT()
    {
        super( TEST_SNAPSHOT_REPO );
    }
    
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void releaseMetaDataInSnapshotRepo()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "simple-artifact", "1.0.4", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, null, false, null );

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

        Assert.assertFalse( fileWasDownloaded, "The file was downloaded and it should not have been." );

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

        Assert.assertFalse( fileWasDownloaded, "The file was downloaded and it should not have been." );
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

        Reader is = new FileReader(snapShotMetaDataFile);
        Metadata snapshotRepoMetaData = r.read( is );
        is.close();

        File groupMetaDataFile = this.downloadFile( snapshotRepoMetaDataURL, "./target/downloads/groupMetaData.xml" );
        is = new FileReader(groupMetaDataFile);
        Metadata groupMetaData = r.read( is );
        is.close();

        Assert.assertTrue(
                           snapshotRepoMetaData.getVersioning().getVersions().size() == 2,
                           "Metadata from snapshot repo does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test." );
        Assert.assertTrue(
                           groupMetaData.getVersioning().getVersions().size() == 2,
                           "Metadata from group does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test." );

    }

}
