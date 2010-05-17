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
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

/**
 * @author juven
 */
public class Nexus1599ViewPrivilegeIT
    extends AbstractPrivilegeTest
{

    protected RepositoryMessageUtil repoMsgUtil;

    public Nexus1599ViewPrivilegeIT()
        throws Exception
    {
        super( REPO_TEST_HARNESS_REPO );

        this.repoMsgUtil = new RepositoryMessageUtil(
            this, this.getJsonXStream(),
            MediaType.APPLICATION_JSON,
            getRepositoryTypeRegistry() );
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
        this.giveUserRole( TEST_USER_NAME, "ui-search" );
        this.giveUserPrivilege( TEST_USER_NAME, "T1" ); // all m2 repo, read

        // without view privilege
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertEquals( 1, getSearchMessageUtil().searchFor( getTestId() ).size() );

        // with view privilege
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertEquals( 1, getSearchMessageUtil().searchFor( getTestId() ).size() );

        this.removePrivilege( TEST_USER_NAME, "T1" );
    }

    @Test
    public void testBrowseFeed()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-system-feeds" );
        this.giveUserPrivilege( TEST_USER_NAME, "T1" ); // all m2 repo, read

        // without view privilege
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertFalse( FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries().isEmpty() );

        // with view privilege
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertFalse( FeedUtil.getFeed( "recentlyChangedArtifacts" ).getEntries().isEmpty() );

        this.removePrivilege( TEST_USER_NAME, "T1" );
    }

    @Test
    public void testBrowseRepository()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        List<RepositoryListResource> repos = repoMsgUtil.getList();

        Assert.assertTrue( !repos.isEmpty() );

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
