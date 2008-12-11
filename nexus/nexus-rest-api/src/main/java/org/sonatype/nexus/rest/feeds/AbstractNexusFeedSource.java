/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.feeds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
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
    @Requirement
    private NexusItemAuthorizer nexusItemAuthorizer;

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
                        
                        result.append( " : " ).append( gav.getExtension() );
                        
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

    public abstract List<NexusArtifactEvent> getEventList( Integer from, Integer count, Map<String, String> params );

    public abstract String getTitle();

    public abstract String getDescription();

    public SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
    {
        List<NexusArtifactEvent> items = getEventList( from, count, params );

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemStatus().getVersion() );

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

            try
            {
                Repository repo = getNexus().getRepository( item.getNexusItemInfo().getRepositoryId() );

                RepositoryItemUid uid = repo.createUid( item.getNexusItemInfo().getPath() );

                if ( nexusItemAuthorizer.authorizePath( uid, null, Action.read ) )
                {
                    gav = getGav( item.getNexusItemInfo() );

                    StringBuffer msg = new StringBuffer( "The " )
                        .append( gav ).append( " artifact in repository " ).append(
                            item.getNexusItemInfo().getRepositoryId() ).append( " is " );

                    if ( NexusArtifactEvent.ACTION_CACHED.equals( item.getAction() ) )
                    {
                        msg
                            .append( "cached by Nexus from remote URL " ).append(
                                item.getNexusItemInfo().getRemoteUrl() ).append( "." );

                        if ( username != null )
                        {
                            msg
                                .append( " Caching happened to fulfill a request from user " ).append( username )
                                .append( "." );
                        }
                    }
                    else if ( NexusArtifactEvent.ACTION_DEPLOYED.equals( item.getAction() ) )
                    {
                        msg.append( "deployed into Nexus." );

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
            }
            catch ( NoSuchRepositoryException e )
            {
                // Can't get repository for artifact, therefore we can't authorize access, therefore you dont see it
                getLogger().debug(
                    "Feed entry contained invalid repository id " + item.getNexusItemInfo().getRepositoryId(),
                    e );
            }
        }

        feed.setEntries( entries );

        return feed;
    }
}
