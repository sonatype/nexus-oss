/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.selenium.nexus2191;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import org.codehaus.plexus.component.annotations.Component;
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
        assertThat( feeds.getFeedData( "description" ), startsWith( "Successfully authenticated user [admin]" ) );
    }
}
