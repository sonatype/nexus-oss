/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus643;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.tasks.descriptors.EmptyTrashTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Tests empty trash task.
 */
public class Nexus643EmptyTrashTaskTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void emptyTrashTask()
        throws Exception
    {

        delete( "nexus643" );

        File trashContent = new File( nexusBaseDir, "runtime/work/trash/nexus-test-harness-repo/nexus643" );
        Assert.assertTrue( "Something should be at trash!", trashContent.exists() );

        // This is THE important part
        TaskScheduleUtil.runTask( EmptyTrashTaskDescriptor.ID );

        Assert.assertFalse( "Trash should be empty!", trashContent.exists() );
    }

    private void delete( String groupId )
        throws IOException
    {
        String serviceURI = "service/local/repositories/nexus-test-harness-repo/content/" + groupId + "/";
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertTrue( "Unable to delete nexus643 artifacts", response.getStatus().isSuccess() );
    }
}
