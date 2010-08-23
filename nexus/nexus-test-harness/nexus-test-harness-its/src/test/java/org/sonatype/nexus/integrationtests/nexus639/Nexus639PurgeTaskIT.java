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
package org.sonatype.nexus.integrationtests.nexus639;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.PurgeTimelineTaskDescriptor;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Test if the Purge Timeline Task works.
 */
public class Nexus639PurgeTaskIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void doPurgeTaskTest()
        throws Exception
    {
        // an artifact was deployed already, so test the deploy feed has something.

        SyndFeed feed = FeedUtil.getFeed( "recentlyDeployedArtifacts" );
        List<SyndEntry> entries = feed.getEntries();

        Assert.assertTrue( entries.size() > 0, "Expected artifacts in the recentlyDeployed feed." );

        // run the purge task for everything
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "purgeOlderThan" );
        repo.setValue( "0" );
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( "purge", PurgeTimelineTaskDescriptor.ID, repo );

        Assert.assertNotNull( task );
        Assert.assertEquals( task.getStatus(), "SUBMITTED"  );

        // validate the feeds contain nothing.

        feed = FeedUtil.getFeed( "recentlyDeployedArtifacts" );
        entries = feed.getEntries();

        // for ( SyndEntry syndEntry : entries )
        // {
        // ! should use logger
        // System.out.println( "entry: "+ syndEntry.getTitle() );
        // }
        //
        Assert.assertTrue( entries.size() == 0, "Expected ZERO artifacts in the recentlyDeployed feed." );
    }

}
