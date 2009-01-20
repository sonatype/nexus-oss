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
package org.sonatype.nexus.integrationtests.nexus970;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

/**
 *When deleting a repository folders related to it is should be removed from disk
 */
public class Nexus970DeleteRepositoryTest
    extends AbstractNexusIntegrationTest
{

    /*
     * a) if repo is created using Nexus default storage (within ${sonatype-work}/nexus/storage), then Nexus could trash
     * the storage directory with all the files (but again, there might be involved a lot of FS operations if repo is
     * big)
     */
    @Test
    public void deleteOnDefaultStorage()
        throws Exception
    {
        File storageDir = new File( nexusBaseDir, "runtime/work/storage/nexus970-default" );
        File artifactFile = new File( storageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        // sanity check
        Assert.assertTrue( storageDir.isDirectory() );
        Assert.assertTrue( artifactFile.isFile() );

        String uri = "service/local/repositories/nexus970-default";
        Status status = RequestFacade.sendMessage( uri, Method.DELETE ).getStatus();
        Assert.assertTrue( "Unable to delete nexus970-default repository", status.isSuccess() );

        Thread.sleep( 1000 );

        Assert.assertFalse( "Artifacts shouldn't exists on deleted repo", artifactFile.exists() );
        Assert.assertFalse( "Storage dir should be deleted", storageDir.exists() );

        File trashStorageDir = new File( nexusBaseDir, "runtime/work/trash/nexus970-default" );
        File trashArtifactFile = new File( trashStorageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        Assert.assertTrue( "Storage should be moved to trash", trashStorageDir.isDirectory() );
        Assert.assertTrue( "Artifacts should be moved to trash", trashArtifactFile.isFile() );
    }

    /*
     * b) if repo is created outside Nexus default storage, then repo files should remain intact.
     */
    @Test
    public void deleteOnOverwroteStorage()
        throws Exception
    {
        File storageDir = getTestFile( "overwrote-repo" );
        File artifactFile = new File( storageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        // sanity check
        Assert.assertTrue( storageDir.isDirectory() );
        Assert.assertTrue( artifactFile.isFile() );

        String uri = "service/local/repositories/nexus970-overwrote";
        Status status = RequestFacade.sendMessage( uri, Method.DELETE ).getStatus();
        Assert.assertTrue( "Unable to delete nexus970-default repository", status.isSuccess() );

        Thread.sleep( 1000 );

        Assert.assertTrue( "Artifacts should exists on deleted repo", artifactFile.isFile() );
        Assert.assertTrue( "Storage dir shouldn't be deleted", storageDir.isDirectory() );

        File trashStorageDir = new File( nexusBaseDir, "runtime/work/trash/nexus970-overwrote" );
        File trashArtifactFile = new File( trashStorageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        Assert.assertFalse( "Storage shouldn't be moved to trash", trashStorageDir.exists() );
        Assert.assertFalse( "Artifacts shouldn't be moved to trash", trashArtifactFile.exists() );
    }

}
