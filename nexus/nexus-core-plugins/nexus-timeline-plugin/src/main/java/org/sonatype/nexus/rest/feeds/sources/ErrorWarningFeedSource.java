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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.sonatype.nexus.feeds.ErrorWarningEvent;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Show recored nexus errors and warnings
 * 
 * @author juven
 */
@Component( role = FeedSource.class, hint = "errorWarning" )
public class ErrorWarningFeedSource
    extends AbstractFeedSource
{
    public static final String CHANNEL_KEY = "errorWarning";

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    @Override
    public String getTitle()
    {
        return "Error and Warning";
    }

    @Override
    public String getDescription()
    {
        return "Error and Warning events";
    }

    public String getFeedName()
    {
        return getDescription();
    }

    public SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
        throws IOException
    {
        List<ErrorWarningEvent> items = getNexus().getErrorWarningEvents( from, count );

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemStatus().getVersion() );

        feed.setPublishedDate( new Date() );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( items.size() );

        for ( ErrorWarningEvent item : items )
        {
            SyndEntry entry = new SyndEntryImpl();

            if ( ErrorWarningEvent.ACTION_ERROR.equals( item.getAction() ) )
            {
                entry.setTitle( "Error" );
            }
            else if ( ErrorWarningEvent.ACTION_WARNING.equals( item.getAction() ) )
            {
                entry.setTitle( "Warning" );
            }
            else
            {
                entry.setTitle( item.getAction() );
            }

            SyndContent content = new SyndContentImpl();

            StringBuilder contentValue = new StringBuilder();

            contentValue.append( item.getMessage() );

            if ( StringUtils.isNotEmpty( item.getStackTrace() ) )
            {
                // we need <br/> and &nbsp; to display stack trace on RSS
                String stackTrace = item.getStackTrace().replace(
                    (String) System.getProperties().get( "line.separator" ),
                    "<br/>" );
                
                stackTrace = stackTrace.replace( "\t", "&nbsp;&nbsp;&nbsp;&nbsp;" );

                contentValue.append( "<br/>" ).append( stackTrace );
            }

            content.setType( MediaType.TEXT_PLAIN.toString() );

            content.setValue( contentValue.toString() );

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
