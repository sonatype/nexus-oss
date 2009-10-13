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
