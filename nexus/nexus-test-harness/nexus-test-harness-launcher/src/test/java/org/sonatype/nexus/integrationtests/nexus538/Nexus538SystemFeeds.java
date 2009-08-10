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
package org.sonatype.nexus.integrationtests.nexus538;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

public class Nexus538SystemFeeds
    extends AbstractNexusIntegrationTest
{
    @Test
    public void bootEventTest()
        throws Exception, IllegalArgumentException, FeedException
    {
        Assert.assertTrue( findFeedEntry( FeedUtil.getFeed( "systemChanges" ), "Booting", null ) );
    }

    @Test
    public void updateRepoTest()
        throws IOException, IllegalArgumentException, FeedException, InterruptedException, ComponentLookupException
    {
        // change the name of the test repo
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );

        RepositoryBaseResource repo = repoUtil.getRepository( this.getTestRepositoryId() );
        String oldName = repo.getName();
        String newName = repo.getName() + "-new";
        repo.setName( newName );
        repoUtil.updateRepo( repo );

        final SyndFeed feed = FeedUtil.getFeed( "systemChanges" );
        Assert.assertTrue( "Update repo feed not found\r\n\r\n" + feed, findFeedEntry( feed, "Configuration change",
                                                                               new String[] { newName, oldName } ) );
    }

    @Test
    public void changeProxyStatusTest()
        throws IOException, IllegalArgumentException, FeedException, InterruptedException, ComponentLookupException
    {
        // change the name of the test repo
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );

        RepositoryStatusResource repo = repoUtil.getStatus( "release-proxy-repo-1" );
        repo.setProxyMode( ProxyMode.BLOCKED_AUTO.name() );
        repoUtil.updateStatus( repo );

        Assert.assertTrue( findFeedEntry( FeedUtil.getFeed( "systemChanges" ), "Repository proxy mode change",
                                          new String[] { "release-proxy-repo-1" } ) );
        Assert.assertTrue( findFeedEntry( FeedUtil.getFeed( "systemRepositoryStatusChanges" ),
                                          "Repository proxy mode change", new String[] { "release-proxy-repo-1" } ) );
    }

    @SuppressWarnings( "unchecked" )
    private boolean findFeedEntry( SyndFeed feed, String title, String[] bodyPortions )
    {
        List<SyndEntry> entries = feed.getEntries();

        for ( SyndEntry entry : entries )
        {
            if ( entry.getTitle().equals( title ) )
            {
                if ( bodyPortions == null )
                {
                    return true;
                }

                boolean missingPortion = false;

                SyndContent description = entry.getDescription();
                String value = description.getValue();
                for ( int i = 0; i < bodyPortions.length; i++ )
                {
                    if ( !value.contains( bodyPortions[i] ) )
                    {
                        missingPortion = true;
                        break;
                    }
                }

                if ( !missingPortion )
                {
                    return true;
                }
            }
        }

        return false;
    }

}
