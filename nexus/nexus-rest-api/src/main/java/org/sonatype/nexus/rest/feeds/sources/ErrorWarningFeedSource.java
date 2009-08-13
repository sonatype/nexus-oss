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

            StringBuffer contentValue = new StringBuffer();

            contentValue.append( item.getMessage() );

            if ( StringUtils.isNotEmpty( item.getStackTrace() ) )
            {
                contentValue.append( "\n" ).append( item.getStackTrace() );
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
