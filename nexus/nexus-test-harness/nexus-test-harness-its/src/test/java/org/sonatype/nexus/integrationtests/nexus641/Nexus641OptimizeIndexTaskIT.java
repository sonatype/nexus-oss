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
package org.sonatype.nexus.integrationtests.nexus641;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.OptimizeIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test task OptimizeIndex Repositories.
 * 
 * @author marvin
 */
public class Nexus641OptimizeIndexTaskIT
    extends AbstractNexusIntegrationTest
{
    protected static Logger logger = Logger.getLogger( Nexus641OptimizeIndexTaskIT.class );

    public Nexus641OptimizeIndexTaskIT()
        throws IOException
    {
        super( "nexus641" );
    }

    @Test
    public void testIndexOptimizer()
        throws Exception
    {
        // reindex
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryOrGroupId" );
        prop.setValue( "nexus-test-harness-repo" );

        // reindex
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( OptimizeIndexTaskDescriptor.ID, prop );
        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );

    }

}
