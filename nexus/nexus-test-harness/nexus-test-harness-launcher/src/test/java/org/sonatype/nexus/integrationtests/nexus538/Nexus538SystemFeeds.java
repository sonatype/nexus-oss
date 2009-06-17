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

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.thoughtworks.xstream.XStream;

public class Nexus538SystemFeeds
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void bootEventTest()
        throws Exception, IllegalArgumentException, FeedException
    {

        // restart and look for event in feed

        NexusStatusUtil.doHardStop();
        NexusStatusUtil.doHardStart();

        SyndFeed feed = FeedUtil.getFeed( "systemChanges" );
        List<SyndEntry> entries = feed.getEntries();

        for ( SyndEntry entry : entries )
        {
            if ( "Booting".equals( entry.getTitle() ) )
            {
                return;
            }
        }

        Assert.fail( "Unable to find booting entry!" );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void updateRepoTest()
        throws IOException, IllegalArgumentException, FeedException, InterruptedException, ComponentLookupException
    {

        // sleep a little so we can make sure its the first item
        // the time sorting is only accurate to the second
        Thread.sleep( 1000 );

        // change the name of the test repo
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );

        RepositoryBaseResource repo = repoUtil.getRepository( this.getTestRepositoryId() );
        String oldName = repo.getName();
        String newName = repo.getName() + "-new";
        repo.setName( newName );
        repoUtil.updateRepo( repo );

        SyndFeed feed = FeedUtil.getFeed( "systemChanges" );
        List<SyndEntry> entries = feed.getEntries();

        for ( SyndEntry entry : entries )
        {
            if ( "Configuration change".equals( entry.getTitle() )
                && entry.getDescription().getValue().contains( newName )
                && entry.getDescription().getValue().contains( oldName ) )
            {
                return;
            }
        }

        Assert.fail( "Unable to find a valid entry to repository update:\n" + new XStream().toXML( entries ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void changeProxyStatusTest()
        throws IOException, IllegalArgumentException, FeedException, InterruptedException, ComponentLookupException
    {

        // sleep a little so we can make sure its the first item
        // the time sorting is only accurate to the second
        Thread.sleep( 1000 );

        // change the name of the test repo
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );

        RepositoryStatusResource repo = repoUtil.getStatus( "release-proxy-repo-1" );
        repo.setProxyMode( ProxyMode.BLOCKED_AUTO.name() );
        repoUtil.updateStatus( repo );

        SyndFeed feed = FeedUtil.getFeed( "systemChanges" );
        List<SyndEntry> entries = feed.getEntries();

        // the first item should be the boot event
        SyndEntry entry = entries.get( 0 );
        Assert.assertEquals( "Feed entry: " + entry.getPublishedDate(), "Repository proxy mode change",
                             entry.getTitle() );
        Assert.assertTrue( "Could not find repo id in feed entry, Entry body:\n" + entry.getDescription().getValue(),
                           entry.getDescription().getValue().contains( "release-proxy-repo-1" ) );

        feed = FeedUtil.getFeed( "systemRepositoryStatusChanges" );
        entries = feed.getEntries();

        // the first item should be the boot event
        entry = entries.get( 0 );
        Assert.assertEquals( "Feed entry: " + entry.getPublishedDate(), "Repository proxy mode change",
                             entry.getTitle() );
        Assert.assertTrue( "Could not find repo id in feed entry, Entry body:\n" + entry.getDescription().getValue(),
                           entry.getDescription().getValue().contains( "release-proxy-repo-1" ) );
    }

}
