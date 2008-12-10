/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus779;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.test.utils.FeedUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public abstract class AbstractRssTest
    extends AbstractPrivilegeTest
{

    private static final String RECENTLY_DEPLOYED = "recentlyDeployed";

    private List<SyndEntry> entries;

    public AbstractRssTest( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    public AbstractRssTest()
    {
        super();
    }

    protected String entriesToString()
        throws Exception
    {
        if ( entries == null )
        {
            return "No entries";
        }

        StringBuffer buffer = new StringBuffer();

        for ( SyndEntry syndEntry : entries )
        {
            buffer.append( "\n" ).append( syndEntry.getTitle() );
        }

        return buffer.toString();
    }

    @SuppressWarnings( "unchecked" )
    protected boolean feedListContainsArtifact( String groupId, String artifactId, String version )
        throws Exception
    {
        SyndFeed feed = FeedUtil.getFeed( RECENTLY_DEPLOYED );
        entries = feed.getEntries();

        for ( SyndEntry entry : entries )
        {
            if ( entry.getTitle().contains( groupId ) && entry.getTitle().contains( artifactId )
                && entry.getTitle().contains( version ) )
            {
                return true;
            }
        }
        return false;
    }

}