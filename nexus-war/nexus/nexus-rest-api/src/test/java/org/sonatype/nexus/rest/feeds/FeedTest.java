package org.sonatype.nexus.rest.feeds;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.Assert;

import org.restlet.Context;
import org.restlet.data.Request;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.sun.syndication.feed.synd.SyndFeed;

public class FeedTest
    extends AbstractNexusTestCase
{

    public void testFeedSources()
        throws Exception
    {

        Map<String, FeedSource> map = this.getContainer().lookupMap( FeedSource.class );

        System.out.println( "map: " + map );

        FeedPlexusResource feedResource = (FeedPlexusResource) this.lookup( PlexusResource.class, "feed" );

        // need to test the protected method, its a little hacky, but the problem i am trying to test has to do with
        // Plexus loading this class
        // so subclassing to expose this method, sort of get around what i am trying to test.

        System.out.println( "feedResource: " + feedResource );

        Field feedField = AbstractFeedPlexusResource.class.getDeclaredField( "feeds" );
        feedField.setAccessible( true );
        Map<String, FeedSource> feeds = (Map<String, FeedSource>) feedField.get( feedResource );

        Assert.assertNotNull( feeds );

        Method getFeedMethod = feedResource.getClass().getDeclaredMethod(
            "getFeed",
            Context.class,
            Request.class,
            String.class,
            Integer.class,
            Integer.class,
            Map.class );

        SyndFeed feed = (SyndFeed) getFeedMethod.invoke( feedResource, null, null, "brokenArtifacts", null, null, null );

        Assert.assertNotNull( feed );

    }

}
