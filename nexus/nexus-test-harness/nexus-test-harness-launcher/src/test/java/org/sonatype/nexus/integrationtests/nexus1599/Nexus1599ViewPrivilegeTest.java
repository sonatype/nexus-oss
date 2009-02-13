/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus1599;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.RepositoryViewPrivilegeDescriptor;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * @author juven
 */
public class Nexus1599ViewPrivilegeTest
    extends AbstractPrivilegeTest
{
    private static final String TEST__REPO_ID = "nexus-test-harness-repo";

    private static final String TEST__REPO_NAME = "nexus-test-harness-repo";

    private static final String USER_ID = "test-user";

    private static final String PASSWORD = "admin123";

    protected RepositoryMessageUtil repoMsgUtil;

    protected SearchMessageUtil searchMsgUtil;

    public Nexus1599ViewPrivilegeTest()
        throws Exception
    {
        super( TEST__REPO_ID );

        this.repoMsgUtil = new RepositoryMessageUtil(
            this.getJsonXStream(),
            MediaType.APPLICATION_JSON,
            getRepositoryTypeRegistry() );

        this.searchMsgUtil = new SearchMessageUtil();
    }

    @BeforeClass
    public static void enableSecureContext()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testSearch()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        addPrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( TEST__REPO_ID ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        List<NexusArtifact> results = searchMsgUtil.searchFor( getTestId() );
        Assert.assertEquals( "With view perm, there should be 1 results, but was " + results.size() + " !", 1, results
            .size() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        removePrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( TEST__REPO_ID ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        results = searchMsgUtil.searchFor( getTestId() );
        Assert.assertTrue( "Without perm, the result should be empty! ", results.isEmpty() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testFeed()
        throws Exception
    {
        // with view perm, all entries returned
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        // rss feeds perm
        // addPrivilege( USER_ID, "44" );
        addPrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( TEST__REPO_ID ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        List<SyndEntry> entriesWithPerm = FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries();
        Assert.assertTrue( "There should be one or more feeds returned!", entriesWithPerm.size() > 0 );
        Assert.assertTrue( entryContainsMsg( entriesWithPerm.get( 0 ), TEST__REPO_NAME ) );

        // without view perm, entries related with the specific repo are filtered
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        removePrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( TEST__REPO_ID ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        List<SyndEntry> entriesWithoutPerm = FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries();
        Assert.assertTrue( entriesWithPerm.size() > entriesWithoutPerm.size() );
        for ( SyndEntry entry : entriesWithoutPerm )
        {
            Assert.assertFalse( entryContainsMsg( entry, TEST__REPO_NAME ) );
        }
    }

    private boolean entryContainsMsg( SyndEntry entry, String msg )
    {
        if ( entry.getDescription().getValue().contains( msg ) )
        {
            return true;
        }
        return false;
    }

    @Test
    public void testBrowseRepo()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        List<RepositoryListResource> repos = repoMsgUtil.getList();
        Assert.assertFalse( repos.isEmpty() );

        for ( RepositoryListResource repo : repos )
        {
            assertViewPrivilege( repo.getId() );
        }

    }

    private void assertViewPrivilege( String repoId )
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( repoId ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        Assert.assertTrue( containsRepo( repoMsgUtil.getList(), repoId ) );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        removePrivilege( USER_ID, RepositoryViewPrivilegeDescriptor.buildPrivilege( repoId ) );
        TestContainer.getInstance().getTestContext().setUsername( USER_ID );
        TestContainer.getInstance().getTestContext().setPassword( PASSWORD );

        Assert.assertFalse( "Repo '" + repoId + "' should be hidden!", containsRepo( repoMsgUtil.getList(), repoId ) );
    }

    private boolean containsRepo( List<RepositoryListResource> repos, String repoId )
    {
        for ( RepositoryListResource repo : repos )
        {
            if ( repo.getId().equals( repoId ) )
            {
                return true;
            }
        }
        return false;
    }

}
