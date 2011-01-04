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
package org.sonatype.nexus.integrationtests.nexus970;

import java.io.File;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *When deleting a repository folders related to it is should be removed from disk
 */
public class Nexus970DeleteRepositoryIT
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
        File storageDir = new File( nexusWorkDir, "storage/nexus970-default" );
        File artifactFile = new File( storageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        // sanity check
        Assert.assertTrue( storageDir.isDirectory() );
        Assert.assertTrue( artifactFile.isFile() );

        String uri = "service/local/repositories/nexus970-default";
        Status status = RequestFacade.sendMessage( uri, Method.DELETE ).getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to delete nexus970-default repository" );

        // give a chance to wait for task to start
        Thread.sleep( 1500 );
        getEventInspectorsUtil().waitForCalmPeriod();
        TaskScheduleUtil.waitForAllTasksToStop();
        

        Assert.assertFalse( artifactFile.exists(), "Artifacts shouldn't exists on deleted repo" );
        Assert.assertFalse( storageDir.exists(), "Storage dir should be deleted" );

        File trashStorageDir = new File( nexusWorkDir, "trash/nexus970-default" );
        File trashArtifactFile = new File( trashStorageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        Assert.assertTrue( trashStorageDir.isDirectory(), "Storage should be moved to trash" );
        Assert.assertTrue( trashArtifactFile.isFile(), "Artifacts should be moved to trash" );
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
        Assert.assertTrue( status.isSuccess(), "Unable to delete nexus970-default repository" );

        // give a chance to wait for task to start
        Thread.sleep( 500 );
        TaskScheduleUtil.waitForAllTasksToStop();

        Assert.assertTrue( artifactFile.isFile(), "Artifacts should exists on deleted repo" );
        Assert.assertTrue( storageDir.isDirectory(), "Storage dir shouldn't be deleted" );

        File trashStorageDir = new File( nexusWorkDir, "trash/nexus970-overwrote" );
        File trashArtifactFile = new File( trashStorageDir, "nexus970/artifact/1.0.0/artifact-1.0.0.jar" );

        Assert.assertFalse( trashStorageDir.exists(), "Storage shouldn't be moved to trash" );
        Assert.assertFalse( trashArtifactFile.exists(), "Artifacts shouldn't be moved to trash" );
    }

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

}
