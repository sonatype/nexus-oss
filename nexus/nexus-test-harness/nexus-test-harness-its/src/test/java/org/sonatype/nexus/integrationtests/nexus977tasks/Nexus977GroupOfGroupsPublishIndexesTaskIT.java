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
package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.DownloadIndexesTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.PublishIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsPublishIndexesTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        // first must be sure there is an index to be published
        RepositoryMessageUtil.updateIndexes( "r1", "r2", "r3" );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "repository_r4" );
        TaskScheduleUtil.runTask( "r4", DownloadIndexesTaskDescriptor.ID, repo );
        
        repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "repository_r5" );
        TaskScheduleUtil.runTask( "r5", DownloadIndexesTaskDescriptor.ID, repo );
    }

    @Test
    public void publishIndexes()
        throws Exception
    {
        Assert.assertFalse( new File( nexusWorkDir, "storage/g1/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g2/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g3/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g4/.index" ).exists() );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "g4" );
        TaskScheduleUtil.runTask( "PublishIndexesTaskDescriptor-snapshot", PublishIndexesTaskDescriptor.ID, repo );
        
        Assert.assertTrue( new File( nexusWorkDir, "storage/r1/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r2/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r3/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r4/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r5/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g1/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g2/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g3/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g4/.index" ).exists() );
    }

}
