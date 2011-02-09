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
package org.sonatype.nexus.integrationtests.nexus3882;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Tests for deployment entries in feeds.
 */
public class Nexus3882IPAtAthenticationFailureFeedIT
    extends AbstractPrivilegeTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void failAuthentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "juka" );
        TestContainer.getInstance().getTestContext().setPassword( "juka" );

        // user doesn't exists, nexus shall not allow me to login
        Status status = UserCreationUtil.login();
        assertTrue( status.isError(), "Juka shall not be able to login: " + status );

        TestContainer.getInstance().getTestContext().useAdminForRequests();

        SyndFeed feed = FeedUtil.getFeed( "authcAuthz" );

        List<SyndEntry> entries = feed.getEntries();

        Assert.assertTrue( entries.size() >= 1, "Expected more then 1 entries, but got " + entries.size() + " - "
            + entries );

        validateIP( entries );
    }

    private static final Pattern V4 =
        Pattern.compile( "(([2]([5][0-5]|[0-4][0-9]))|([1][0-9]{2})|([1-9]?[0-9]))(\\.(([2]([5][0-5]|[0-4][0-9]))|([1][0-9]{2})|([1-9]?[0-9]))){3}" );

    private void validateIP( List<SyndEntry> entries )
        throws Exception
    {
        StringBuilder titles = new StringBuilder();

        for ( SyndEntry entry : entries )
        {
            // check if the title contains the file name (pom or jar)
            String title = entry.getDescription().getValue();
            titles.append( title );
            titles.append( ',' );

            Matcher match = V4.matcher( title );
            if ( match.find() )
            {
                return;
            }
        }

        Assert.fail( titles.toString() );
    }

}
