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
package org.sonatype.nexus.integrationtests.nexus4301;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.ITHelperLogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * See NEXUS-4301: Load test impact of lots of WARN/ERROR logs as being recorded into Nexus feeds.
 * 
 * @author adreghiciu@gmail.com
 */
public class Nexus4301WarnErrorLogsLoadTestIT
    extends AbstractNexusIntegrationTest
{

    /**
     * When an ERROR is logged a corresponding feed entry should be created.
     */
    @Test
    public void test()
        throws Exception
    {

        final List<String> messagesError = new ArrayList<String>();
        final List<String> messagesWarn = new ArrayList<String>();

        Thread threadError = new Thread()
        {

            @Override
            public void run()
            {
                for ( int i = 0; i < 500; i++ )
                {
                    String message = generateMessage( "error" );
                    try
                    {
                        ITHelperLogUtils.error( message );
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                    messagesError.add( message );
                    yield();
                }
            }

        };

        Thread threadWarn = new Thread()
        {

            @Override
            public void run()
            {
                for ( int i = 0; i < 500; i++ )
                {
                    String message = generateMessage( "warn" );
                    try
                    {
                        ITHelperLogUtils.warn( message );
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                    messagesError.add( message );
                    yield();
                }
            }

        };

        threadError.start();
        threadWarn.start();

        threadError.join();
        threadWarn.join();

        // logging is asynchronous so give it a bit of time
        getEventInspectorsUtil().waitForCalmPeriod();
        
        SyndFeed feed = FeedUtil.getFeed( "errorWarning", 0, Integer.MAX_VALUE );

        for ( String message : messagesError )
        {
            assertFeedContainsEntryFor(feed, message );
        }

        for ( String message : messagesWarn )
        {
            assertFeedContainsEntryFor(feed, message );
        }
    }

    private String generateMessage( String id )
    {
        return this.getClass().getName() + "-" + System.currentTimeMillis() + "(" + id + ")";
    }

    private void assertFeedContainsEntryFor(SyndFeed feed, String message )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        List<SyndEntry> entries = feed.getEntries();
        for ( SyndEntry entry : entries )
        {
            SyndContent description = entry.getDescription();
            if ( description != null && description.getValue().startsWith( message ) )
            {
                return;
            }
        }
        Assert.fail( "Feed does not contain entry for " + message );
    }

}
