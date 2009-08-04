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
package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;

/**
 * Test the privilege for CRUD operations.
 */
public class Nexus385RoutesPermissionTests extends AbstractPrivilegeTest
{
    
    @Test
    public void testCreatePermission()
        throws IOException
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );
        
        RepositoryRouteResource route = new RepositoryRouteResource();
        route.setGroupId( "nexus-test" );
        route.setPattern( ".*testCreatePermission.*" );
        route.setRuleType( "blocking" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( TEST_USER_NAME, "22" );   

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        route = this.routeUtil.getResourceFromResponse( response );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryRouteResource route = new RepositoryRouteResource();
        route.setGroupId( "nexus-test" );
        route.setPattern( ".*testUpdatePermission.*" );
        route.setRuleType( "blocking" );

        Response response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        route.setPattern( ".*testUpdatePermission2.*" );
        response = this.routeUtil.sendMessage( Method.PUT, route );
//        log.debug( "PROBLEM: "+ this.userUtil.getUser( TEST_USER_NAME ) );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give update
        this.giveUserPrivilege( TEST_USER_NAME, "24" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        
    }
    
    @Test
    public void testReadPermission()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryRouteResource route = new RepositoryRouteResource();
        route.setGroupId( "nexus-test" );
        route.setPattern( ".*testUpdatePermission.*" );
        route.setRuleType( "blocking" );

        Response response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give read
        this.giveUserPrivilege( TEST_USER_NAME, "23" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // read should fail
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        
    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryRouteResource route = new RepositoryRouteResource();
        route.setGroupId( "nexus-test" );
        route.setPattern( ".*testUpdatePermission.*" );
        route.setRuleType( "blocking" );

        Response response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( TEST_USER_NAME, "25" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );
        
        
    }
    
    
}
