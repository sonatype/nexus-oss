/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.feeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.proxy.access.AccessDecisionVoter;
import org.sonatype.nexus.proxy.access.IpAddressAccessDecisionVoter;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * The system changes feed.
 * 
 * @author cstamas
 * @plexus.component role-hint="systemChanges"
 */
public class SystemFeedSource
    extends AbstractFeedSource
{
    public static final String CHANNEL_KEY = "systemChanges";

    public List<SystemEvent> getEventList()
    {
        return getNexus().getSystemEvents();
    }

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
        return "System changes in Nexus.";
    }

    @Override
    public String getTitle()
    {
        return "Nexus System Changes";
    }

    public SyndFeed getFeed()
    {
        List<SystemEvent> items = getEventList();

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemState().getVersion() );

        feed.setPublishedDate( new Date() );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( items.size() );

        SyndEntry entry = null;

        SyndContent content = null;

        String username = null;

        String ipAddress = null;

        String itemLink = null;

        int i = 0;

        for ( SystemEvent item : items )
        {
            i++;

            if ( item.getEventContext().containsKey( AccessDecisionVoter.REQUEST_USER ) )
            {
                username = (String) item.getEventContext().get( AccessDecisionVoter.REQUEST_USER );
            }
            else
            {
                username = null;
            }

            if ( item.getEventContext().containsKey( IpAddressAccessDecisionVoter.REQUEST_REMOTE_ADDRESS ) )
            {
                ipAddress = (String) item.getEventContext().get( IpAddressAccessDecisionVoter.REQUEST_REMOTE_ADDRESS );
            }
            else
            {
                ipAddress = null;
            }

            StringBuffer msg = new StringBuffer( item.toString() ).append( " On " ).append(
                formatDate( item.getEventDate() ) ).append( ". " );

            if ( username != null )
            {
                msg.append( " It was initiated by a request from user " ).append( username ).append( "." );
            }

            if ( ipAddress != null )
            {
                msg.append( " The request was originated from IP address " ).append( ipAddress ).append( "." );
            }

            entry = new SyndEntryImpl();

            if ( FeedRecorder.SYSTEM_BOOT_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Booting" );
            }
            else if ( FeedRecorder.SYSTEM_CONFIG_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Configuration change" );
            }
            else if ( FeedRecorder.SYSTEM_CLEARCACHE_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Clearing caches" );
            }
            else if ( FeedRecorder.SYSTEM_REINDEX_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Reindexing" );
            }
            else if ( FeedRecorder.SYSTEM_REBUILDATTRIBUTES_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Rebuilding Attributes" );
            }
            else
            {
                entry.setTitle( item.getAction() );
            }

            StringBuffer uriToAppend = new StringBuffer( "http://nexus.sonatype.org/" );

            itemLink = uriToAppend.toString();

            entry.setPublishedDate( item.getEventDate() );

            entry.setAuthor( feed.getAuthor() );

            entry.setLink( itemLink );

            content = new SyndContentImpl();

            content.setType( MediaType.TEXT_PLAIN.toString() );

            content.setValue( msg.toString() );

            entry.setDescription( content );

            entries.add( entry );
        }

        feed.setEntries( entries );

        return feed;
    }

}
