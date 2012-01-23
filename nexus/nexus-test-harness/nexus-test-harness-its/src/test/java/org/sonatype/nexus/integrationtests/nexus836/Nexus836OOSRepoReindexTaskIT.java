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
package org.sonatype.nexus.integrationtests.nexus836;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus836OOSRepoReindexTaskIT
    extends AbstractNexusIntegrationTest
{

    @BeforeMethod
    public void putOutOfService()
        throws Exception
    {
        RepositoryStatusMessageUtil.putOutOfService( REPO_TEST_HARNESS_SHADOW, "hosted" );
    }

    @Test
    public void expireAllRepos()
        throws Exception
    {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( "all_repo" );

        TaskScheduleUtil.waitForAllTasksToStop();

        TaskScheduleUtil.runTask( UpdateIndexTaskDescriptor.ID, prop );
    }
}
