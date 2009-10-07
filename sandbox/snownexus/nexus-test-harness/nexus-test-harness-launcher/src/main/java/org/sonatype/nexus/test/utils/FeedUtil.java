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
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import junit.framework.Assert;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedUtil
{
    private static final String FEED_URL_PART = "service/local/feeds/";

    public static SyndFeed getFeed( String feedId )
        throws IllegalArgumentException,
            MalformedURLException,
            FeedException,
            IOException
    {
        SyndFeedInput input = new SyndFeedInput();
        
        Response response = RequestFacade.sendMessage( FEED_URL_PART + feedId + "?_dc=" + System.currentTimeMillis(), Method.GET );
        Assert.assertTrue( "Expected content", response.getEntity().isAvailable());
        
        SyndFeed feed = input.build( new XmlReader( response.getEntity().getStream() ) );

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
