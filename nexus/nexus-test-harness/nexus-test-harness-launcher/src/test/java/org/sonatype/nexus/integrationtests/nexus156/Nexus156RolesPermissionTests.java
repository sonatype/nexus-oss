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
package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.rest.model.RoleResource;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus156RolesPermissionTests extends AbstractPrivilegeTest
{
    
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
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "34" );
        

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        
        role = this.roleUtil.getResourceFromResponse( response );
        
        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        role.setName( "testUpdatePermission2" );
        response = this.roleUtil.sendMessage( Method.PUT, role );
//        log.debug( "PROBLEM: "+ this.userUtil.getUser( "test-user" ) );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
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
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        
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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // read should fail
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        
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
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        role = this.roleUtil.getResourceFromResponse( response );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

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
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );
        
        
    }
    
    
}
