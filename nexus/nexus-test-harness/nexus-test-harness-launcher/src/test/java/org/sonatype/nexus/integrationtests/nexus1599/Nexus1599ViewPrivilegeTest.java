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

    protected RepositoryMessageUtil repoMsgUtil;

    protected SearchMessageUtil searchMsgUtil;

    public Nexus1599ViewPrivilegeTest()
        throws Exception
    {
        super( REPO_TEST_HARNESS_REPO );

        this.repoMsgUtil =
            new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );

        this.searchMsgUtil = new SearchMessageUtil();
    }

    @BeforeClass
    public static void enableSecureContext()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testSearchWithoutView()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-search" ); // search priv

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<NexusArtifact> results = searchMsgUtil.searchFor( getTestId() );
        Assert.assertTrue( "Without perm, the result should be empty! ", results.isEmpty() );
    }

    @Test
    public void testSearchWithView()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-search" ); // search priv
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<NexusArtifact> results = searchMsgUtil.searchFor( "nexus1599" );
        Assert.assertEquals( "With view perm, there should be 1 results, but was " + results.size() + " !", 1,
                             results.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFeedWithoutView()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-system-feeds" ); // feeds priv

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<SyndEntry> entries = FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries();
        for ( SyndEntry entry : entries )
        {
            Assert.assertFalse( entryContainsMsg( entry, REPO_TEST_HARNESS_REPO ) );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testFeedWithView()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-system-feeds" ); // feeds priv
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<SyndEntry> entries = FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries();
        Assert.assertTrue( "There should be one or more feeds returned!", entries.size() > 0 );
        Assert.assertTrue( entries.size() > entries.size() );
        boolean containRepo = false;
        for ( SyndEntry entry : entries )
        {
            if(entryContainsMsg( entry, REPO_TEST_HARNESS_REPO ) ) {
                containRepo = true;
                break;
            }
        }
        Assert.assertTrue("Not found repo feed " + entries, containRepo );

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
    public void testBrowseRepoWithoutView()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-repo-browser" ); // browser priv
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

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
        addPrivilege( TEST_USER_NAME, "repository-" + repoId );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertTrue( containsRepo( repoMsgUtil.getList(), repoId ) );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        removePrivilege( TEST_USER_NAME, "repository-" + repoId );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

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
