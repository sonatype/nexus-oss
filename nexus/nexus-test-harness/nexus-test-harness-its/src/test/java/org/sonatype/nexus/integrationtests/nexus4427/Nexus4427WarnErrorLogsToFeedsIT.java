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
package org.sonatype.nexus.integrationtests.nexus4427;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.ITHelperLogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Multiset.Entry;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

/**
 * See NEXUS-4427: Test that WARN/ERROR logs are recorded into Nexus feeds.
 * 
 * @author adreghiciu@gmail.com
 */
public class Nexus4427WarnErrorLogsToFeedsIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void error()
        throws Exception
    {
        String message = this.getClass().getName();
        ITHelperLogUtils.error( message );
        assertFeedEntryFor( message );
    }
    
    @Test
    public void warn()
        throws Exception
    {
        String message = this.getClass().getName();
        ITHelperLogUtils.warn( message );
        assertFeedEntryFor( message );
    }

    private void assertFeedEntryFor( String message )
        throws Exception
    {
        SyndFeed feed = FeedUtil.getFeed( "errorWarning" );
        @SuppressWarnings( "unchecked" )
        List<SyndEntry> entries = feed.getEntries();
        for ( SyndEntry entry : entries )
        {
            SyndContent description = entry.getDescription();
            if ( description != null && description.getValue().equals( message ) )
            {
                return;
            }
        }
        Assert.fail( "No feed entry found for " + message );
    }

}
