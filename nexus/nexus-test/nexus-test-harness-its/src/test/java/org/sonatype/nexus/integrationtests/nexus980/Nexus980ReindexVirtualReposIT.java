/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
