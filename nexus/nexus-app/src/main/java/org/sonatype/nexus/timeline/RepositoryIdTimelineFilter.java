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
package org.sonatype.nexus.timeline;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.feeds.DefaultFeedRecorder;
import org.sonatype.timeline.TimelineFilter;

public class RepositoryIdTimelineFilter
    implements TimelineFilter
{
    private final Set<String> repositoryIds;

    public RepositoryIdTimelineFilter( String repositoryId )
    {
        this.repositoryIds = new HashSet<String>();

        this.repositoryIds.add( repositoryId );
    }

    public RepositoryIdTimelineFilter( Set<String> repositoryIds )
    {
        this.repositoryIds = repositoryIds;
    }

    public boolean accept( Map<String, String> hit )
    {
        return ( hit.containsKey( DefaultFeedRecorder.REPOSITORY ) && repositoryIds.contains( hit
            .get( DefaultFeedRecorder.REPOSITORY ) ) );
    }
}
