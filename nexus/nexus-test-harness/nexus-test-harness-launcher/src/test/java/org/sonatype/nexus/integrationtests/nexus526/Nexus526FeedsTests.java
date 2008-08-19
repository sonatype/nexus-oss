package org.sonatype.nexus.integrationtests.nexus526;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Nexus526FeedsTests
    extends AbstractNexusIntegrationTest
{

    private static final String FEED_URL_PART = "service/local/feeds/";

    private Gav gav;

    public Nexus526FeedsTests()
    {
        super( "nexus-test-harness-repo" );
        this.gav =
            new Gav( this.getTestId(), "artifact1", "1.0.0", null, "jar", 0, new Date().getTime(), "Artifact 1", false,
                     false, null, false, null );
    }

    private URL getFeedUrl( String feedId )
        throws MalformedURLException
    {
        return new URL( this.getBaseNexusUrl() + FEED_URL_PART + feedId );
    }

    private SyndFeed getFeed( String feedId )
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build( new XmlReader( this.getFeedUrl( feedId ) ) );
        // sort it by date
        sortSyndEntryOrderByPublishedDate( feed );

        return feed;
    }

    private void validateArtifactInFeedEntries( List<SyndEntry> entries, Gav gav, String... extensions )
        throws FileNotFoundException
    {

        List<String> links = new ArrayList<String>();

        for ( SyndEntry entry : entries )
        {

            // check if the title contains the groupid, artifactid, and version
            String title = entry.getTitle();

            Assert.assertTrue( "Feed title does not contain the groupId. Title was: " + title,
                               title.contains( gav.getGroupId() ) );
            Assert.assertTrue( "Feed title does not contain the artifactId. Title was: " + title,
                               title.contains( gav.getArtifactId() ) );
            Assert.assertTrue( "Feed title does not contain the version. Title was: " + title,
                               title.contains( gav.getVersion() ) );

            // check link
            String link = entry.getLink();
            Assert.assertNotNull( "Feed title link was null", link );

            // http://
            //localhost:8087/nexus/content/repositories/nexus-test-harness-repo/nexus526/artifact1/1.0.0/artifact1-1.0.0
            // .pom

            links.add( link );
        }

        for ( String extention : extensions )
        {
            String expectedUrl =
                this.getBaseNexusUrl() + "content/repositories/" + this.getTestRepositoryId() + "/"
                    + this.getRelitiveArtifactPath( gav );
            expectedUrl = expectedUrl.replaceAll( gav.getExtension() + "$", extention );
            Assert.assertTrue( "The feed link was wrong", links.contains( expectedUrl ) );
        }

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentlyDeployedFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = this.getFeed( "recentlyDeployed" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

        this.validateArtifactInFeedEntries( entries, gav, "pom", "jar" );

        // problems at windows
        // get last entry
        // SyndEntry lastEntry = entries.get( 0 );
        // this.validateArtifactInFeedEntry( lastEntry, gav, "pom" ); // one is the pom
        // this.validateArtifactInFeedEntry( entries.get( 1 ), gav, "jar" ); // the other the jar
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentChangesFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = this.getFeed( "recentChanges" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

        this.validateArtifactInFeedEntries( entries, gav, "pom", "jar" );

        // problems at windows
        // get last entry
        // SyndEntry lastEntry = entries.get( 0 );
        // this.validateArtifactInFeedEntry( lastEntry, gav, "pom" ); // one is the pom
        // this.validateArtifactInFeedEntry( entries.get( 1 ), gav, "jar" ); // the other the jar
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentCacheOrDeploymentsFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = this.getFeed( "recentCacheOrDeployments" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

        this.validateArtifactInFeedEntries( entries, gav, "pom", "jar" );

        // problems at windows
        // get last entry
        // SyndEntry lastEntry = entries.get( 0 );
        // this.validateArtifactInFeedEntry( lastEntry, gav, "pom" ); // one is the pom
        // this.validateArtifactInFeedEntry( entries.get( 1 ), gav, "jar" ); // the other the jar
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
