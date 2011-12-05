/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus639;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.timeline.tasks.PurgeTimelineTaskDescriptor;
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
        TaskScheduleUtil.runTask( "purge", PurgeTimelineTaskDescriptor.ID, repo );

        // validate the feeds contain nothing.

        feed = FeedUtil.getFeed( "recentlyDeployedArtifacts" );
        entries = feed.getEntries();

        Assert.assertTrue( entries.size() == 0, "Expected ZERO artifacts in the recentlyDeployed feed." );
    }

}
