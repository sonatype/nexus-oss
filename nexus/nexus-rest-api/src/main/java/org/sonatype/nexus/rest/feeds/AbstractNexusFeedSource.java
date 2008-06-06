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
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.AccessDecisionVoter;
import org.sonatype.nexus.proxy.access.IpAddressAccessDecisionVoter;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * And abstract class for NexusArtifactEvent based feeds. This class implements all needed to create a feed,
 * implementors needs only to implement 3 abtract classes.
 * 
 * @author cstamas
 */
public abstract class AbstractNexusFeedSource
    extends AbstractFeedSource
{
    /**
     * Creates/formats GAV strings from ArtifactInfo.
     * 
     * @param ai
     * @return
     */
    protected String getGav( NexusItemInfo ai )
    {
        if ( ai == null )
        {
            return "unknown : unknown : unknown";
        }
        else
        {
            try
            {
                Repository repo = getNexus().getRepository( ai.getRepositoryId() );

                if ( MavenRepository.class.isAssignableFrom( repo.getClass() ) )
                {
                    Gav gav = ( (MavenRepository) repo ).getGavCalculator().pathToGav( ai.getPath() );

                    if ( gav != null )
                    {
                        StringBuffer result = new StringBuffer( gav.getGroupId() ).append( " : " ).append(
                            gav.getArtifactId() ).append( " : " ).append(
                            gav.getVersion() != null ? gav.getVersion() : "unknown" );

                        if ( gav.getClassifier() != null )
                        {
                            result.append( " : " ).append( gav.getClassifier() );
                        }

                        return result.toString();
                    }
                    else
                    {
                        return ai.getPath();
                    }
                }
                else
                {
                    return ai.getPath();
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                return ai.getPath();
            }
        }
    }

    public abstract List<NexusArtifactEvent> getEventList();

    public abstract String getTitle();

    public abstract String getDescription();

    public SyndFeed getFeed()
    {
        List<NexusArtifactEvent> items = getEventList();

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemState().getVersion() );

        feed.setPublishedDate( new Date() );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( items.size() );

        SyndEntry entry = null;

        SyndContent content = null;

        String gav = null;

        String username = null;

        String ipAddress = null;

        String itemLink = null;

        for ( NexusArtifactEvent item : items )
        {
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

            gav = getGav( item.getNexusItemInfo() );

            StringBuffer msg = new StringBuffer( "On " )
                .append( formatDate( item.getEventDate() ) ).append( " the " ).append( gav ).append(
                    " artifact in repository " ).append( item.getNexusItemInfo().getRepositoryId() ).append( " is " );

            if ( NexusArtifactEvent.ACTION_CACHED.equals( item.getAction() ) )
            {
                msg
                    .append( "cached by Nexus from remote URL " ).append( item.getNexusItemInfo().getRemoteUrl() )
                    .append( "." );

                if ( username != null )
                {
                    msg.append( " Caching happened to fulfill a request from user " ).append( username ).append( "." );
                }
            }
            else if ( NexusArtifactEvent.ACTION_DEPLOYED.equals( item.getAction() ) )
            {
                msg.append( "deployed onto Nexus." );

                if ( username != null )
                {
                    msg.append( " Deployment was initiated by user " ).append( username ).append( "." );
                }
            }
            else if ( NexusArtifactEvent.ACTION_DELETED.equals( item.getAction() ) )
            {
                msg.append( "deleted from Nexus." );

                if ( username != null )
                {
                    msg.append( " Deletion was initiated by user " ).append( username ).append( "." );
                }
            }
            else if ( NexusArtifactEvent.ACTION_BROKEN.equals( item.getAction() ) )
            {
                msg.append( "broken." );

                if ( item.getMessage() != null )
                {
                    msg.append( " Details: \n" );

                    msg.append( item.getMessage() );

                    msg.append( "\n" );
                }

                if ( username != null )
                {
                    msg.append( " Processing was initiated by user " ).append( username ).append( "." );
                }
            }
            else if ( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM.equals( item.getAction() ) )
            {
                msg.append( "proxied, and the remote repository contains wrong checksum for it." );

                if ( item.getMessage() != null )
                {
                    msg.append( " Details: \n" );

                    msg.append( item.getMessage() );

                    msg.append( "\n" );
                }

                if ( username != null )
                {
                    msg.append( " Processing was initiated by user " ).append( username ).append( "." );
                }
            }
            else if ( NexusArtifactEvent.ACTION_RETRIEVED.equals( item.getAction() ) )
            {
                msg.append( "served by Nexus." );

                if ( username != null )
                {
                    msg.append( " Request was initiated by user " ).append( username ).append( "." );
                }
            }

            if ( ipAddress != null )
            {
                msg.append( " The request was originated from IP address " ).append( ipAddress ).append( "." );
            }

            StringBuffer uriToAppend = new StringBuffer( "content/repositories/" ).append(
                item.getNexusItemInfo().getRepositoryId() ).append( item.getNexusItemInfo().getPath() );

            itemLink = uriToAppend.toString();

            entry = new SyndEntryImpl();

            entry.setTitle( getGav( item.getNexusItemInfo() ) );

            entry.setLink( itemLink );

            entry.setPublishedDate( item.getEventDate() );

            entry.setAuthor( username );

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
