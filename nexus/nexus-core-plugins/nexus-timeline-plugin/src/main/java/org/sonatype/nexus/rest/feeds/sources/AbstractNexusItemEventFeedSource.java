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
package org.sonatype.nexus.rest.feeds.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.feeds.NexusArtifactEvent;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * All NexusArtifactEvent related FeedSource should be inherited from this class. </br> Here we name it with
 * "NexusItemEvent" is because file is a kind of item, maven artifact is another, (maybe there will be p2 items) so
 * NexusArtifactEvent is misleading.
 * 
 * @author Juven Xu
 */
public abstract class AbstractNexusItemEventFeedSource
    extends AbstractFeedSource
{
    public abstract List<NexusArtifactEvent> getEventList( Integer from, Integer count, Map<String, String> params );

    public abstract SyndEntryBuilder<NexusArtifactEvent> getSyndEntryBuilder( NexusArtifactEvent event );

    public SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
    {
        SyndFeedImpl feed = createFeed();

        List<NexusArtifactEvent> events = getEventList( from, count, params );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( events.size() );

        for ( NexusArtifactEvent event : events )
        {
            SyndEntryBuilder<NexusArtifactEvent> entryBuilder = getSyndEntryBuilder( event );

            if ( entryBuilder.shouldBuildEntry( event ) )
            {
                entries.add( getSyndEntryBuilder( event ).buildEntry( event ) );
            }
        }

        feed.setEntries( entries );

        return feed;
    }

    private SyndFeedImpl createFeed()
    {
        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getApplicationStatusSource().getSystemStatus().getVersion() );

        feed.setPublishedDate( new Date() );

        return feed;
    }

    protected Set<String> getRepoIdsFromParams( Map<String, String> params )
    {
        if ( params != null && params.containsKey( "r" ) )
        {
            HashSet<String> result = new HashSet<String>();

            String value = params.get( "r" );

            if ( value.contains( "," ) )
            {
                String[] values = StringUtils.split( value, "," );

                result.addAll( Arrays.asList( values ) );
            }
            else
            {
                result.add( value );
            }

            return result;
        }
        else
        {
            return null;
        }
    }

}
