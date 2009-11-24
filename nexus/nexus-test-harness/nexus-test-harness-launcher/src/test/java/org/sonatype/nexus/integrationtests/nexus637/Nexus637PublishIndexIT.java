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
package org.sonatype.nexus.integrationtests.nexus637;

import java.io.File;
import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.PublishIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test task Publish Indexes is working.
 * 
 * @author marvin
 */
public class Nexus637PublishIndexIT
    extends AbstractNexusIntegrationTest
{

    public Nexus637PublishIndexIT()
    {
        super( "nexus-test-harness-repo" );
    }

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

    @Test
    public void publishIndex()
        throws Exception
    {
        File repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-repo" );
        File index = new File( repositoryPath, ".index" );

        if ( index.exists() )
        {
            // can't contain the OSS index
            Assert.assertThat(
                               Arrays.asList( index.list() ),
                               CoreMatchers.not( IsCollectionContaining.hasItems(
                                                                                  "nexus-maven-repository-index.gz",
                                                                                  "nexus-maven-repository-index.gz.md5",
                                                                                  "nexus-maven-repository-index.gz.sha1",
                                                                                  "nexus-maven-repository-index.properties",
                                                                                  "nexus-maven-repository-index.properties.md5",
                                                                                  "nexus-maven-repository-index.properties.sha1",
                                                                                  "nexus-maven-repository-index.zip",
                                                                                  "nexus-maven-repository-index.zip.md5",
                                                                                  "nexus-maven-repository-index.zip.sha1" ) ) );
        }

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "nexus-test-harness-repo" );

        TaskScheduleUtil.runTask( PublishIndexesTaskDescriptor.ID, prop );

        Assert.assertTrue( ".index should exists after publish index task was run.", index.exists() );
    }
}
