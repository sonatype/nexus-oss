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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.proxy.access.AccessManager;

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
 */
public abstract class AbstractSystemFeedSource
    extends AbstractFeedSource
{
    public abstract List<SystemEvent> getEventList( Integer from, Integer count, Map<String, String> params );

    public SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
    {
        List<SystemEvent> items = getEventList( from, count, params );

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemStatus().getVersion() );

        feed.setPublishedDate( new Date() );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( items.size() );

        SyndEntry entry = null;

        SyndContent content = null;

        String username = null;

        String ipAddress = null;

        int i = 0;

        for ( SystemEvent item : items )
        {
            i++;

            if ( item.getEventContext().containsKey( AccessManager.REQUEST_USER ) )
            {
                username = (String) item.getEventContext().get( AccessManager.REQUEST_USER );
            }
            else
            {
                username = null;
            }

            if ( item.getEventContext().containsKey( AccessManager.REQUEST_REMOTE_ADDRESS ) )
            {
                ipAddress = (String) item.getEventContext().get( AccessManager.REQUEST_REMOTE_ADDRESS );
            }
            else
            {
                ipAddress = null;
            }

            StringBuilder msg = new StringBuilder( item.getMessage() ).append( ". " );

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
            else if ( FeedRecorder.SYSTEM_TL_PURGE_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Timeline purge" );
            }
            else if ( FeedRecorder.SYSTEM_REINDEX_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Reindexing" );
            }
            else if ( FeedRecorder.SYSTEM_PUBLISHINDEX_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Publishing indexes" );
            }
            else if ( FeedRecorder.SYSTEM_REBUILDATTRIBUTES_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Rebuilding attributes" );
            }
            else if ( FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Repository local status change" );
            }
            else if ( FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Repository proxy mode change" );
            }
            else if ( FeedRecorder.SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Repository proxy mode change (user intervention may be needed!)" );
            }
            else if ( FeedRecorder.SYSTEM_EXPIRE_CACHE_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Expiring caches" );
            }
            else if ( FeedRecorder.SYSTEM_EVICT_UNUSED_PROXIED_ITEMS_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Evicting unused proxied items" );
            }
            else if ( SnapshotRemoverTask.SYSTEM_REMOVE_SNAPSHOTS_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Removing snapshots" );
            }
            else if ( FeedRecorder.SYSTEM_REMOVE_REPO_FOLDER_ACTION.equals( item.getAction() ) )
            {
            	entry.setTitle( "Removing repository folder" );
            }
            else if ( FeedRecorder.SYSTEM_EMPTY_TRASH_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Emptying Trash" );
            }
            else if ( FeedRecorder.SYSTEM_SYNC_SHADOW_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Synchronizing Shadow Repository" );
            }
            else if ( RebuildMavenMetadataTask.REBUILD_MAVEN_METADATA_ACTION.equals( item.getAction() ) )
            {
                entry.setTitle( "Rebuilding maven metadata files" );
            }
            else if ( FeedRecorder.SYSTEM_DOWNLOADINDEX_ACTION.equals( item.getAction() ))
            {
                entry.setTitle( "Downloading Indexes" );
            }
            else
            {
                entry.setTitle( item.getAction() );
            }

            content = new SyndContentImpl();

            content.setType( MediaType.TEXT_PLAIN.toString() );

            content.setValue( msg.toString() );

            entry.setPublishedDate( item.getEventDate() );

            entry.setAuthor( feed.getAuthor() );

            entry.setLink( "/" );

            entry.setDescription( content );

            entries.add( entry );
        }

        feed.setEntries( entries );

        return feed;
    }

}
