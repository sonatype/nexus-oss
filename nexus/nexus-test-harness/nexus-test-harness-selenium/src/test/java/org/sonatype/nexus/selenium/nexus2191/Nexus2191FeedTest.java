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
package org.sonatype.nexus.selenium.nexus2191;

import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.hamcrest.text.StringStartsWith;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.FeedsTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2191FeedTest.class )
public class Nexus2191FeedTest
    extends SeleniumTest
{

    @Test
    public void authFeed()
    {
        doLogin();

        Assert.assertTrue( main.viewsPanel().systemFeedsAvailable() );

        FeedsTab feeds = main.openFeeds().selectCategory( "authcAuthz" ).selectFeed( 0 );

        Assert.assertEquals( "Authentication", feeds.getFeedData( "title" ) );
        Assert.assertEquals( "", feeds.getFeedData( "author" ) );
        Assert.assertEquals( "", feeds.getFeedData( "content" ) );
        assertThat( feeds.getFeedData( "description" ),
                    StringStartsWith.startsWith( "Successfully authenticated user [admin]" ) );
    }
}
