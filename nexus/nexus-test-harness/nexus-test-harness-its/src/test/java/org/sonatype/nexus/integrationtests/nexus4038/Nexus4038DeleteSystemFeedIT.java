package org.sonatype.nexus.integrationtests.nexus4038;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class Nexus4038DeleteSystemFeedIT
    extends AbstractPrivilegeTest
{

    @Test
    public void delete()
        throws Exception
    {
        giveUserPrivilege( TEST_USER_NAME, "1000" );
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        Gav gav = GavUtil.newGav( "nexus4038", "artifact", "1.0" );
        assertTrue( Status.isSuccess( getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, gav,
            getTestFile( "artifact.jar" ) ) ) );

        // timeline resolution is _one second_, so to be sure that ordering is kept he keep gaps between operations bigger than one second 
        Thread.sleep( 1100 );
        getEventInspectorsUtil().waitForCalmPeriod();

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String serviceURI =
            "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content/"
                + GavUtil.getRelitiveArtifactPath( gav ).replace( ".jar", ".pom" );
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Failed to delete " + gav + status );

        // timeline resolution is _one second_, so to be sure that ordering is kept he keep gaps between operations bigger than one second 
        Thread.sleep( 1100 );
        getEventInspectorsUtil().waitForCalmPeriod();

        SyndFeed feed = FeedUtil.getFeed( "recentlyChangedArtifacts" );

        List<SyndEntry> entries = feed.getEntries();

        Assert.assertTrue( entries.size() >= 2, "Expected more than 2 entries, but got " + entries.size() + " - "
            + entries );

        List<String> desc = new ArrayList<String>();
        for ( SyndEntry entry : entries )
        {
            desc.add( entry.getDescription().getValue() );
        }

        assertThat( desc, hasItem( containsString( "deleted.Action was initiated by user \"" + TEST_USER_NAME + "\"" ) ) );
    }
}
