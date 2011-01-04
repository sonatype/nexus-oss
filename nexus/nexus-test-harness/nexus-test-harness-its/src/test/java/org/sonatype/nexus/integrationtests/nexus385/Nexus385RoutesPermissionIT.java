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
package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privilege for CRUD operations.
 */
public class Nexus385RoutesPermissionIT extends AbstractPrivilegeTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
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
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( TEST_USER_NAME, "22" );   

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        route = this.routeUtil.getResourceFromResponse( response );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        route.setPattern( ".*testUpdatePermission2.*" );
        response = this.routeUtil.sendMessage( Method.PUT, route );
//        log.debug( "PROBLEM: "+ this.userUtil.getUser( TEST_USER_NAME ) );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        
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
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.routeUtil.sendMessage( Method.PUT, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should fail
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        
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
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        route = this.routeUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

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
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should succeed (inherited)
        response = this.routeUtil.sendMessage( Method.GET, route );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.routeUtil.sendMessage( Method.POST, route );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.routeUtil.sendMessage( Method.DELETE, route );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );
        
        
    }
    
    
}
