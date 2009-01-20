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

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test task Reindex Repositories.
 * @author marvin
 */
public class Nexus641ReindexTaskTest
    extends AbstractNexusIntegrationTest
{
    
    private SearchMessageUtil messageUtil = new SearchMessageUtil();
    
    private File repositoryPath = new File( nexusBaseDir, "runtime/work/storage/"+ this.getTestRepositoryId() );

    
    public Nexus641ReindexTaskTest() throws IOException
    {
        super( "nexus641" );
        
    }
    
    @Test
    public void testReindex()
        throws Exception
    {

        this.repositoryPath = new File( nexusBaseDir, "runtime/work/storage/"+ this.getTestRepositoryId() );
        System.out.println( "path: "+ repositoryPath );
        File oldSnapshot = getTestFile( "repo" );

        // Copy artifact to avoid indexing
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        // try to seach and fail
        List<NexusArtifact> search = messageUtil.searchFor( "nexus641" );
        Assert.assertEquals( "The artifact was already indexed", 1, search.size() );

        // reindex
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "nexus-test-harness-repo" );

        // reindex
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ReindexTaskDescriptor.ID, prop );
        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
        
        // try to download again and success
        search = messageUtil.searchFor( "nexus641" );
        Assert.assertEquals( "The artifact should be indexed", 2, search.size() );
    }

}
