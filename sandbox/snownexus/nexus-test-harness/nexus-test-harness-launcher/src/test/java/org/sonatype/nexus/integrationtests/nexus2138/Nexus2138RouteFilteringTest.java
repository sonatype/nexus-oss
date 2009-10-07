package org.sonatype.nexus.integrationtests.nexus2138;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;

public class Nexus2138RouteFilteringTest
    extends AbstractPrivilegeTest
{

    @Test
    public void testRouteList()
        throws Exception
    {
        // create some test routes
        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );

        response = this.createRouteTest( "public", this.getTestRepositoryId(), "nexus-test-harness-release-repo" );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );

        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertEquals( 0, this.routeUtil.getList().size() );
    }

    @Test
    public void testFilteredList()
        throws Exception
    {
        RoutesMessageUtil.removeAllRoutes();
        
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );

        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + "public" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + this.getTestRepositoryId() );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<RepositoryRouteListResource> routes = this.routeUtil.getList();
        Assert.assertEquals( 1, routes.size() );
        Assert.assertEquals( "public", routes.get( 0 ).getGroupId() );
    }
    
    @Test
    public void testFilteredListWithOutAccessToGroup()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + this.getTestRepositoryId() );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Assert.assertEquals( 0, this.routeUtil.getList().size() );
    }

    @Test
    public void testForbiddendGet()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );
        RepositoryRouteResource routeResource = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        response = RequestFacade.doGetRequest( RoutesMessageUtil.SERVICE_PART + "/" + routeResource.getId() );
        Assert.assertEquals( Status.CLIENT_ERROR_FORBIDDEN, response.getStatus() );
    }

    @Test
    public void testForbiddendGetWithAccessToGroup()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );

        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + "public" );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );
        RepositoryRouteResource routeResource = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        response = RequestFacade.doGetRequest( RoutesMessageUtil.SERVICE_PART + "/" + routeResource.getId() );
        Assert.assertEquals( Status.CLIENT_ERROR_FORBIDDEN, response.getStatus() );
    }

    @Test
    public void testForbiddendGetWithOutAccessToGroup()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + this.getTestRepositoryId() );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId() );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );
        RepositoryRouteResource routeResource = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        response = RequestFacade.doGetRequest( RoutesMessageUtil.SERVICE_PART + "/" + routeResource.getId() );
        Assert.assertEquals( Status.CLIENT_ERROR_FORBIDDEN, response.getStatus() );
    }

    @Test
    public void testForbiddendGetWithAccessToGroupAndOneRepo()
        throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "ui-routing-admin" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + "public" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + this.getTestRepositoryId() );

        Response response = this.createRouteTest( "public", this.getTestRepositoryId(), "nexus-test-harness-release-repo" );
        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );
        RepositoryRouteResource routeResource = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        response = RequestFacade.doGetRequest( RoutesMessageUtil.SERVICE_PART + "/" + routeResource.getId() );
        Assert.assertEquals( Status.CLIENT_ERROR_FORBIDDEN, response.getStatus() );
    }

    private Response createRouteTest( String groupId, String... repoIds )
        throws Exception
    {
        RepositoryRouteResource routeResource = new RepositoryRouteResource();
        routeResource.setGroupId( groupId );
        routeResource.setRuleType( RepositoryRouteResource.EXCLUSION_RULE_TYPE );
        routeResource.setPattern( ".*/test-me/.*" );

        for ( String memberRepoId : repoIds )
        {
            RepositoryRouteMemberRepository memberRepoReleases = new RepositoryRouteMemberRepository();
            memberRepoReleases.setId( memberRepoId );
            routeResource.addRepository( memberRepoReleases );
        }

        return this.routeUtil.sendMessage( Method.POST, routeResource );
    }
}
