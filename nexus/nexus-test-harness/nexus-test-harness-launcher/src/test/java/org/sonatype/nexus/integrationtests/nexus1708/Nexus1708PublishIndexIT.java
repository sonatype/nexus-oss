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
package org.sonatype.nexus.integrationtests.nexus1708;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.PublishIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test task Publish Indexes is working.
 * 
 * @author marvin
 */
public class Nexus1708PublishIndexIT
    extends AbstractNexusIntegrationTest
{

    public Nexus1708PublishIndexIT()
    {
        super( "nexus-test-harness-repo" );
    }

    @Test
    public void publishIndex()
        throws Exception
    {
        File repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-repo" );
        File index = new File( repositoryPath, ".index" );

        Assert.assertFalse( ".index shouldn't exists before publish index task is run.", index.exists() );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "nexus-test-harness-repo" );

        TaskScheduleUtil.runTask( PublishIndexesTaskDescriptor.ID, prop );

        Assert.assertTrue( ".index should exists after publish index task was run.", index.exists() );
    }

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }
}
