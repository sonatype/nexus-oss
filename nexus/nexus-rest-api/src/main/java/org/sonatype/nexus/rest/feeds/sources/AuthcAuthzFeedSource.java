package org.sonatype.nexus.rest.feeds.sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.MediaType;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

@Component( role = FeedSource.class, hint = "authcAuthz" )
public class AuthcAuthzFeedSource
    extends AbstractFeedSource
{

    public static final String CHANNEL_KEY = "authcAuthz";

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    public String getFeedName()
    {
        return getDescription();
    }

    @Override
    public String getTitle()
    {
        return "Authentication and Authorization";
    }

    @Override
    public String getDescription()
    {
        return "Authentication and Authorization Events";
    }

    public SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
        throws IOException
    {
        List<AuthcAuthzEvent> items = getNexus().getAuthcAuthzEvents( from, count );

        SyndFeedImpl feed = new SyndFeedImpl();

        feed.setTitle( getTitle() );

        feed.setDescription( getDescription() );

        feed.setAuthor( "Nexus " + getNexus().getSystemStatus().getVersion() );

        feed.setPublishedDate( new Date() );

        List<SyndEntry> entries = new ArrayList<SyndEntry>( items.size() );

        for ( AuthcAuthzEvent item : items )
        {
            SyndEntry entry = new SyndEntryImpl();

            if ( FeedRecorder.SYSTEM_AUTHC.equals( item.getAction() ) )
            {
                entry.setTitle( "Authencation" );
            }
            else if ( FeedRecorder.SYSTEM_AUTHZ.equals( item.getAction() ) )
            {
                entry.setTitle( "Authorization" );
            }
            else
            {
                entry.setTitle( item.getAction() );
            }

            SyndContent content = new SyndContentImpl();

            content.setType( MediaType.TEXT_PLAIN.toString() );

            content.setValue( item.getMessage() );

            entry.setPublishedDate( item.getEventDate() );

            entry.setAuthor( feed.getAuthor() );

            entry.setLink( "http://nexus.sonatype.org/" );

            entry.setDescription( content );

            entries.add( entry );
            
        }
        
        feed.setEntries( entries );

        return feed;
    }

}
