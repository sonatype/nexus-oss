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
package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.rest.model.RoleResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus156RolesPermissionIT extends AbstractPrivilegeTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @Test
    public void testCreatePermission()
        throws IOException
    {
        RoleResource role = new RoleResource();

        role.setDescription( "testCreatePermission" );
        role.setName( "testCreatePermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "34" );
        

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        
        role = this.roleUtil.getResourceFromResponse( response );
        
        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        RoleResource role = new RoleResource();
        role.setDescription( "testUpdatePermission" );
        role.setName( "testUpdatePermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        role.setName( "testUpdatePermission2" );
        response = this.roleUtil.sendMessage( Method.PUT, role );
//        log.debug( "PROBLEM: "+ this.userUtil.getUser( "test-user" ) );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give update
        this.giveUserPrivilege( "test-user", "36" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );

        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        
    }
    
    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        RoleResource role = new RoleResource();
        role.setDescription( "testReadPermission" );
        role.setName( "testReadPermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give read
        this.giveUserPrivilege( "test-user", "35" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should fail
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        
    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        RoleResource role = new RoleResource();
        role.setDescription( "testUpdatePermission" );
        role.setName( "testUpdatePermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 201, "Response status: " );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "37" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );

        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Response status: " );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Response status: " );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Response status: " );
        
        
    }
    
    
}
