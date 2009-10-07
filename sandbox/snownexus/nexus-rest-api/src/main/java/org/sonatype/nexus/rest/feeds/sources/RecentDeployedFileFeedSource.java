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
package org.sonatype.nexus.rest.feeds.sources;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.NexusArtifactEvent;

/**
 * The overall deployments feed.
 * 
 * @author cstamas
 */
@Component( role = FeedSource.class, hint = "recentlyDeployedFiles" )
public class RecentDeployedFileFeedSource
    extends AbstractNexusItemEventFeedSource
{
    @Requirement( hint = "file" )
    private SyndEntryBuilder<NexusArtifactEvent> entryBuilder;
    
    public static final String CHANNEL_KEY = "recentlyDeployedFiles";

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    public String getFeedName()
    {
        return getDescription();
    }

    @Override
    public String getDescription()
    {
        return "New deployed files in all Nexus repositories (deployed).";
    }

    @Override
    public List<NexusArtifactEvent> getEventList( Integer from, Integer count, Map<String, String> params )
    {
        return getNexus().getRecentlyDeployedArtifacts( from, count, getRepoIdsFromParams( params ) );
    }

    @Override
    public String getTitle()
    {
        return "New deployed files";
    }

    @Override
    public SyndEntryBuilder<NexusArtifactEvent> getSyndEntryBuilder( NexusArtifactEvent event )
    {
        return entryBuilder;
    }
}
