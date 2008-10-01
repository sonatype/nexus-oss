package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedUtil
{

    private static final String FEED_URL_PART = "service/local/feeds/";
    
    private static URL getFeedUrl( String feedId )
        throws MalformedURLException
    {
        return new URL( TestProperties.getString( "nexus.base.url" ) + FEED_URL_PART + feedId );
    }

    public static SyndFeed getFeed( String feedId )
        throws IllegalArgumentException,
            MalformedURLException,
            FeedException,
            IOException
    {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build( new XmlReader( getFeedUrl( feedId ) ) );
        // sort it by date
        sortSyndEntryOrderByPublishedDate( feed );

        return feed;
    }

    @SuppressWarnings( "unchecked" )
    public static void sortSyndEntryOrderByPublishedDate( SyndFeed feed )
    {
        Collections.sort( feed.getEntries(), new Comparator<SyndEntry>()
        {
            public int compare( SyndEntry o1, SyndEntry o2 )
            {
                Date d1 = ( (SyndEntry) o1 ).getPublishedDate();
                Date d2 = ( (SyndEntry) o2 ).getPublishedDate();
                // sort desc by date
                if ( d2 != null && d1 != null )
                    return d2.compareTo( d1 );
                return -1;
            }
        } );
    }

}
