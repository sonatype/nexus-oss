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
package org.sonatype.nexus.integrationtests.nexus980;

import static org.sonatype.nexus.integrationtests.ITGroups.INDEX;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Reindex a virtual repo should fail
 */
public class Nexus980ReindexVirtualReposIT
    extends AbstractNexusIntegrationTest
{

    @Test(groups = INDEX)
    public void manualReindex()
        throws IOException
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( Nexus980ReindexVirtualReposIT.class, "manualReindex" );
            return;
        }
        String serviceURI = "service/local/data_index/repositories/nexus-test-harness-shadow/content";
        Status status = RequestFacade.sendMessage( serviceURI, Method.DELETE ).getStatus();
        Assert.assertFalse( status.isSuccess(), "Should not being able to reindex a shadow repo" );
    }

    @Test(groups = INDEX)
    public void taskReindex()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( Nexus980ReindexVirtualReposIT.class, "taskReindex" );
            return;
        }
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( REPO_TEST_HARNESS_SHADOW );

        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( "reindex_shadow" );
        scheduledTask.setTypeId( UpdateIndexTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( prop );
        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertFalse( status.isSuccess(), "Should not be able to create a reindex task to a virtual repo" );
    }

}
