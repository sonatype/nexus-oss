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
package org.sonatype.nexus.integrationtests.nexus4038;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.hamcrest.MatcherAssert;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.Assert;
import org.testng.AssertJUnit;
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

        // timeline resolution is _one second_, so to be sure that ordering is kept he keep gaps between operations
        // bigger than one second
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

        // timeline resolution is _one second_, so to be sure that ordering is kept he keep gaps between operations
        // bigger than one second
        Thread.sleep( 1100 );
        getEventInspectorsUtil().waitForCalmPeriod();

        SyndFeed feed = FeedUtil.getFeed( "recentlyChangedArtifacts" );

        List<SyndEntry> entries = feed.getEntries();

        Assert.assertTrue( entries.size() >= 2, "Expected more than 2 entries, but got " + entries.size() + " - "
            + entries );

        final String expected = "deleted.Action was initiated by user \"" + TEST_USER_NAME + "\"";
        boolean foundExpected = false;
        List<String> desc = new ArrayList<String>();
        for ( SyndEntry entry : entries )
        {
            final String val = entry.getDescription().getValue();
            desc.add( val );
            if(val.contains( expected )){
                foundExpected = true;
            }
        }

        // FIXME not sure why this does not compile atm on cmd line, eclipse seems happy
        // assertThat( desc, hasItem( containsString( expected ) ) );
        // HACK
        assertThat("Did not find expected string in any value", foundExpected);

    }
}
