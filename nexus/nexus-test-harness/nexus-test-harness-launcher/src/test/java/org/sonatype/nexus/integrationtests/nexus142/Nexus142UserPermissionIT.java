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
package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.rest.model.UserResource;

/**
 * Test the privileges for CRUD operations.
 */
public class Nexus142UserPermissionIT
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        // create a user with anon access

        UserResource user = new UserResource();
        user.setEmail( "tesCreatePermission@foo.org" );
        user.setName( "tesCreatePermission" );
        user.setUserId( "tesCreatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "38" );

        // print out the users privs
//        this.printUserPrivs( "test-user" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "tesUpdatePermission@foo.org" );
        user.setName( "tesUpdatePermission" );
        user.setUserId( "tesUpdatePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give update
        this.giveUserPrivilege( "test-user", "40" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }
    
    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testReadPermission@foo.org" );
        user.setName( "testReadPermission" );
        user.setUserId( "testReadPermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        user.setUserId( "tesUpdatePermission" );
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give read
        this.giveUserPrivilege( "test-user", "39" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }
    
    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        UserResource user = new UserResource();
        user.setEmail( "testDeletePermission@foo.org" );
        user.setName( "testDeletePermission" );
        user.setUserId( "testDeletePermission" );
        user.setStatus( "active" );
        user.addRole( "anonymous" );

        Response response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give delete
        this.giveUserPrivilege( "test-user", "41" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...

        // update user
        response = this.userUtil.sendMessage( Method.PUT, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.userUtil.sendMessage( Method.GET, user );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.userUtil.sendMessage( Method.POST, user );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.userUtil.sendMessage( Method.DELETE, user );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );

    }

}
