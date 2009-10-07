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
package org.sonatype.nexus.integrationtests.nexus640;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Tests the rebuild repository attributes task.
 */
public class Nexus640RebuildRepositoryAttributesTaskTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void rebuildAttributes()
        throws Exception
    {
        String attributePath = "proxy/attributes/nexus-test-harness-repo/nexus640/artifact/1.0.0/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );

        File jar = new File( nexusWorkDir, attributePath + "artifact-1.0.0.jar" );
        Assert.assertTrue( "Attribute files should be generated after rebuild", jar.exists() );
        File pom = new File( nexusWorkDir, attributePath + "artifact-1.0.0.pom" );
        Assert.assertTrue( "Attribute files should be generated after rebuild", pom.exists() );

    }

}
