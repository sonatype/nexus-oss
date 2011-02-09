/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.test.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedUtil
{
    private static final String FEED_URL_PART = "service/local/feeds/";

    public static SyndFeed getFeed( String feedId )
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeedInput input = new SyndFeedInput();

        Response response =
            RequestFacade.sendMessage( FEED_URL_PART + feedId + "?_dc=" + System.currentTimeMillis(), Method.GET );

        String text = response.getEntity().getText();
        Assert.assertTrue( response.getStatus().isSuccess(), "Unexpected content: " + text );

        SyndFeed feed = input.build( new XmlReader( new ByteArrayInputStream( text.getBytes() ) ) );
        return feed;
    }

    @SuppressWarnings( "unchecked" )
    public static void sortSyndEntryOrderByPublishedDate( SyndFeed feed )
    {
        Collections.sort( feed.getEntries(), new Comparator<SyndEntry>()
        {
            public int compare( SyndEntry o1, SyndEntry o2 )
            {
                Date d1 = ( o1 ).getPublishedDate();
                Date d2 = ( o2 ).getPublishedDate();
                // sort desc by date
                if ( d2 != null && d1 != null )
                {
                    return d2.compareTo( d1 );
                }
                return -1;
            }
        } );
    }
}
