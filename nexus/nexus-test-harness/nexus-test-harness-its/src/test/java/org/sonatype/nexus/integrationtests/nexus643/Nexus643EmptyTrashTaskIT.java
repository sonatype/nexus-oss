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
package org.sonatype.nexus.integrationtests.nexus643;

import java.io.File;
import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EmptyTrashTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests empty trash task.
 */
public class Nexus643EmptyTrashTaskIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void emptyTrashTask()
        throws Exception
    {

        delete( "nexus643" );

        File trashContent = new File( nexusWorkDir, "storage/nexus-test-harness-repo/.nexus/trash/nexus643" );
        Assert.assertTrue( trashContent.exists(), "Something should be at trash!" );

        // Empty trash content older than 1 days
        File oldTrashFile =
            new File( nexusWorkDir,
                "storage/nexus-test-harness-repo/.nexus/trash/nexus643/artifact-1/1.0.0/artifact-1-1.0.0.pom" );
        File newTrashFile =
            new File( nexusWorkDir,
                "storage/nexus-test-harness-repo/.nexus/trash/nexus643/artifact-1/1.0.0/artifact-1-1.0.0.jar" );
        oldTrashFile.setLastModified( System.currentTimeMillis() - 24L * 60L * 60L * 1000L * 2 );

        Assert.assertTrue( newTrashFile.exists(), "New trash content should be kept! " );
        Assert.assertTrue( oldTrashFile.exists(), "Old trash content should be kept!" );

        // this is unsupported, disabled for now (UI is not using it either)
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setKey( EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID );
        age.setValue( "1" );

        TaskScheduleUtil.runTask( "Empty Trash Older Than", EmptyTrashTaskDescriptor.ID, age );

        Assert.assertTrue( newTrashFile.exists(), "New trash content should be kept! " );
        Assert.assertFalse( oldTrashFile.exists(), "Old trash content should be removed!" );

        // Empty the whole trash
        TaskScheduleUtil.runTask( "Empty Whole Trash", EmptyTrashTaskDescriptor.ID );

        Assert.assertFalse( trashContent.exists(), "Trash should be empty!" );
    }

    private void delete( String groupId )
        throws IOException
    {
        String serviceURI = "service/local/repositories/nexus-test-harness-repo/content/" + groupId + "/";
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertTrue( response.getStatus().isSuccess(), "Unable to delete nexus643 artifacts" );
    }

}
